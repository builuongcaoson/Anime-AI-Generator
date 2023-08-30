package com.sola.anime.ai.generator.feature.pickAvatar

import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import com.basic.common.base.LsActivity
import com.basic.common.extension.*
import com.sola.anime.ai.generator.common.ConfigApp
import com.sola.anime.ai.generator.common.Constraint
import com.sola.anime.ai.generator.common.extension.*
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
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
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
            tryOrNull {
                field = value
                photoAdapter.data = value
                binding.recyclerView.isVisible = value.isNotEmpty()
                updateUiCredit()
            }
        }
    private val pickPhotoLaunchers = (MAX_PHOTO_PICK downTo MIN_PHOTO_PICK).map {
        it to pickPhotoResult(limit = it) { urisHadNull ->
            val uris = urisHadNull.mapNotNull { uri -> uri }
            when {
                binding.switchDetectFace.isChecked -> detectFaceUris(uris)
                else -> this@PickAvatarActivity.urisHadFace = this@PickAvatarActivity.urisHadFace + uris
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transparent()
        lightStatusBar()
        lightNavigationBar()
        setContentView(binding.root)

        initView()
        initObservable()
        initData()
        listenerView()
    }

    private fun updateUiCredit(){
        val creditForNumbersOfImages = photoAdapter.data.size * 40.0f
        val discount = 0.02

        Timber.e("DiscountCredits: ${(creditForNumbersOfImages - (creditForNumbersOfImages * discount))}")

        configApp.discountCreditAvatar = (creditForNumbersOfImages - (creditForNumbersOfImages * discount)).roundToInt()
        val totalCredit = creditForNumbersOfImages.roundToInt()

        binding.discountCredit.text = configApp.discountCreditAvatar.toString()
        binding.totalCredit.apply {
            text = totalCredit.toString()
            paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            isVisible = configApp.discountCreditAvatar != totalCredit
        }
        binding.timeGenerate.text = "About ${((photoAdapter.data.size * 4 / 10) + 1)} minute"
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
        binding.viewSwitchClicks.clicks { binding.switchDetectFace.setNewChecked(!binding.switchDetectFace.isChecked) }
    }

    private fun generateClicks() {
        val task = {
            lifecycleScope.launch(Dispatchers.Main) {
                binding.viewLoading.isVisible = true

                analyticManager.logEvent(AnalyticManager.TYPE.GENERATE_AVATAR_CLICKED)

                val urisCropCenter = withContext(Dispatchers.IO){
                    photoAdapter.data.mapNotNull { uri ->
                        uri.resizeAndCropImage(this@PickAvatarActivity)
                    }
                }

                val dezgoBodies = urisCropCenter.flatMapIndexed { index: Int, uri: Uri ->
                    val prompt = objectAdapter.item?.prompt?.random() ?: "Beautiful"
                    val negativePrompt = Constraint.Dezgo.DEFAULT_NEGATIVE
                    val strength = tryOrNull { binding.slider.currentValue } ?: Constraint.Dezgo.DEFAULT_STRENGTH_IMG_TO_IMG

                    initDezgoBodyImagesToImages(
                        context = this@PickAvatarActivity,
                        prefs = prefs,
                        configApp = configApp,
                        creditsPerImage = configApp.discountCreditBatch.toFloat() / (urisCropCenter.size * 4).toFloat(),
                        groupId = index.toLong(),
                        maxChildId = 3,
                        initImage = uri,
                        prompt = prompt,
                        negative = negativePrompt,
                        guidance = "7.5",
                        steps = configApp.stepPremium,
                        model = Constraint.Dezgo.DEFAULT_MODEL,
                        sampler = listOf(Sampler.Ddim, Sampler.Dpm, Sampler.Euler, Sampler.EulerA).random().sampler,
                        upscale = "2",
                        styleId = -1,
                        ratio = Ratio.Ratio1x1,
                        strength = strength.toString(),
                        seed = null,
                        type = 2
                    )
                }

                configApp.dezgoBodiesImagesToImages = dezgoBodies

                launch(Dispatchers.Main){
                    startAvatarProcessing()
                    finish()
                }
            }
        }

        when {
            photoAdapter.data.isEmpty() -> makeToast("Please choose 1-5 photos to perform the function!")
            !isNetworkAvailable() -> networkDialog.show(this) {
                networkDialog.dismiss()
            }
            configApp.discountCreditAvatar > prefs.getCredits().roundToInt() -> startCredit()
            else -> task()
        }
    }

    private fun initData() {

    }

    private fun initObservable() {
        objectAdapter
            .clicks
            .autoDispose(scope())
            .subscribe { itemObject ->
                objectAdapter.item = itemObject
            }

        photoAdapter
            .subjectDeleteClicks
            .autoDispose(scope())
            .subscribe { uri ->
                urisHadFace = ArrayList(urisHadFace).apply {
                    remove(uri)
                }
            }

        prefs
            .creditsChanges
            .asObservable()
            .map { prefs.getCredits() }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribe { credits ->
                binding.credits.text = credits.roundToInt().toString()
            }
    }

    private fun initView() {
        binding.viewTop.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            this.topMargin = when(val statusBarHeight = getStatusBarHeight()) {
                0 -> getDimens(com.intuit.sdp.R.dimen._30sdp).toInt()
                else -> statusBarHeight
            }
        }
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