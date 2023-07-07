package com.sola.anime.ai.generator.feature.processing.art

import android.animation.Animator
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.basic.common.base.LsActivity
import com.basic.common.extension.lightStatusBar
import com.basic.common.extension.makeToast
import com.basic.common.extension.transparent
import com.basic.common.extension.tryOrNull
import com.sola.anime.ai.generator.common.ConfigApp
import com.sola.anime.ai.generator.common.extension.back
import com.sola.anime.ai.generator.common.extension.setCurrentItem
import com.sola.anime.ai.generator.common.extension.startArtResult
import com.sola.anime.ai.generator.common.extension.toChildHistory
import com.sola.anime.ai.generator.common.ui.dialog.ArtGenerateDialog
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.data.db.query.ProcessDao
import com.sola.anime.ai.generator.data.db.query.HistoryDao
import com.sola.anime.ai.generator.databinding.ActivityArtProcessingBinding
import com.sola.anime.ai.generator.domain.manager.AnalyticManager
import com.sola.anime.ai.generator.domain.model.status.DezgoStatusImageToImage
import com.sola.anime.ai.generator.domain.model.status.DezgoStatusTextToImage
import com.sola.anime.ai.generator.domain.model.status.GenerateImagesToImagesProgress
import com.sola.anime.ai.generator.domain.model.status.GenerateTextsToImagesProgress
import com.sola.anime.ai.generator.domain.model.status.StatusBodyImageToImage
import com.sola.anime.ai.generator.domain.model.status.StatusBodyTextToImage
import com.sola.anime.ai.generator.domain.model.textToImage.DezgoBodyImageToImage
import com.sola.anime.ai.generator.domain.repo.DezgoApiRepository
import com.sola.anime.ai.generator.domain.repo.HistoryRepository
import com.sola.anime.ai.generator.feature.processing.art.adapter.PreviewAdapter
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposables
import kotlinx.coroutines.*
import timber.log.Timber
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class ArtProcessingActivity : LsActivity<ActivityArtProcessingBinding>(ActivityArtProcessingBinding::inflate) {

    @Inject lateinit var previewAdapter: PreviewAdapter
    @Inject lateinit var configApp: ConfigApp
    @Inject lateinit var artGenerateDialog: ArtGenerateDialog
    @Inject lateinit var artProcessDao: ProcessDao
    @Inject lateinit var dezgoApiRepo: DezgoApiRepository
    @Inject lateinit var historyRepo: HistoryRepository
    @Inject lateinit var historyDao: HistoryDao
    @Inject lateinit var analyticManager: AnalyticManager

    private var timeInterval = Disposables.empty()
    private var dezgoStatusTextsToImages = listOf<DezgoStatusTextToImage>()
    private var dezgoStatusImagesToImages = listOf<DezgoStatusImageToImage>()
    private var animator: Animator? = null

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

    override fun onDestroy() {
        tryOrNull { animator?.cancel() }
        super.onDestroy()
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
                        analyticManager.logEvent(AnalyticManager.TYPE.GENERATE_FAILED)

                        makeToast("Server error, please wait for us to fix the error or try again!")
                        tryOrNull { artGenerateDialog.dismiss() }
                        finish()
                    }
                    else -> {
                        tryOrNull { binding.viewPager.post { tryOrNull { previewAdapter.insert() } } }
                        tryOrNull { binding.viewPager.post { animator = tryOrNull { binding.viewPager.setCurrentItem(item = binding.viewPager.currentItem + 1) } } }
                    }
                }
            }
    }

    private fun stopInterval(){
        timeInterval.dispose()
    }

    private fun initData() {
        when {
            configApp.dezgoBodiesTextsToImages.isNotEmpty() -> {
                dezgoStatusTextsToImages = configApp
                    .dezgoBodiesTextsToImages
                    .flatMap { dezgo ->
                        dezgo
                            .bodies
                            .map { body ->
                                DezgoStatusTextToImage(
                                    body = body,
                                    status = StatusBodyTextToImage.Loading
                                )
                            }
                    }

                tryOrNull {
                    lifecycleScope.launch {
                        val deferredHistoryIds = arrayListOf<Long?>()

                        dezgoApiRepo.generateTextsToImages(
                            datas = ArrayList(configApp.dezgoBodiesTextsToImages),
                            progress = { progress ->
                                when (progress){
                                    GenerateTextsToImagesProgress.Idle -> Timber.e("IDLE")
                                    GenerateTextsToImagesProgress.Loading -> {
                                        Timber.e("LOADING")

                                        launch(Dispatchers.Main) {
                                            artGenerateDialog.show(this@ArtProcessingActivity)
                                        }
                                    }
                                    is GenerateTextsToImagesProgress.LoadingWithId -> {
                                        Timber.e("LOADING WITH ID: ${progress.groupId} --- ${progress.childId}")

                                        markLoadingWithIdAndChildId(groupId = progress.groupId, childId = progress.childId)
                                    }
                                    is GenerateTextsToImagesProgress.SuccessWithId ->  {
                                        Timber.e("SUCCESS WITH ID: ${progress.groupId} --- ${progress.childId}")

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
                                    is GenerateTextsToImagesProgress.FailureWithId ->  {
                                        Timber.e("FAILURE WITH ID: ${progress.groupId} --- ${progress.childId}")

                                        markFailureWithIdAndChildId(groupId = progress.groupId, childId = progress.childId)
                                    }
                                    is GenerateTextsToImagesProgress.Done ->  {
                                        Timber.e("DONE")

                                        launch {
                                            val historyIds = deferredHistoryIds.mapNotNull { it }

                                            launch(Dispatchers.Main) {
                                                tryOrNull { animator?.cancel() }
                                                tryOrNull { artGenerateDialog.dismiss() }

                                                when {
                                                    dezgoStatusTextsToImages.none { it.status !is StatusBodyTextToImage.Success } && historyIds.isNotEmpty() -> {
                                                        startArtResult(historyId = historyIds.firstOrNull() ?: -1L, isGallery = false)
                                                        finish()
                                                    }
                                                    else -> {
                                                        analyticManager.logEvent(AnalyticManager.TYPE.GENERATE_FAILED)

                                                        makeToast("Server error, please wait for us to fix the error or try again!")
                                                        finish()
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                            }
                        )
                    }
                } ?: {
                    makeToast("Server error, please wait for us to fix the error or try again!")
                    finish()
                }
            }
            configApp.dezgoBodiesImagesToImages.isNotEmpty() -> {
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

                tryOrNull {
                    lifecycleScope.launch {
                        val deferredHistoryIds = arrayListOf<Long?>()

                        dezgoApiRepo.generateImagesToImages(
                            datas = ArrayList(configApp.dezgoBodiesImagesToImages),
                            progress = { progress ->
                                when (progress){
                                    GenerateImagesToImagesProgress.Idle -> Timber.e("IDLE")
                                    GenerateImagesToImagesProgress.Loading -> {
                                        Timber.e("LOADING")

                                        launch(Dispatchers.Main) {
                                            artGenerateDialog.show(this@ArtProcessingActivity)
                                        }
                                    }
                                    is GenerateImagesToImagesProgress.LoadingWithId -> {
                                        Timber.e("LOADING WITH ID: ${progress.groupId} --- ${progress.childId}")

                                        markLoadingWithIdAndChildId(groupId = progress.groupId, childId = progress.childId)
                                    }
                                    is GenerateImagesToImagesProgress.SuccessWithId ->  {
                                        Timber.e("SUCCESS WITH ID: ${progress.groupId} --- ${progress.childId}")

                                        when {
                                            dezgoStatusTextsToImages.isNotEmpty() -> {
                                                configApp
                                                    .dezgoBodiesTextsToImages
                                                    .find { dezgo ->
                                                        dezgo.id == progress.groupId
                                                    }?.bodies
                                                    ?.find { body ->
                                                        body.id == progress.childId && body.groupId == progress.groupId
                                                    }?.toChildHistory(progress.photoUri.toString(), progress.file.path)?.let {
                                                        deferredHistoryIds.add(historyRepo.markHistory(it))
                                                    }
                                            }
                                            dezgoStatusImagesToImages.isNotEmpty() -> {
                                                configApp
                                                    .dezgoBodiesImagesToImages
                                                    .find { dezgo ->
                                                        dezgo.id == progress.groupId
                                                    }?.bodies
                                                    ?.find { body ->
                                                        body.id == progress.childId && body.groupId == progress.groupId
                                                    }?.toChildHistory(progress.file.path)?.let {
                                                        deferredHistoryIds.add(historyRepo.markHistory(it))
                                                    }
                                            }
                                        }

                                        markSuccessWithIdAndChildId(groupId = progress.groupId, childId = progress.childId, file = progress.file)
                                    }
                                    is GenerateImagesToImagesProgress.FailureWithId ->  {
                                        Timber.e("FAILURE WITH ID: ${progress.groupId} --- ${progress.childId}")

                                        markFailureWithIdAndChildId(groupId = progress.groupId, childId = progress.childId)
                                    }
                                    is GenerateImagesToImagesProgress.Done ->  {
                                        Timber.e("DONE")

                                        launch {
                                            val historyIds = deferredHistoryIds.mapNotNull { it }

                                            launch(Dispatchers.Main) {
                                                tryOrNull { animator?.cancel() }
                                                tryOrNull { artGenerateDialog.dismiss() }

                                                when {
                                                    dezgoStatusImagesToImages.none { it.status !is StatusBodyImageToImage.Success } && historyIds.isNotEmpty() -> {
                                                        startArtResult(historyId = historyIds.firstOrNull() ?: -1L, isGallery = false)
                                                        finish()
                                                    }
                                                    else -> {
                                                        analyticManager.logEvent(AnalyticManager.TYPE.GENERATE_FAILED)

                                                        makeToast("Server error, please wait for us to fix the error or try again!")
                                                        finish()
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                            }
                        )
                    }
                } ?: {
                    makeToast("Server error, please wait for us to fix the error or try again!")
                    finish()
                }
            }
            else -> {
                analyticManager.logEvent(AnalyticManager.TYPE.GENERATE_FAILED)

                makeToast("Server error, please wait for us to fix the error or try again!")
                finish()
                return
            }
        }

        artProcessDao
            .getAllLive()
            .observe(this){ artProcesses ->
                previewAdapter.apply {
                    this.data = artProcesses.shuffled()
                    this.totalCount = artProcesses.size
                }
        }
    }

    private fun markLoadingWithIdAndChildId(groupId: Long, childId: Long) {
        when {
            dezgoStatusTextsToImages.isNotEmpty() -> {
                dezgoStatusTextsToImages
                    .find { status ->
                        status.body.id == childId && status.body.groupId == groupId
                    }?.status = StatusBodyTextToImage.Loading
            }
            dezgoStatusImagesToImages.isNotEmpty() -> {
                dezgoStatusImagesToImages
                    .find { status ->
                        status.body.id == childId && status.body.groupId == groupId
                    }?.status = StatusBodyImageToImage.Loading
            }
        }
    }

    private fun markSuccessWithIdAndChildId(groupId: Long, childId: Long, file: File) {
        when {
            dezgoStatusTextsToImages.isNotEmpty() -> {
                dezgoStatusTextsToImages
                    .find { status ->
                        status.body.id == childId && status.body.groupId == groupId
                    }?.status = StatusBodyTextToImage.Success(file)
            }
            dezgoStatusImagesToImages.isNotEmpty() -> {
                dezgoStatusImagesToImages
                    .find { status ->
                        status.body.id == childId && status.body.groupId == groupId
                    }?.status = StatusBodyImageToImage.Success(file)
            }
        }
    }

    private fun markFailureWithIdAndChildId(groupId: Long, childId: Long) {
        when {
            dezgoStatusTextsToImages.isNotEmpty() -> {
                dezgoStatusTextsToImages
                    .find { status ->
                        status.body.id == childId && status.body.groupId == groupId
                    }?.status = StatusBodyTextToImage.Failure()
            }
            dezgoStatusImagesToImages.isNotEmpty() -> {
                dezgoStatusImagesToImages
                    .find { status ->
                        status.body.id == childId && status.body.groupId == groupId
                    }?.status = StatusBodyImageToImage.Failure()
            }
        }
    }

    private fun initObservable() {

    }

    private fun initView() {
        binding.viewPager.apply {
            this.isUserInputEnabled = false
            this.adapter = previewAdapter
        }
    }

    @Deprecated("Deprecated in Java", ReplaceWith("finish()"))
    override fun onBackPressed() {
        back()
    }

}