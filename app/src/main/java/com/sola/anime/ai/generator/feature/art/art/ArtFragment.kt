package com.sola.anime.ai.generator.feature.art.art

import android.annotation.SuppressLint
import android.os.Build
import android.view.MotionEvent
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
import com.sola.anime.ai.generator.domain.repo.DezgoApiRepository
import com.sola.anime.ai.generator.feature.art.ArtActivity
import com.sola.anime.ai.generator.feature.art.art.adapter.AspectRatioAdapter
import com.sola.anime.ai.generator.feature.art.art.adapter.LoRAAdapter
import com.sola.anime.ai.generator.feature.art.art.adapter.ExploreAdapter
import com.trello.rxlifecycle2.kotlin.bindToLifecycle
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

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

    private val sheetHistory by lazy { SheetHistory() }
    private val sheetAdvanced by lazy { SheetAdvanced() }
    private val sheetPhoto by lazy { SheetPhoto() }
    private val sheetModel by lazy { SheetModel() }
    private val sheetStyle by lazy { SheetStyle() }
    private val sheetLoRA by lazy { SheetLoRA() }
    private val sheetExplore by lazy { SheetExplore() }

    override fun onViewCreated() {
        initView()
        initObservable()
        initData()
        listenerView()
    }

    private fun initData() {
        exploreDao.getAllDislikeLive().observe(viewLifecycleOwner){ explores ->
            exploreAdapter.data = explores
        }

        modelDao.getAllDislikeLive().observeAndRemoveWhenNotEmpty(viewLifecycleOwner){ models ->
            val model = models.find { model -> model.modelId == Constraint.Dezgo.DEFAULT_MODEL } ?: models.firstOrNull()
            sheetModel.model = model
            updateUiModel(model)
        }

        styleDao.getAllLive().observeAndRemoveWhenNotEmpty(viewLifecycleOwner) { styles ->
            val style = styles.find { style -> style.display == "No Style" } ?: styles.firstOrNull()
            sheetStyle.style = style
            updateUiStyle(style)
        }

        modelDao.getAllLive().observe(viewLifecycleOwner){ models ->
            val pairModelsFavourite = "Favourite" to models.filter { it.isFavourite }
            val pairModelsOther = "Other" to models.filter { !it.isFavourite && !it.isDislike }
            val pairModelsDislike = "Dislike" to models.filter { it.isDislike }

            sheetModel.pairs = listOf(pairModelsFavourite, pairModelsOther, pairModelsDislike)
        }

        styleDao.getAllLive().observe(viewLifecycleOwner) { styles ->
            sheetStyle.pairs = listOf("" to styles)
        }

        exploreDao.getAllLive().observe(viewLifecycleOwner) { explores ->
            val pairExploresFavourite = "Favourite" to explores.filter { it.isFavourite }
            val pairExploresOther = "Other" to explores.filter { !it.isFavourite && !it.isDislike }
            val pairExploresDislike = "Dislike" to explores.filter { it.isDislike }

            sheetExplore.pairs = listOf(pairExploresFavourite, pairExploresOther, pairExploresDislike)
        }

        loRAGroupDao.getAllLive().observe(viewLifecycleOwner){ loRAGroups ->
            val loRAs = loRAGroups.flatMap { loRAGroup -> loRAGroup.childs.map { loRAPreview -> LoRAPreview(loRA = loRAPreview, loRAGroupId = loRAGroup.id) } }

            val pairLoRAsFavourite = "Favourite" to loRAs.filter { loRAPreview -> loRAPreview.loRA.isFavourite }
            val pairLoRAsOther = "Other" to loRAs.filter { loRAPreview -> !loRAPreview.loRA.isFavourite && !loRAPreview.loRA.isDislike }
            val pairLoRAsDislike = "Dislike" to loRAs.filter { loRAPreview -> loRAPreview.loRA.isDislike }

            sheetLoRA.pairs = listOf(pairLoRAsFavourite, pairLoRAsOther, pairLoRAsDislike)
        }
    }

    private fun updateUiModel(model: Model?) {
        binding.viewNoModel.isVisible = model == null
        binding.viewHadModel.isVisible = model != null

        binding.displayModel.text = when (model) {
            null -> "Pick a Model"
            else -> model.display
        }
    }

    @SuppressLint("AutoDispose", "CheckResult")
    private fun initObservable() {
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

                binding.viewNoLoRA.isVisible = loRAAdapter.data.isEmpty()
                binding.viewHadLoRA.isVisible = loRAAdapter.data.isNotEmpty()
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
            .timer(500L, TimeUnit.MILLISECONDS)
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
                when {
                    prefs.isUpgraded.get() -> {
                        val findRatio = Ratio.values().find { it.ratio == explore?.ratio } ?: Ratio.Ratio1x1
                        subjectRatioClicks.onNext(findRatio)
                    }
                }

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

        prefs
            .isUpgraded
            .asObservable()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
            .bindToLifecycle(binding.root)
            .subscribe { isUpgraded ->
                binding.iconWatchAd.isVisible = !isUpgraded
                binding.textDescription.isVisible = !isUpgraded
            }
    }

    private fun updateUiExplore(explore: Explore?) {
        explore?.let { binding.editPrompt.setText(explore.prompt) }
    }

    private fun updateUiStyle(style: Style?){
        binding.viewNoStyle.isVisible = style == null
        binding.viewHadStyle.isVisible = style != null

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
                val alphaBottom = 1 - scrollY.toFloat() / binding.cardGenerate.height.toFloat()

                (activity as? ArtActivity)?.binding?.viewShadow?.alpha = alpha
                binding.viewShadowBottom.alpha = alphaBottom
            }
        }
        binding.cardGenerate.clicks(withAnim = false) { generateClicks() }
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

            binding.viewNoLoRA.isVisible = loRAAdapter.data.isEmpty()
            binding.viewHadLoRA.isVisible = loRAAdapter.data.isNotEmpty()
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

    private fun generateClicks() {
        val prompt = tryOrNull { binding.editPrompt.text?.trim()?.takeIf { it.isNotEmpty() }?.toString() } ?: tryOrNull { exploreDao.getAll().random().prompt } ?: listOf("Girl", "Boy").random()

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
                            sampler = "euler_a",
                            upscale = "2",
                            styleId = tryOrNull { sheetStyle.style?.id } ?: -1,
                            ratio = aspectRatioAdapter.ratio,
                            strength = strength.toString(),
                            seed = null,
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
                            sampler = "euler_a",
                            upscale = "2",
                            styleId = tryOrNull { sheetStyle.style?.id } ?: -1,
                            ratio = aspectRatioAdapter.ratio,
                            seed = null,
                            type = 0
                        )
                        configApp.dezgoBodiesImagesToImages = emptyList()
                    }
                }

                activity.startArtProcessing()
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
            prefs.isUpgraded.get() && prefs.numberCreatedArtwork.get() >= configApp.maxNumberGeneratePremium -> activity?.makeToast("You have requested more than ${Preferences.MAX_NUMBER_CREATE_ARTWORK_IN_A_DAY} times a day")
            else -> task()
        }
    }

    private fun initView() {
        activity?.let { activity ->
            binding.recyclerViewAspectRatio.apply {
                this.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
                this.adapter = aspectRatioAdapter
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