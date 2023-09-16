package com.sola.anime.ai.generator.feature.processing.batch

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.ViewGroup
import androidx.core.view.drawToBitmap
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.basic.common.base.LsActivity
import com.basic.common.extension.clicks
import com.basic.common.extension.getDimens
import com.basic.common.extension.lightStatusBar
import com.basic.common.extension.makeToast
import com.basic.common.extension.transparent
import com.basic.common.extension.tryOrNull
import com.sola.anime.ai.generator.common.ConfigApp
import com.sola.anime.ai.generator.common.extension.back
import com.sola.anime.ai.generator.common.extension.getStatusBarHeight
import com.sola.anime.ai.generator.common.extension.show
import com.sola.anime.ai.generator.common.extension.startIap
import com.sola.anime.ai.generator.common.extension.toChildHistory
import com.sola.anime.ai.generator.common.ui.sheet.download.DownloadSheet
import com.sola.anime.ai.generator.common.util.AESEncyption
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.databinding.ActivityBatchProcessingBinding
import com.sola.anime.ai.generator.domain.manager.AnalyticManager
import com.sola.anime.ai.generator.domain.manager.UserPremiumManager
import com.sola.anime.ai.generator.domain.model.status.DezgoStatusTextToImage
import com.sola.anime.ai.generator.domain.model.status.GenerateTextsToImagesProgress
import com.sola.anime.ai.generator.domain.model.status.StatusBodyTextToImage
import com.sola.anime.ai.generator.domain.repo.DezgoApiRepository
import com.sola.anime.ai.generator.domain.repo.FileRepository
import com.sola.anime.ai.generator.domain.repo.HistoryRepository
import com.sola.anime.ai.generator.feature.processing.batch.adapter.PreviewAdapter
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class BatchProcessingActivity : LsActivity<ActivityBatchProcessingBinding>(ActivityBatchProcessingBinding::inflate) {

    @Inject lateinit var previewAdapter: PreviewAdapter
    @Inject lateinit var configApp: ConfigApp
    @Inject lateinit var analyticManager: AnalyticManager
    @Inject lateinit var dezgoApiRepo: DezgoApiRepository
    @Inject lateinit var historyRepo: HistoryRepository
    @Inject lateinit var prefs: Preferences
    @Inject lateinit var userPremiumManager: UserPremiumManager
    @Inject lateinit var fileRepo: FileRepository

    private val totalCreditsDeducted by lazy { intent.getFloatExtra("totalCreditsDeducted", 0f) }
    private val creditsPerImage by lazy { intent.getFloatExtra("creditsPerImage", 0f) }
    private var dezgoStatusTextsToImages = listOf<DezgoStatusTextToImage>()
    private var isSuccessAll = false
    private val downloadSheet by lazy { DownloadSheet() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transparent()
        lightStatusBar()
        setContentView(binding.root)

        analyticManager.logEvent(AnalyticManager.TYPE.GENERATE_PROCESSING_BATCH)

        initView()
        initData()
        initObservable()
        listenerView()
    }

    private fun initObservable() {
        downloadSheet
            .downloadFrameClicks
            .autoDispose(scope())
            .subscribe { view ->
                tryOrNull {
                    analyticManager.logEvent(AnalyticManager.TYPE.DOWNLOAD_CLICKED)

                    lifecycleScope.launch(Dispatchers.IO) {
                        val bitmap = tryOrNull { view.drawToBitmap() } ?: return@launch
                        fileRepo.downloads(bitmap)
                        launch(Dispatchers.Main) {
                            tryOrNull { downloadSheet.dismiss() }

                            makeToast("Download successfully!")
                        }
                    }
                }
            }

        downloadSheet
            .downloadOriginalClicks
            .autoDispose(scope())
            .subscribe { file ->

                val task = {
                    analyticManager.logEvent(AnalyticManager.TYPE.DOWNLOAD_ORIGINAL_CLICKED)

                    tryOrNull {
                        lifecycleScope.launch {
                            fileRepo.downloads(file)
                            launch(Dispatchers.Main) {
                                prefs.numberDownloadedOriginal.set(prefs.numberDownloadedOriginal.get() + 1)

                                tryOrNull { downloadSheet.dismiss() }

                                makeToast("Download successfully!")
                            }
                        }
                    }
                }

                when {
                    !prefs.isUpgraded.get() -> startIap()
                    else -> task()
                }
            }

        previewAdapter
            .downloadClicks
            .autoDispose(scope())
            .subscribe { dezgoStatus ->
                when (val status = dezgoStatus.status) {
                    is StatusBodyTextToImage.Success -> {
                        val file = status.file

                        if (!file.exists()){
                            return@subscribe
                        }

                        val body = dezgoStatus.body

                        downloadSheet.file = file
                        downloadSheet.ratio = "${body.width}:${body.height}"
                        downloadSheet.show(this)
                    }

                    else -> {}
                }
            }
    }

    private fun initData() {
        val markFailed = {
            analyticManager.logEvent(AnalyticManager.TYPE.GENERATE_FAILED_BATCH)

            makeToast("Server error, please wait for us to fix the error or try again!")
            back()
        }

        when {
            configApp.dezgoBodiesTextsToImages.isEmpty() -> {
                markFailed()
                return
            }
            else -> {
                lifecycleScope.launch(Dispatchers.Main) {
                    dezgoStatusTextsToImages = configApp.dezgoBodiesTextsToImages.flatMap { dezgo -> dezgo.bodies.map { body -> DezgoStatusTextToImage(body = body, status = StatusBodyTextToImage.Loading) } }
                    previewAdapter.data = dezgoStatusTextsToImages

                    prefs.getUserPurchased()?.let {
                        val isSuccess = userPremiumManager.updateCredits(prefs.getCredits() - totalCreditsDeducted)

                        launch(Dispatchers.Main) {
                            when {
                                isSuccess -> {
                                    prefs.setCredits(prefs.getCredits() - totalCreditsDeducted)

                                    generate()
                                }
                                else -> markFailed()
                            }
                        }
                    } ?: run {
                        prefs.setCredits(prefs.getCredits() - totalCreditsDeducted)

                        generate()
                    }
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun generate() {
        tryOrNull {
            lifecycleScope.launch(Dispatchers.Main) {
                val deferredHistoryIds = arrayListOf<Long?>()

                dezgoApiRepo.generateTextsToImages(
                    keyApi = AESEncyption.decrypt(configApp.keyDezgoPremium) ?: "",
                    datas = ArrayList(configApp.dezgoBodiesTextsToImages),
                    progress = { progress ->
                        when (progress){
                            GenerateTextsToImagesProgress.Idle -> {}
                            GenerateTextsToImagesProgress.Loading -> {}
                            is GenerateTextsToImagesProgress.LoadingWithId -> launch(Dispatchers.Main) { markLoadingWithIdAndChildId(groupId = progress.groupId, childId = progress.childId) }
                            is GenerateTextsToImagesProgress.SuccessWithId ->  {
                                launch(Dispatchers.Main) {
                                    configApp
                                        .dezgoBodiesTextsToImages
                                        .find { dezgo ->
                                            dezgo.id == progress.groupId
                                        }?.bodies
                                        ?.find { body ->
                                            body.id == progress.childId && body.groupId == progress.groupId
                                        }?.toChildHistory(progress.file.path)?.let {
                                            deferredHistoryIds.add(historyRepo.markHistory(it))
                                        }

                                    markSuccessWithIdAndChildId(groupId = progress.groupId, childId = progress.childId, file = progress.file)
                                }
                            }
                            is GenerateTextsToImagesProgress.FailureWithId -> launch(Dispatchers.Main) { markFailureWithIdAndChildId(groupId = progress.groupId, childId = progress.childId) }
                            is GenerateTextsToImagesProgress.Done ->  {
                                launch(Dispatchers.Main) {
                                    isSuccessAll = true

                                    previewAdapter.notifyDataSetChanged()
                                }
                            }
                        }
                    }
                )
            }
        } ?: run {
            makeToast("Server error, please wait for us to fix the error or try again!")
            back()
        }
    }

    private fun markLoadingWithIdAndChildId(groupId: Long, childId: Long) {
        dezgoStatusTextsToImages
            .find { status ->
                status.body.id == childId && status.body.groupId == groupId
            }?.status = StatusBodyTextToImage.Loading

        dezgoStatusTextsToImages
            .indexOfFirst { status ->
                status.body.id == childId && status.body.groupId == groupId
            }.takeIf { it != -1 }?.let { index ->
                lifecycleScope.launch(Dispatchers.Main) {
                    previewAdapter.notifyItemChanged(index)
                }
            }
    }

    private fun markSuccessWithIdAndChildId(groupId: Long, childId: Long, file: File) {
        dezgoStatusTextsToImages
            .find { status ->
                status.body.id == childId && status.body.groupId == groupId
            }?.status = StatusBodyTextToImage.Success(file)

        dezgoStatusTextsToImages
            .indexOfFirst { status ->
                status.body.id == childId && status.body.groupId == groupId
            }.takeIf { it != -1 }?.let { index ->
                lifecycleScope.launch(Dispatchers.Main) {
                    previewAdapter.notifyItemChanged(index)
                }
            }
    }

    private fun markFailureWithIdAndChildId(groupId: Long, childId: Long) {
        dezgoStatusTextsToImages
            .find { status ->
                status.body.id == childId && status.body.groupId == groupId
            }?.status = StatusBodyTextToImage.Failure()

        dezgoStatusTextsToImages
            .indexOfFirst { status ->
                status.body.id == childId && status.body.groupId == groupId
            }.takeIf { it != -1 }?.let { index ->
                lifecycleScope.launch(Dispatchers.Main) {
                    previewAdapter.notifyItemChanged(index)
                }
            }
    }

    private fun initView() {
        binding.viewTop.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            this.topMargin = when(val statusBarHeight = getStatusBarHeight()) {
                0 -> getDimens(com.intuit.sdp.R.dimen._30sdp).toInt()
                else -> statusBarHeight
            }
        }
        binding.recyclerView.apply {
            this.itemAnimator = null
            this.layoutManager = object: StaggeredGridLayoutManager(2, VERTICAL){
                override fun canScrollVertically(): Boolean {
                    return false
                }
            }
            this.adapter = previewAdapter
        }
    }

    private fun listenerView() {
        binding.back.clicks { onBackPressed() }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
//            binding.nestedScrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
//                val alpha = scrollY.toFloat() / binding.viewShadow.height.toFloat()
//
//                binding.viewShadow.alpha = alpha
//            }
//        }
    }

    @Deprecated("Deprecated in Java", ReplaceWith("finish()"))
    override fun onBackPressed() {
        when {
            !isSuccessAll -> {
                MaterialDialog(this)
                    .show {
                        title(text = "Processing")
                        message(text = "Do you want to cancel the image creation process in progress?")
                        positiveButton(text = "Yes") {
                            back()
                        }
                        negativeButton(text = "No") { dialog ->
                            dialog.dismiss()
                        }
                }
            }
            else -> back()
        }
    }

}