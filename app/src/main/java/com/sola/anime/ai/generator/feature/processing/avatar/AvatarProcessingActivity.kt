package com.sola.anime.ai.generator.feature.processing.avatar

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.ViewGroup
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
import com.sola.anime.ai.generator.BuildConfig
import com.sola.anime.ai.generator.common.ConfigApp
import com.sola.anime.ai.generator.common.Constraint
import com.sola.anime.ai.generator.common.extension.back
import com.sola.anime.ai.generator.common.extension.getDeviceId
import com.sola.anime.ai.generator.common.extension.getStatusBarHeight
import com.sola.anime.ai.generator.common.extension.show
import com.sola.anime.ai.generator.common.extension.toChildHistory
import com.sola.anime.ai.generator.common.ui.sheet.download.DownloadSheet
import com.sola.anime.ai.generator.common.util.AESEncyption
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.databinding.ActivityAvatarProcessingBinding
import com.sola.anime.ai.generator.domain.manager.AnalyticManager
import com.sola.anime.ai.generator.domain.model.status.DezgoStatusImageToImage
import com.sola.anime.ai.generator.domain.model.status.GenerateImagesToImagesProgress
import com.sola.anime.ai.generator.domain.model.status.StatusBodyImageToImage
import com.sola.anime.ai.generator.domain.repo.DezgoApiRepository
import com.sola.anime.ai.generator.domain.repo.HistoryRepository
import com.sola.anime.ai.generator.feature.processing.avatar.adapter.PreviewAdapter
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

@SuppressLint("SimpleDateFormat")
@AndroidEntryPoint
class AvatarProcessingActivity : LsActivity<ActivityAvatarProcessingBinding>(ActivityAvatarProcessingBinding::inflate) {

    @Inject lateinit var previewAdapter: PreviewAdapter
    @Inject lateinit var configApp: ConfigApp
    @Inject lateinit var analyticManager: AnalyticManager
    @Inject lateinit var dezgoApiRepo: DezgoApiRepository
    @Inject lateinit var historyRepo: HistoryRepository
    @Inject lateinit var prefs: Preferences

