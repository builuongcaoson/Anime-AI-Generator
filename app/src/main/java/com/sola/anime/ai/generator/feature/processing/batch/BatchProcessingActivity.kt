package com.sola.anime.ai.generator.feature.processing.batch

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.basic.common.base.LsActivity
import com.basic.common.extension.clicks
import com.basic.common.extension.lightStatusBar
import com.basic.common.extension.makeToast
import com.basic.common.extension.transparent
import com.basic.common.extension.tryOrNull
import com.sola.anime.ai.generator.common.ConfigApp
import com.sola.anime.ai.generator.common.extension.startArtResult
import com.sola.anime.ai.generator.common.extension.toChildHistory
import com.sola.anime.ai.generator.databinding.ActivityBatchProcessingBinding
import com.sola.anime.ai.generator.domain.manager.AnalyticManager
import com.sola.anime.ai.generator.domain.model.status.DezgoStatusTextToImage
import com.sola.anime.ai.generator.domain.model.status.GenerateTextsToImagesProgress
import com.sola.anime.ai.generator.domain.model.status.StatusBodyTextToImage
import com.sola.anime.ai.generator.domain.repo.DezgoApiRepository
import com.sola.anime.ai.generator.domain.repo.HistoryRepository
import com.sola.anime.ai.generator.feature.processing.batch.adapter.PreviewAdapter
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

    private var dezgoStatusTextsToImages = listOf<DezgoStatusTextToImage>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transparent()
        lightStatusBar()
        setContentView(binding.root)

        initView()
        initData()
        listenerView()
    }

    private fun initData() {
        when {
            configApp.dezgoBodiesTextsToImages.isEmpty() -> {
                analyticManager.logEvent(AnalyticManager.TYPE.GENERATE_FAILED)

                makeToast("Server error, please wait for us to fix the error or try again!")
                finish()
                return
            }
        }

        lifecycleScope.launch {
            val deferredHistoryIds = arrayListOf<Long?>()

            dezgoApiRepo.generateTextsToImages(
                datas = ArrayList(configApp.dezgoBodiesTextsToImages),
                progress = { progress ->
                    when (progress){
                        GenerateTextsToImagesProgress.Idle -> Timber.e("IDLE")
                        GenerateTextsToImagesProgress.Loading -> {
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

                            previewAdapter.data = dezgoStatusTextsToImages
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
//                                    when {
//                                        dezgoStatusTextsToImages.none { it.status !is StatusBodyTextToImage.Success } && historyIds.isNotEmpty() -> {
//                                            startArtResult(historyId = historyIds.firstOrNull() ?: -1L, isGallery = false)
//                                            finish()
//                                        }
//                                        else -> {
//                                            analyticManager.logEvent(AnalyticManager.TYPE.GENERATE_FAILED)
//
//                                            makeToast("Server error, please wait for us to fix the error or try again!")
//                                            finish()
//                                        }
//                                    }
                                }
                            }
                        }
                    }

                }
            )
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
                previewAdapter.notifyItemChanged(index)
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
                previewAdapter.notifyItemChanged(index)
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
                previewAdapter.notifyItemChanged(index)
            }
    }

    private fun initView() {
        binding.recyclerView.apply {
            this.itemAnimator = null
            this.adapter = previewAdapter
        }
    }

    private fun listenerView() {
        binding.back.clicks { onBackPressed() }
    }

    @Deprecated("Deprecated in Java", ReplaceWith("finish()"))
    override fun onBackPressed() {
        finish()
    }

}