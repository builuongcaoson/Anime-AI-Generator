package com.sola.anime.ai.generator.feature.pickAvatar

import android.net.Uri
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.basic.common.base.LsActivity
import com.basic.common.extension.clicks
import com.basic.common.extension.isNetworkAvailable
import com.basic.common.extension.tryOrNull
import com.sola.anime.ai.generator.common.ConfigApp
import com.sola.anime.ai.generator.common.Constraint
import com.sola.anime.ai.generator.common.extension.back
import com.sola.anime.ai.generator.common.extension.initDezgoBodyImagesToImages
import com.sola.anime.ai.generator.common.extension.initDezgoBodyTextsToImages
import com.sola.anime.ai.generator.common.extension.startBatchProcessing
import com.sola.anime.ai.generator.common.extension.startCredit
import com.sola.anime.ai.generator.common.ui.dialog.NetworkDialog
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.databinding.ActivityPickAvatarBinding
import com.sola.anime.ai.generator.domain.manager.AnalyticManager
import com.sola.anime.ai.generator.domain.model.PromptBatch
import com.sola.anime.ai.generator.domain.model.Ratio
import com.sola.anime.ai.generator.domain.model.Sampler
import com.sola.anime.ai.generator.domain.repo.DetectFaceRepository
import com.sola.anime.ai.generator.feature.pickAvatar.adapter.ObjectAdapter
import com.sola.anime.ai.generator.feature.pickAvatar.adapter.PhotoAdapter
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class PickAvatarActivity : LsActivity<ActivityPickAvatarBinding>(ActivityPickAvatarBinding::inflate) {

    companion object {
        private const val MAX_PHOTO_PICK = 5
        private const val MIN_PHOTO_PICK = 1
    }

    @Inject lateinit var objectAdapter: ObjectAdapter
    @Inject lateinit var photoAdapter: PhotoAdapter
    @Inject lateinit var detectFaceRepo: DetectFaceRepository
    @Inject lateinit var analyticManager: AnalyticManager
    @Inject lateinit var networkDialog: NetworkDialog
    @Inject lateinit var configApp: ConfigApp
    @Inject lateinit var prefs: Preferences

    private var urisHadFace: List<Uri> = emptyList()
        set(value) {
            field = value
            photoAdapter.data = value
        }
    private val pickPhotoLaunchers = (MAX_PHOTO_PICK downTo MIN_PHOTO_PICK).map {
        it to pickPhotoResult(limit = it) { urisHadNull ->
            val uris = urisHadNull.mapNotNull { uri -> uri }
            when {
                binding.switchDetectFace.isChecked -> detectFaceUris(uris)
                else -> this@PickAvatarActivity.urisHadFace = this@PickAvatarActivity.urisHadFace + urisHadFace
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initView()
        initObservable()
        initData()
        listenerView()
    }

    private fun detectFaceUris(uris: List<Uri>) {
        lifecycleScope.launch {
            launch(Dispatchers.Main){ binding.viewLoading.isVisible = true }

            val urisHadFace = detectFaceRepo.detectFaceUris(*uris.toTypedArray())

            launch(Dispatchers.Main){
                binding.viewLoading.isVisible = false

                this@PickAvatarActivity.urisHadFace = this@PickAvatarActivity.urisHadFace + urisHadFace
            }
        }
    }

    private fun listenerView() {
        binding.back.clicks { onBackPressed() }
        binding.viewCredit.clicks(withAnim = true) { startCredit() }
        binding.viewUpload.clicks(withAnim = true) {
            tryOrNull {
                pickPhotoLaunchers.find { pair -> pair.first == MAX_PHOTO_PICK - urisHadFace.size }?.second?.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
        }
        binding.cardGenerate.clicks { tryOrNull { generateClicks() } }
    }

    private fun generateClicks() {
        val task = {
            analyticManager.logEvent(AnalyticManager.TYPE.GENERATE_AVATAR_CLICKED)

            val dezgoBodies = photoAdapter.data.flatMapIndexed { index: Int, uri: Uri ->
                val prompt = objectAdapter.item?.prompt?.random() ?: "Beautiful"
                val negativePrompt = Constraint.Dezgo.DEFAULT_NEGATIVE

                initDezgoBodyImagesToImages(
                    groupId = index.toLong(),
                    maxChildId = 1,
                    initImage = uri,
                    prompt = prompt,
                    negativePrompt = negativePrompt,
                    guidance = "7.5",
                    steps = configApp.stepPremium,
                    model = configApp.modelBatchChoice?.model ?: Constraint.Dezgo.DEFAULT_MODEL,
                    sampler = listOf(Sampler.Ddim, Sampler.Dpm, Sampler.Euler, Sampler.EulerA).random().sampler,
                    upscale = "2",
                    styleId = -1,
                    ratio = Ratio.Ratio1x1,
                    seed = null,
                    type = 1
                )
            }

            configApp.dezgoBodiesImagesToImages = dezgoBodies

            startBatchProcessing()
        }

        when {
            !isNetworkAvailable() -> networkDialog.show(this) {
                networkDialog.dismiss()
            }
            configApp.discountCredit > prefs.getCredits().roundToInt() -> startCredit()
            else -> task()
        }
    }

    private fun initData() {

    }

    private fun initObservable() {
        objectAdapter
            .clicks
            .autoDispose(scope())
            .subscribe {

            }
    }

    private fun initView() {
        binding.recyclerObject.apply {
            this.adapter = objectAdapter.apply {
                this.data = Object.values().toList()
                this.item = Object.values().firstOrNull()
            }
        }
        binding.recyclerView.apply {
            this.adapter = photoAdapter
        }
    }

    @Deprecated("Deprecated in Java", ReplaceWith("finish()"))
    override fun onBackPressed() {
        back()
    }

    enum class Object(val display: String, val prompt: List<String>){
        Woman(
            display = "Woman",
            listOf(
                "((masterpiece))), (((best quality))), ((ultra-detailed)), Beautiful girl",
                "((masterpiece))), (((best quality))), ((ultra-detailed)), Beautiful girl",
            )
        ),
        Man(
            display = "Man",
            listOf(
                "((masterpiece))), (((best quality))), ((ultra-detailed)), Handsome boy",
                "((masterpiece))), (((best quality))), ((ultra-detailed)), Handsome man",
            )
        )
    }

    private fun AppCompatActivity.pickPhotoResult(
        limit: Int = 1,
        callBack: (List<Uri?>) -> Unit
    ): ActivityResultLauncher<PickVisualMediaRequest> {
        return when (limit) {
            1 -> {
                registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                    callBack(listOf(uri))
                }
            }
            else -> {
                registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(limit)) { uris ->
                    callBack(uris)
                }
            }
        }
    }

}