package com.sola.anime.ai.generator.feature.processing.art

import android.os.Bundle
import com.basic.common.base.LsActivity
import com.basic.common.extension.lightStatusBar
import com.basic.common.extension.makeToast
import com.basic.common.extension.transparent
import com.basic.common.extension.tryOrNull
import com.sola.anime.ai.generator.common.ConfigApp
import com.sola.anime.ai.generator.common.extension.setCurrentItem
import com.sola.anime.ai.generator.common.extension.startArtResult
import com.sola.anime.ai.generator.common.ui.dialog.ArtGenerateDialog
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.data.db.query.ArtProcessDao
import com.sola.anime.ai.generator.data.db.query.HistoryDao
import com.sola.anime.ai.generator.databinding.ActivityArtProcessingBinding
import com.sola.anime.ai.generator.domain.model.history.ChildHistory
import com.sola.anime.ai.generator.domain.model.history.History
import com.sola.anime.ai.generator.domain.model.status.DezgoStatusTextToImage
import com.sola.anime.ai.generator.domain.model.status.GenerateTextsToImagesProgress
import com.sola.anime.ai.generator.domain.model.status.StatusBodyTextToImage
import com.sola.anime.ai.generator.domain.repo.DezgoApiRepository
import com.sola.anime.ai.generator.domain.repo.HistoryRepository
import com.sola.anime.ai.generator.feature.processing.art.adapter.PreviewAdapter
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposables
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class ArtProcessingActivity : LsActivity() {

    @Inject lateinit var previewAdapter: PreviewAdapter
    @Inject lateinit var configApp: ConfigApp
    @Inject lateinit var artGenerateDialog: ArtGenerateDialog
    @Inject lateinit var artProcessDao: ArtProcessDao
    @Inject lateinit var dezgoApiRepo: DezgoApiRepository
    @Inject lateinit var historyRepo: HistoryRepository
    @Inject lateinit var historyDao: HistoryDao
//    @Inject lateinit var markHistories: MarkHistories

    private val binding by lazy { ActivityArtProcessingBinding.inflate(layoutInflater) }
    private var timeInterval = Disposables.empty()
    private var dezgoStatusTextsToImages = listOf<DezgoStatusTextToImage>()
    private var historyIds = listOf<Long>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transparent()
        lightStatusBar()
        setContentView(binding.root)

        initView()
        initObservable()
        initData()
    }

    override fun onResume() {
        startInterval()
        super.onResume()
    }

    override fun onStop() {
        stopInterval()
        super.onStop()
    }

    private fun startInterval(){
        timeInterval.dispose()
        timeInterval = Observable
            .interval(2, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribe { millisecond ->
                when {
                    millisecond >= Preferences.MAX_SECOND_GENERATE_ART -> {
                        makeToast("An error occurred. Please try again!")
                        artGenerateDialog.dismiss()
                        finish()
                    }
//                    millisecond >= 2 -> {
//                        artGenerateDialog.dismiss()
//                        startArtResult()
//                        finish()
//                    }
                    else -> {
                        tryOrNull { binding.viewPager.post { previewAdapter.insert() } }
                        tryOrNull { binding.viewPager.setCurrentItem(item = binding.viewPager.currentItem + 1) }
                    }
                }
            }
    }

    private fun stopInterval(){
        timeInterval.dispose()
    }

    private fun initData() {
        when {
            configApp.dezgoBodiesTextsToImages.isEmpty() -> {
                makeToast("An error occurred, please check again!")
                finish()
                return
            }
        }

        dezgoStatusTextsToImages = configApp
            .dezgoBodiesTextsToImages
            .flatMap { dezgo ->
                dezgo
                    .bodies
                    .map { body ->
                        DezgoStatusTextToImage(
                            id = body.groupId,
                            groupId = body.id,
                            status = StatusBodyTextToImage.Idle
                        )
                    }
            }

        artProcessDao
            .getAllLive()
            .observe(this){ artProcesses ->
                previewAdapter.apply {
                    this.data = artProcesses
                    this.totalCount = artProcesses.size
                }
        }

        CoroutineScope(Dispatchers.Main).launch {
            dezgoApiRepo.generateTextsToImages(ArrayList(configApp.dezgoBodiesTextsToImages))
        }
    }

    private fun initObservable() {
        dezgoApiRepo
            .progress()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribe { progress ->
                when (progress){
                    GenerateTextsToImagesProgress.Idle -> Timber.e("IDLE")
                    GenerateTextsToImagesProgress.Loading -> {
                        Timber.e("LOADING")

                        artGenerateDialog.show(this)
                    }
                    is GenerateTextsToImagesProgress.LoadingWithId -> {
                        Timber.e("LOADING WITH ID: ${progress.groupId} --- ${progress.childId}")

                        dezgoStatusTextsToImages
                            .find { status ->
                                status.id == progress.childId && status.groupId == progress.groupId
                            }?.status = StatusBodyTextToImage.Loading
                    }
                    is GenerateTextsToImagesProgress.SuccessWithId ->  {
                        Timber.e("SUCCESS WITH ID: ${progress.groupId} --- ${progress.childId}")

//                        historyIds = historyDao.inserts(History(title = "Fantasy", prompt = "ABC", pathDir = progress.file.parentFile?.path, pathPreview = progress.file.path))
                        CoroutineScope(Dispatchers.Main).launch {
                            historyIds = historyRepo.markHistories(ChildHistory())
                        }

                        dezgoStatusTextsToImages
                            .find { status ->
                                status.id == progress.childId && status.groupId == progress.groupId
                            }?.status = StatusBodyTextToImage.Success(progress.bitmap)
                    }
                    is GenerateTextsToImagesProgress.FailureWithId ->  {
                        Timber.e("FAILURE WITH ID: ${progress.groupId} --- ${progress.childId}")

                        dezgoStatusTextsToImages
                            .find { status ->
                                status.id == progress.childId && status.groupId == progress.groupId
                            }?.status = StatusBodyTextToImage.Failure()
                    }
                    is GenerateTextsToImagesProgress.Done ->  {
                        Timber.e("DONE")

                        artGenerateDialog.dismiss()

                        when {
                            dezgoStatusTextsToImages.any { it.status is StatusBodyTextToImage.Success } && historyIds.isNotEmpty() -> {
                                startArtResult(historyId = historyIds.firstOrNull() ?: -1)
                                finish()
                            }
                            else -> {
                                makeToast("An error occurred, please check again!")
                                finish()
                            }
                        }
                    }
                }
            }
    }

    private fun initView() {
        binding.viewPager.apply {
            this.isUserInputEnabled = false
            this.adapter = previewAdapter
        }
    }

}