    private val creditsPerImage by lazy { intent.getFloatExtra("creditsPerImage", 10f) }
    private var dezgoStatusImagesToImages = listOf<DezgoStatusImageToImage>()
    private var isSuccessAll = false
    private val downloadSheet by lazy { DownloadSheet() }
    private val newPrompt by lazy { SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(Date()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transparent()
        lightStatusBar()
        setContentView(binding.root)

        analyticManager.logEvent(AnalyticManager.TYPE.GENERATE_PROCESSING_AVATAR)

        initView()
        initData()
        initObservable()
        listenerView()
    }

    private fun initObservable() {
        previewAdapter
            .downloadClicks
            .autoDispose(scope())
            .subscribe { dezgoStatus ->
                when (val status = dezgoStatus.status) {
                    is StatusBodyImageToImage.Success -> {
                        val file = status.file

                        if (!file.exists()){
                            return@subscribe
                        }

                        val body = dezgoStatus.body

                        downloadSheet.file = file
                        downloadSheet.ratio = "${body.width}:${body.height}"
                        downloadSheet.style = "No Style"
                        downloadSheet.show(this)
                    }

                    else -> {}
                }
            }
    }

    private fun initData() {
        when {
            configApp.dezgoBodiesImagesToImages.isEmpty() -> {
                analyticManager.logEvent(AnalyticManager.TYPE.GENERATE_FAILED_AVATAR)

                makeToast("Server error, please wait for us to fix the error or try again!")
                finish()
                return
            }
        }

        generate()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun generate() {
        tryOrNull {
            lifecycleScope.launch {
                val deferredHistoryIds = arrayListOf<Long?>()

//                val decryptKey = AESEncyption.decrypt(Constraint.Dezgo.KEY_PREMIUM) ?: ""

//                val subNegativeDevice = "${getDeviceId()}_${BuildConfig.VERSION_CODE}"
//                val subFeature = "avatar"
//                val subPremiumAndCredits = "${prefs.isUpgraded.get()}_${prefs.getCredits().roundToInt()}"
//                val subNumberCreatedAndMax = "${prefs.numberCreatedArtwork.get() + 1}_${if (prefs.isUpgraded.get()) configApp.maxNumberGeneratePremium else configApp.maxNumberGenerateFree}"
//                val subNegative = "($subNegativeDevice)_${subFeature}_($subPremiumAndCredits)_($subNumberCreatedAndMax)"

                dezgoApiRepo.generateImagesToImages(
                    keyApi = AESEncyption.decrypt(configApp.keyDezgoPremium) ?: "",
                    subNegative = "subNegative",
                    datas = ArrayList(configApp.dezgoBodiesImagesToImages),
                    progress = { progress ->
                        when (progress){
                            GenerateImagesToImagesProgress.Idle -> Timber.e("IDLE")
                            GenerateImagesToImagesProgress.Loading -> {
                                dezgoStatusImagesToImages = configApp
                                    .dezgoBodiesImagesToImages
                                    .flatMap { dezgo ->
                                        dezgo
                                            .bodies
                                            .map { body ->
                                                DezgoStatusImageToImage(
                                                    body = body,
                                                    status = StatusBodyImageToImage.Loading
                                                )
                                            }
                                    }

                                previewAdapter.data = dezgoStatusImagesToImages
                            }
                            is GenerateImagesToImagesProgress.LoadingWithId -> {
                                Timber.e("LOADING WITH ID: ${progress.groupId} --- ${progress.childId}")

                                markLoadingWithIdAndChildId(groupId = progress.groupId, childId = progress.childId)
                            }
                            is GenerateImagesToImagesProgress.SuccessWithId ->  {
                                prefs.setCredits(prefs.getCredits() - creditsPerImage)
                                Timber.e("SUCCESS WITH ID: ${progress.groupId} --- ${progress.childId}")

                                configApp
                                    .dezgoBodiesImagesToImages
                                    .find { dezgo ->
                                        dezgo.id == progress.groupId
                                    }?.bodies
                                    ?.find { body ->
                                        body.id == progress.childId && body.groupId == progress.groupId
                                    }?.toChildHistory(newPrompt, progress.photoUri.toString(), progress.file.path)?.let {
                                        deferredHistoryIds.add(historyRepo.markHistory(it))
                                    }

                                markSuccessWithIdAndChildId(groupId = progress.groupId, childId = progress.childId, file = progress.file)
                            }
                            is GenerateImagesToImagesProgress.FailureWithId ->  {
                                Timber.e("FAILURE WITH ID: ${progress.groupId} --- ${progress.childId}")

                                markFailureWithIdAndChildId(groupId = progress.groupId, childId = progress.childId)
                            }
                            is GenerateImagesToImagesProgress.Done ->  {
                                isSuccessAll = true

                                Timber.e("DONE")

                                launch(Dispatchers.Main) {
                                    previewAdapter.notifyDataSetChanged()
                                }
                            }
                        }

                    }
                )
            }
        } ?: run {
            makeToast("Server error, please wait for us to fix the error or try again!")
            finish()
        }
    }

    private fun markLoadingWithIdAndChildId(groupId: Long, childId: Long) {
        dezgoStatusImagesToImages
            .find { status ->
                status.body.id == childId && status.body.groupId == groupId
            }?.status = StatusBodyImageToImage.Loading

        dezgoStatusImagesToImages
            .indexOfFirst { status ->
                status.body.id == childId && status.body.groupId == groupId
            }.takeIf { it != -1 }?.let { index ->
                lifecycleScope.launch(Dispatchers.Main) {
                    previewAdapter.notifyItemChanged(index)
                }
            }
    }

    private fun markSuccessWithIdAndChildId(groupId: Long, childId: Long, file: File) {
        dezgoStatusImagesToImages
            .find { status ->
                status.body.id == childId && status.body.groupId == groupId
            }?.status = StatusBodyImageToImage.Success(file)

        dezgoStatusImagesToImages
            .indexOfFirst { status ->
                status.body.id == childId && status.body.groupId == groupId
            }.takeIf { it != -1 }?.let { index ->
                lifecycleScope.launch(Dispatchers.Main) {
                    previewAdapter.notifyItemChanged(index)
                }
            }
    }

    private fun markFailureWithIdAndChildId(groupId: Long, childId: Long) {
        dezgoStatusImagesToImages
            .find { status ->
                status.body.id == childId && status.body.groupId == groupId
            }?.status = StatusBodyImageToImage.Failure()

        dezgoStatusImagesToImages
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