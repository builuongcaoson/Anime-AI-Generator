package com.sola.anime.ai.generator.feature.art.art

import android.annotation.SuppressLint
import android.graphics.Paint
import android.os.Build
import android.view.MotionEvent
import android.view.View
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.basic.common.base.LsFragment
import com.basic.common.extension.*
import com.jakewharton.rxbinding2.widget.textChanges
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.common.ConfigApp
import com.sola.anime.ai.generator.common.Constraint
import com.sola.anime.ai.generator.common.extension.*
import com.sola.anime.ai.generator.common.ui.dialog.BlockSensitivesDialog
import com.sola.anime.ai.generator.common.ui.dialog.ExploreDialog
import com.sola.anime.ai.generator.common.ui.dialog.NetworkDialog
import com.sola.anime.ai.generator.common.ui.sheet.advanced.SheetAdvanced
import com.sola.anime.ai.generator.common.ui.sheet.explore.SheetExplore
import com.sola.anime.ai.generator.common.ui.sheet.history.SheetHistory
import com.sola.anime.ai.generator.common.ui.sheet.loRA.SheetLoRA
import com.sola.anime.ai.generator.common.ui.sheet.model.SheetModel
import com.sola.anime.ai.generator.common.ui.sheet.photo.SheetPhoto
import com.sola.anime.ai.generator.common.ui.sheet.style.SheetStyle
import com.sola.anime.ai.generator.common.widget.quantitizer.QuantitizerListener
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.data.db.query.ExploreDao
import com.sola.anime.ai.generator.data.db.query.LoRAGroupDao
import com.sola.anime.ai.generator.data.db.query.ModelDao
import com.sola.anime.ai.generator.data.db.query.StyleDao
import com.sola.anime.ai.generator.databinding.FragmentArtBinding
import com.sola.anime.ai.generator.domain.manager.AdmobManager
import com.sola.anime.ai.generator.domain.manager.AnalyticManager
import com.sola.anime.ai.generator.domain.model.LoRAPreview
import com.sola.anime.ai.generator.domain.model.Ratio
import com.sola.anime.ai.generator.domain.model.config.explore.Explore
import com.sola.anime.ai.generator.domain.model.config.model.Model
import com.sola.anime.ai.generator.domain.model.config.style.Style
import com.sola.anime.ai.generator.domain.model.history.LoRAHistory
import com.sola.anime.ai.generator.domain.repo.DezgoApiRepository
import com.sola.anime.ai.generator.feature.art.ArtActivity
import com.sola.anime.ai.generator.feature.art.art.adapter.AspectRatioAdapter
import com.sola.anime.ai.generator.feature.art.art.adapter.LoRAAdapter
import com.sola.anime.ai.generator.feature.art.art.adapter.ExploreAdapter
import com.trello.rxlifecycle2.kotlin.bindToLifecycle
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class ArtFragment : LsFragment<FragmentArtBinding>(FragmentArtBinding::inflate) {

    @Inject lateinit var aspectRatioAdapter: AspectRatioAdapter
    @Inject lateinit var configApp: ConfigApp
    @Inject lateinit var styleDao: StyleDao
    @Inject lateinit var exploreDao: ExploreDao
    @Inject lateinit var dezgoApiRepo: DezgoApiRepository
    @Inject lateinit var exploreDialog: ExploreDialog
    @Inject lateinit var prefs: Preferences
    @Inject lateinit var admobManager: AdmobManager
    @Inject lateinit var blockSensitivesDialog: BlockSensitivesDialog
    @Inject lateinit var networkDialog: NetworkDialog
    @Inject lateinit var analyticManager: AnalyticManager
    @Inject lateinit var exploreAdapter: ExploreAdapter
    @Inject lateinit var modelDao: ModelDao
    @Inject lateinit var loRAAdapter: LoRAAdapter
    @Inject lateinit var loRAGroupDao: LoRAGroupDao

    private val subjectRatioClicks: Subject<Ratio> = BehaviorSubject.createDefault(Ratio.Ratio1x1)
    private val useExploreClicks: Subject<Explore> = PublishSubject.create()
    private val detailExploreClicks: Subject<Explore> = PublishSubject.create()
    private val subjectExploreChanges: Subject<List<Explore>> = PublishSubject.create()

    private val sheetHistory by lazy { SheetHistory() }
    private val sheetAdvanced by lazy { SheetAdvanced() }
    private val sheetPhoto by lazy { SheetPhoto() }
    private val sheetModel by lazy { SheetModel() }
    private val sheetStyle by lazy { SheetStyle() }
    private val sheetLoRA by lazy { SheetLoRA() }
    private val sheetExplore by lazy { SheetExplore() }

    var modelId = -1L
    var exploreId = -1L
    var loRAGroupId = -1L
    var loRAId = -1L

    private var totalCreditsDeducted = 10f
    private var creditsPerImage = 10f

    override fun onViewCreated() {
        lifecycleScope.launch {
            delay(250L)

            initView()
            initObservable()
            initData()
            listenerView()
        }
    }

    private fun initData() {
        Timber.e("Model id: $modelId")
        Timber.e("LoRA Group id: $loRAGroupId")
        Timber.e("LoRA id: $loRAId")
        Timber.e("Explore id: $exploreId")

        modelDao.getAllDislikeLive().observeAndRemoveWhenNotEmpty(viewLifecycleOwner){ models ->
            val model = when {
                modelId != -1L -> models.find { model -> model.id == modelId } ?: models.firstOrNull()
                else -> models.find { model -> model.modelId == Constraint.Dezgo.DEFAULT_MODEL } ?: models.firstOrNull()
            }
            sheetModel.model = model
            updateUiModel(model)
        }

        modelDao.getAllLive().observe(viewLifecycleOwner){ models ->
            val pairModelsFavourite = "Favourite" to models.filter { it.isFavourite }
            val pairModelsOther = "Other" to models.filter { !it.isFavourite && !it.isDislike }
            val pairModelsDislike = "Dislike" to models.filter { it.isDislike }

            sheetModel.pairs = listOf(pairModelsFavourite, pairModelsOther, pairModelsDislike)
        }

        styleDao.getAllLive().observeAndRemoveWhenNotEmpty(viewLifecycleOwner) { styles ->
            val style = styles.find { style -> style.display == "No Style" } ?: styles.firstOrNull()
            sheetStyle.style = style
            updateUiStyle(style)
        }

        styleDao.getAllLive().observe(viewLifecycleOwner) { styles ->
            sheetStyle.pairs = listOf("" to styles)
        }

        exploreDao.getAllLive().observeAndRemoveWhenNotEmpty(viewLifecycleOwner) { explores ->
            val explore = when {
                exploreId != -1L -> explores.find { explore -> explore.id == exploreId }
                else -> null
            }
            updateUiExplore(explore)
        }

        exploreDao.getAllLive().observe(viewLifecycleOwner) { explores ->
            val pairExploresFavourite = "Favourite" to explores.filter { it.isFavourite }
            val pairExploresOther = "Other" to explores.filter { !it.isFavourite && !it.isDislike }
            val pairExploresDislike = "Dislike" to explores.filter { it.isDislike }

            subjectExploreChanges.onNext(explores.filter { !it.isDislike })
            sheetExplore.pairs = listOf(pairExploresFavourite, pairExploresOther, pairExploresDislike)
        }

        loRAGroupDao.getAllLive().observeAndRemoveWhenNotEmpty(viewLifecycleOwner) { loRAGroups ->
            val loRA = when {
                loRAGroupId != -1L && loRAId != -1L -> loRAGroups.find { loRAGroup -> loRAGroup.id == loRAGroupId }?.childs?.find { loRA -> loRA.id == loRAId }
                else -> null
            }
            loRA?.let {
                loRAAdapter.data = listOf(LoRAPreview(loRA = loRA, loRAGroupId = loRAGroupId, strength = 0.7f))

                sheetLoRA.loRAs = loRAAdapter.data

                updateUiCredit()
            }
        }

        loRAGroupDao.getAllLive().observe(viewLifecycleOwner){ loRAGroups ->
            val loRAs = loRAGroups.flatMap { loRAGroup -> loRAGroup.childs.map { loRAPreview -> LoRAPreview(loRA = loRAPreview, loRAGroupId = loRAGroup.id, strength = 0.7f) } }

            val pairLoRAsFavourite = "Favourite" to loRAs.filter { loRAPreview -> loRAPreview.loRA.isFavourite }
            val pairLoRAsOther = "Other" to loRAs.filter { loRAPreview -> !loRAPreview.loRA.isFavourite && !loRAPreview.loRA.isDislike }
            val pairLoRAsDislike = "Dislike" to loRAs.filter { loRAPreview -> loRAPreview.loRA.isDislike }

            sheetLoRA.pairs = listOf(pairLoRAsFavourite, pairLoRAsOther, pairLoRAsDislike)
        }
    }

    private fun updateUiModel(model: Model?) {
        binding.displayModel.animate().alpha(1f).setDuration(250L).start()

        binding.displayModel.text = when (model) {
            null -> "Pick a Model"
            else -> model.display
        }
    }

    @SuppressLint("AutoDispose", "CheckResult")
    private fun initObservable() {
        subjectExploreChanges
            .autoDispose(scope())
            .subscribe { explores ->
                lifecycleScope.launch(Dispatchers.Main) {
                    exploreAdapter.data = explores.filter { !it.isDislike }
                    delay(250L)
                    binding.recyclerViewExplore.animate().alpha(1f).setDuration(250L).start()
                }
            }

        loRAAdapter
            .clicks
            .bindToLifecycle(binding.root)
            .subscribe { loRAPreview ->
                activity?.startDetailModelOrLoRA(loRAGroupId = loRAPreview.loRAGroupId, loRAId = loRAPreview.loRA.id)
            }

        loRAAdapter
            .deleteClicks
            .bindToLifecycle(binding.root)
            .subscribe { loRAIndex ->
                tryOrNull {
                    loRAAdapter.data = ArrayList(loRAAdapter.data).apply {
                        this.removeAt(loRAIndex)
                    }
                }

                sheetLoRA.loRAs = loRAAdapter.data
            }

        sheetPhoto
            .clicks
            .bindToLifecycle(binding.root)
            .subscribe { photo ->
                when {
                    photo.preview != null -> configApp.resPhoto = photo.preview
                    photo.photoStorage != null -> configApp.pairUriPhoto = tryOrNull { photo.photoStorage.uriString.toUri() to photo.photoStorage.ratio }
                }
            }

        configApp
            .subjectUriPhotoChanges
            .bindToLifecycle(binding.root)
            .subscribe {
                binding.viewPhoto.isVisible = configApp.pairUriPhoto != null || configApp.resPhoto != null

                when {
                    configApp.pairUriPhoto != null -> {
                        val pair = configApp.pairUriPhoto ?: return@subscribe
                        binding.previewPhoto.load(pair.first, errorRes = R.drawable.place_holder_image)
                        subjectRatioClicks.onNext(pair.second)
                    }
                    configApp.resPhoto != null -> {
                        val res = configApp.resPhoto ?: return@subscribe
                        binding.previewPhoto.load(res, errorRes = R.drawable.place_holder_image)
                        subjectRatioClicks.onNext(Ratio.Ratio1x1)
                    }
                }
            }

        exploreAdapter
            .clicks
            .bindToLifecycle(binding.root)
            .subscribe { explore ->
                activity?.let { activity -> exploreDialog.show(activity, explore, useExploreClicks, detailExploreClicks) }
            }

        Observable
            .timer(2000L, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
            .bindToLifecycle(binding.root)
            .subscribe {
                binding.recyclerViewExplore.animate().alpha(1f).setDuration(500).start()
            }

        aspectRatioAdapter
            .clicks
            .bindToLifecycle(binding.root)
            .subscribe { ratio ->
                when {
                    ratio == Ratio.Ratio1x1 || ratio == Ratio.Ratio9x16 || ratio == Ratio.Ratio16x9 -> subjectRatioClicks.onNext(ratio)
                    !prefs.isUpgraded.get() -> activity?.startIap()
                    else -> subjectRatioClicks.onNext(ratio)
                }
            }

        subjectRatioClicks
            .bindToLifecycle(binding.root)
            .subscribe { ratio ->
                aspectRatioAdapter.ratio = ratio
            }

        binding
            .editPrompt
            .textChanges()
            .bindToLifecycle(binding.root)
            .subscribe { prompt ->
                binding.viewClear.isVisible = !prompt.isNullOrEmpty()
                binding.viewActionPrompt.isVisible = prompt.isNullOrEmpty()

                binding.count.text = "${prompt.length}/1000"
            }

        useExploreClicks
            .bindToLifecycle(binding.root)
            .subscribe { explore ->
                if (exploreDialog.isShowing()){
                    exploreDialog.dismiss()
                }

                binding.nestedScrollView.fullScroll(View.FOCUS_UP)

                updateUiExplore(explore)
            }

        detailExploreClicks
            .bindToLifecycle(binding.root)
            .subscribe { explore ->
                if (sheetExplore.isAdded){
                    sheetExplore.dismiss()
                }
                exploreDialog.dismiss()

                activity?.startDetailExplore(exploreId = explore.id)
            }

        Observable.zip(
            prefs.isUpgraded.asObservable(),
            prefs.numberCreatedArtwork.asObservable()
        ){ _, _ -> }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
            .bindToLifecycle(binding.root)
            .subscribe { updateUiCredit() }
    }

    private fun updateUiExplore(explore: Explore?) {
        when {
            prefs.isUpgraded.get() -> {
                val findRatio = Ratio.values().find { it.ratio == explore?.ratio } ?: Ratio.Ratio1x1
                subjectRatioClicks.onNext(findRatio)
            }
        }

        explore?.let {
            binding.editPrompt.setText(explore.prompt)

            val model = modelDao.getAll().find { model -> explore.modelIds.any { id -> id == model.id } }
            updateUiModel(model)
        }
    }

    private fun updateUiStyle(style: Style?){
        binding.displayStyle.animate().alpha(1f).setDuration(250L).start()

        binding.displayStyle.text = when (style) {
            null -> "Pick a Style"
            else -> style.display
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun listenerView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            binding.nestedScrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
                val viewShadowHeight = when {
                    (activity is ArtActivity) -> (activity as? ArtActivity)?.binding?.viewShadow?.height?.toFloat() ?: 0f
                    else -> 0f
                }
                val alpha = scrollY.toFloat() / viewShadowHeight

                (activity as? ArtActivity)?.binding?.viewShadow?.alpha = alpha
            }
        }
        binding.cardGenerate.clicks(withAnim = false) { generateClicks() }
        binding.cardGenerateCredit.clicks(withAnim = false) { generateCreditClicks() }
        binding.viewModel.clicks(withAnim = false) { modelClicks() }
        binding.viewStyle.clicks(withAnim = false) { styleClicks() }
        binding.viewLoRA.clicks(withAnim = false) { loRAClicks() }
        binding.clear.clicks { binding.editPrompt.setText("") }
        binding.history.clicks { sheetHistory.show(this) }
        binding.editPrompt.setOnTouchListener { view, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    view.parent.requestDisallowInterceptTouchEvent(true)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    view.parent.requestDisallowInterceptTouchEvent(false)
                }
            }
            false
        }
        binding.viewAdvancedSetting.clicks(withAnim = false) {
            sheetAdvanced.ratio = subjectRatioClicks.blockingFirst()
            sheetAdvanced.ratioClicks = { ratio ->
                subjectRatioClicks.onNext(ratio)
            }

            sheetAdvanced.step = if (prefs.isUpgraded.get()) configApp.stepPremium else configApp.stepDefault
            sheetAdvanced.show(this)
        }
        binding.viewSeeAllExplore.clicks {
            sheetExplore.clicks = { explore ->
                activity?.let { activity ->
                    exploreDialog.show(activity, explore, useExploreClicks, detailExploreClicks)
                }
            }
            sheetExplore.show(this)
        }
        binding.closePhoto.clicks {
            configApp.resPhoto = null
            configApp.pairUriPhoto = null

            tryOrNull { sheetPhoto.photoAdapter.photo = null }
        }
        binding.photo.clicks { sheetPhoto.show(this) }
        binding.random.clicks { binding.editPrompt.setText(tryOrNull { exploreDao.getAll().random().prompt } ?: listOf("Girl", "Boy").random()) }
        binding.quantitizer.setQuantitizerListener(object: QuantitizerListener {
            override fun onIncrease() {

            }

            override fun onDecrease() {

            }

            override fun onValueChanged(value: Int) {
                updateUiCredit()
            }
        })
    }

    private fun updateUiCredit() {
        val numberOfImages = binding.quantitizer.value

        val creditsForNumbersOfImages = when {
            prefs.isUpgraded.get() && prefs.numberCreatedArtwork.get() >= configApp.maxNumberGeneratePremium && numberOfImages == 1 -> 10 + (loRAAdapter.data.size * 2)
            else -> numberOfImages * (10 + loRAAdapter.data.size * 2)
        }

        totalCreditsDeducted = (creditsForNumbersOfImages - (creditsForNumbersOfImages * configApp.discountCredits))
        creditsPerImage = (totalCreditsDeducted / numberOfImages.toFloat())

        binding.discountCredit.text = totalCreditsDeducted.roundToInt().toString()
        binding.totalCredit.apply {
            text = creditsForNumbersOfImages.toString()
            paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            isVisible = totalCreditsDeducted.roundToInt() != creditsForNumbersOfImages
        }
        binding.timeGenerate.text = "About ${((numberOfImages / 10) + 1)} minute"

        binding.cardGenerate.isVisible = when {
            numberOfImages != 1 -> false
            loRAAdapter.data.isNotEmpty() -> false
            else -> true
        }
        binding.cardGenerateCredit.isVisible = !binding.cardGenerate.isVisible
        binding.iconWatchAd.isVisible = when {
            !prefs.isUpgraded.get() && prefs.numberCreatedArtwork.get() >= configApp.maxNumberGenerateFree -> false
            !prefs.isUpgraded.get() && prefs.numberCreatedArtwork.get() < configApp.maxNumberGenerateFree -> true
            else -> false
        }
        binding.textDescription.isVisible = when {
            !prefs.isUpgraded.get() && prefs.numberCreatedArtwork.get() >= configApp.maxNumberGenerateFree -> false
            !prefs.isUpgraded.get() && prefs.numberCreatedArtwork.get() < configApp.maxNumberGenerateFree -> true
            prefs.isUpgraded.get() && prefs.numberCreatedArtwork.get() >= configApp.maxNumberGeneratePremium -> true
            prefs.isUpgraded.get() && prefs.numberCreatedArtwork.get() < configApp.maxNumberGeneratePremium -> false
            else -> false
        }
        binding.textDescription.text = when {
            !prefs.isUpgraded.get() && prefs.numberCreatedArtwork.get() >= configApp.maxNumberGenerateFree -> "${creditsPerImage.roundToInt()} Credits"
            !prefs.isUpgraded.get() && prefs.numberCreatedArtwork.get() < configApp.maxNumberGenerateFree -> "Watch an Ad"
            prefs.isUpgraded.get() && prefs.numberCreatedArtwork.get() >= configApp.maxNumberGeneratePremium -> "${creditsPerImage.roundToInt()} Credits"
            prefs.isUpgraded.get() && prefs.numberCreatedArtwork.get() < configApp.maxNumberGeneratePremium -> ""
            else -> ""
        }

        lifecycleScope.launch(Dispatchers.Main) {
            delay(250L)

            binding.cardGenerate.animate().alpha(1f).setDuration(250L).start()
            binding.cardGenerateCredit.animate().alpha(1f).setDuration(250L).start()
        }
    }

    private fun loRAClicks() {
        sheetLoRA.loRAs = loRAAdapter.data
        sheetLoRA.clicks = { loRAPReview ->
            sheetLoRA.dismiss()

            val loRAPreviewIndex = loRAAdapter.data.indexOf(loRAPReview)
            val firstLoRAPreview = loRAAdapter.data.getOrNull(0)

            when {
                loRAPreviewIndex != -1 -> {
                    tryOrNull {
                        loRAAdapter.data = ArrayList(loRAAdapter.data).apply {
                            removeAt(loRAPreviewIndex)
                        }
                    }
                }
                loRAAdapter.data.isEmpty() -> loRAAdapter.data = listOf(loRAPReview)
                loRAAdapter.data.size == 1 && firstLoRAPreview == null -> loRAAdapter.data = listOf(loRAPReview)
                loRAAdapter.data.size == 1 && firstLoRAPreview != null -> loRAAdapter.data = listOf(firstLoRAPreview, loRAPReview)
            }

            sheetLoRA.loRAs = loRAAdapter.data

            updateUiCredit()
        }
        sheetLoRA.detailsClicks = { loRAPreview ->
            lifecycleScope.launch(Dispatchers.Main) {
                sheetLoRA.dismiss()
                delay(250L)
                activity?.startDetailModelOrLoRA(loRAGroupId = loRAPreview.loRAGroupId, loRAId = loRAPreview.loRA.id)
            }
        }
        sheetLoRA.show(this)
    }

    private fun styleClicks() {
        sheetStyle.clicks = { style ->
            sheetStyle.style = style
            sheetStyle.dismiss()

            updateUiStyle(style)
        }
        sheetStyle.show(this)
    }

    private fun modelClicks() {
        sheetModel.clicks = { model ->
            sheetModel.model = model
            sheetModel.dismiss()

            updateUiModel(model)
        }
        sheetModel.detailsClicks = { model ->
            lifecycleScope.launch(Dispatchers.Main) {
                sheetModel.dismiss()

                delay(250L)

                activity?.startDetailModelOrLoRA(modelId = model.id)
            }
        }
        sheetModel.show(this)
    }

    private fun generateCreditClicks() {
        val prompt = tryOrNull { binding.editPrompt.text?.trim()?.takeIf { it.isNotEmpty() }?.toString() } ?: tryOrNull { exploreDao.getAll().randomOrNull()?.prompt } ?: listOf("Girl", "Boy").random()

        analyticManager.logEvent(AnalyticManager.TYPE.GENERATE_CREDITS_CLICKED)

        val task = {
            activity?.let { activity ->
                val photoType = tryOrNull { sheetPhoto.photoAdapter.photo }
                val photoUri = when {
                    photoType != null && photoType is SheetPhoto.PhotoType.Photo && photoType.photoStorage != null -> {
                        photoType.photoStorage.uriString.toUri()
                    }
                    photoType != null && photoType is SheetPhoto.PhotoType.Photo && photoType.preview != null -> {
                        activity.getDrawableUri(photoType.preview)
                    }
                    else -> null
                }
                when {
                    photoUri != null -> {
                        val strength = tryOrNull { sheetPhoto.strength } ?: 0.5f

                        configApp.dezgoBodiesTextsToImages = emptyList()
                        configApp.dezgoBodiesImagesToImages = initDezgoBodyImagesToImages(
                            context = activity,
                            prefs = prefs,
                            configApp = configApp,
                            creditsPerImage = creditsPerImage,
                            groupId = 0,
                            maxChildId = binding.quantitizer.value - 1,
                            initImage = photoUri,
                            prompt = prompt,
                            negative = sheetAdvanced.negative.takeIf { it.isNotEmpty() }?.let { Constraint.Dezgo.DEFAULT_NEGATIVE + ", $it" } ?: Constraint.Dezgo.DEFAULT_NEGATIVE,
                            guidance = sheetAdvanced.guidance.toString(),
                            steps = sheetAdvanced.step,
                            model = tryOrNull { sheetModel.model?.modelId } ?: Constraint.Dezgo.DEFAULT_MODEL,
                            sampler = "dpmpp_2m_karras",
                            upscale = "2",
                            styleId = tryOrNull { sheetStyle.style?.id } ?: -1,
                            ratio = aspectRatioAdapter.ratio,
                            strength = strength.toString(),
                            seed = null,
                            loRAs = loRAAdapter.data.map { LoRAHistory(it.loRA.sha256, it.strength) },
                            type = 0
                        )
                    }
                    else -> {
                        configApp.dezgoBodiesTextsToImages = initDezgoBodyTextsToImages(
                            context = activity,
                            prefs = prefs,
                            configApp = configApp,
                            creditsPerImage = creditsPerImage,
                            groupId = 0,
                            maxChildId = binding.quantitizer.value - 1,
                            prompt = prompt,
                            negative = sheetAdvanced.negative.takeIf { it.isNotEmpty() }?.let { Constraint.Dezgo.DEFAULT_NEGATIVE + ", $it" } ?: Constraint.Dezgo.DEFAULT_NEGATIVE,
                            guidance = sheetAdvanced.guidance.toString(),
                            steps = sheetAdvanced.step,
                            model = tryOrNull { sheetModel.model?.modelId } ?: Constraint.Dezgo.DEFAULT_MODEL,
                            sampler = "dpmpp_2m_karras",
                            upscale = "2",
                            styleId = tryOrNull { sheetStyle.style?.id } ?: -1,
                            ratio = aspectRatioAdapter.ratio,
                            seed = null,
                            loRAs = loRAAdapter.data.map { LoRAHistory(it.loRA.sha256, it.strength) },
                            type = 0
                        )
                        configApp.dezgoBodiesImagesToImages = emptyList()
                    }
                }

                activity.startBatchProcessing(creditsPerImage = creditsPerImage)
            }
        }

        when {
            !isNetworkAvailable() -> activity?.let { activity -> networkDialog.show(activity) }
            totalCreditsDeducted >= prefs.getCredits() -> activity?.startCredit()
            else -> task()
        }
    }

    private fun generateClicks() {
        val prompt = tryOrNull { binding.editPrompt.text?.trim()?.takeIf { it.isNotEmpty() }?.toString() } ?: tryOrNull { exploreDao.getAll().randomOrNull()?.prompt } ?: listOf("Girl", "Boy").random()

        analyticManager.logEvent(AnalyticManager.TYPE.GENERATE_CLICKED)

        val task = {
            activity?.let { activity ->
                val photoType = tryOrNull { sheetPhoto.photoAdapter.photo }
                val photoUri = when {
                    photoType != null && photoType is SheetPhoto.PhotoType.Photo && photoType.photoStorage != null -> {
                        photoType.photoStorage.uriString.toUri()
                    }
                    photoType != null && photoType is SheetPhoto.PhotoType.Photo && photoType.preview != null -> {
                        activity.getDrawableUri(photoType.preview)
                    }
                    else -> null
                }
                when {
                    photoUri != null -> {
                        val strength = tryOrNull { sheetPhoto.strength } ?: 0.5f

                        configApp.dezgoBodiesTextsToImages = emptyList()
                        configApp.dezgoBodiesImagesToImages = initDezgoBodyImagesToImages(
                            context = activity,
                            prefs = prefs,
                            configApp = configApp,
                            creditsPerImage = 0f,
                            groupId = 0,
                            maxChildId = 0,
                            initImage = photoUri,
                            prompt = prompt,
                            negative = sheetAdvanced.negative.takeIf { it.isNotEmpty() }?.let { Constraint.Dezgo.DEFAULT_NEGATIVE + ", $it" } ?: Constraint.Dezgo.DEFAULT_NEGATIVE,
                            guidance = sheetAdvanced.guidance.toString(),
                            steps = sheetAdvanced.step,
                            model = tryOrNull { sheetModel.model?.modelId } ?: Constraint.Dezgo.DEFAULT_MODEL,
                            sampler = "dpmpp_2m_karras",
                            upscale = "2",
                            styleId = tryOrNull { sheetStyle.style?.id } ?: -1,
                            ratio = aspectRatioAdapter.ratio,
                            strength = strength.toString(),
                            seed = null,
                            loRAs = loRAAdapter.data.map { LoRAHistory(it.loRA.sha256, it.strength) },
                            type = 0
                        )
                    }
                    else -> {
                        configApp.dezgoBodiesTextsToImages = initDezgoBodyTextsToImages(
                            context = activity,
                            prefs = prefs,
                            configApp = configApp,
                            creditsPerImage = 0f,
                            groupId = 0,
                            maxChildId = 0,
                            prompt = prompt,
                            negative = sheetAdvanced.negative.takeIf { it.isNotEmpty() }?.let { Constraint.Dezgo.DEFAULT_NEGATIVE + ", $it" } ?: Constraint.Dezgo.DEFAULT_NEGATIVE,
                            guidance = sheetAdvanced.guidance.toString(),
                            steps = sheetAdvanced.step,
                            model = tryOrNull { sheetModel.model?.modelId } ?: Constraint.Dezgo.DEFAULT_MODEL,
                            sampler = "dpmpp_2m_karras",
                            upscale = "2",
                            styleId = tryOrNull { sheetStyle.style?.id } ?: -1,
                            ratio = aspectRatioAdapter.ratio,
                            seed = null,
                            loRAs = loRAAdapter.data.map { LoRAHistory(it.loRA.sha256, it.strength) },
                            type = 0
                        )
                        configApp.dezgoBodiesImagesToImages = emptyList()
                    }
                }

                activity.startArtProcessing(totalCreditsDeducted = totalCreditsDeducted, creditsPerImage = creditsPerImage)
            }
        }

        when {
            !isNetworkAvailable() -> activity?.let { activity -> networkDialog.show(activity) }
            !prefs.isUpgraded.get() && prefs.numberCreatedArtwork.get() >= configApp.maxNumberGenerateFree -> activity?.startIap()
            !prefs.isUpgraded.get() -> activity?.let { activity ->
                admobManager.showRewardCreate(
                    activity,
                    success = {
                        task()
                        admobManager.loadRewardCreate()
                    },
                    failed = {
                        activity.makeToast("Please watch all ads to perform the function!")
                        admobManager.loadRewardCreate()
                    }
                )
            }
            prefs.isUpgraded.get() && prefs.numberCreatedArtwork.get() >= configApp.maxNumberGeneratePremium -> {
                when {
                    totalCreditsDeducted >= prefs.getCredits() -> activity?.startCredit()
                    else -> task()
                }
            }
            else -> task()
        }
    }

    private fun initView() {
        activity?.let { activity ->
            lifecycleScope.launch(Dispatchers.Main) {
                binding.recyclerViewAspectRatio.apply {
                    this.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
                    this.adapter = aspectRatioAdapter
                }
                delay(100L)
                binding.recyclerViewAspectRatio.animate().alpha(1f).setDuration(250L).start()
            }

            binding.recyclerViewExplore.apply {
                this.adapter = exploreAdapter
            }

            binding.recyclerLoRA.apply {
                this.adapter = loRAAdapter
            }

            binding.editPrompt.disableEnter()
        }
    }

}