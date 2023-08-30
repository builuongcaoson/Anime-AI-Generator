package com.sola.anime.ai.generator.feature.main.art

import android.annotation.SuppressLint
import android.os.Build
import android.view.MotionEvent
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import com.sola.anime.ai.generator.common.ui.sheet.history.SheetHistory
import com.sola.anime.ai.generator.common.ui.sheet.model.SheetModel
import com.sola.anime.ai.generator.common.ui.sheet.photo.SheetPhoto
import com.sola.anime.ai.generator.common.ui.sheet.style.SheetStyle
import com.sola.anime.ai.generator.common.widget.cardSlider.CardSliderLayoutManager
import com.sola.anime.ai.generator.common.widget.cardSlider.CardSnapHelper
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.data.db.query.ExploreDao
import com.sola.anime.ai.generator.data.db.query.ModelDao
import com.sola.anime.ai.generator.data.db.query.StyleDao
import com.sola.anime.ai.generator.databinding.FragmentArtBinding
import com.sola.anime.ai.generator.domain.manager.AdmobManager
import com.sola.anime.ai.generator.domain.manager.AnalyticManager
import com.sola.anime.ai.generator.domain.model.Ratio
import com.sola.anime.ai.generator.domain.model.config.explore.Explore
import com.sola.anime.ai.generator.domain.model.config.model.Model
import com.sola.anime.ai.generator.domain.model.config.style.Style
import com.sola.anime.ai.generator.domain.repo.DezgoApiRepository
import com.sola.anime.ai.generator.feature.main.art.adapter.AspectRatioAdapter
import com.sola.anime.ai.generator.feature.main.art.adapter.PreviewAdapter
import com.sola.anime.ai.generator.feature.main.art.adapter.PreviewExploreAdapter
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
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
    @Inject lateinit var previewExploreAdapter: PreviewExploreAdapter
    @Inject lateinit var modelDao: ModelDao

    private val subjectFirstView: Subject<Boolean> = BehaviorSubject.createDefault(true)
    private val useExploreClicks: Subject<Explore> = PublishSubject.create()

    private val sheetHistory by lazy { SheetHistory() }
    private val sheetAdvanced by lazy { SheetAdvanced() }
    private val sheetPhoto by lazy { SheetPhoto() }
    private val sheetModel by lazy { SheetModel() }
    private val sheetStyle by lazy { SheetStyle() }

    override fun onViewCreated() {
        initView()
        initData()
        listenerView()
    }

    private fun initData() {
        exploreDao.getAllLive().observe(viewLifecycleOwner){ explores ->
            previewExploreAdapter.data = explores
        }

        modelDao.getAllLive().observeOnce(viewLifecycleOwner){ models ->
            val model = models.find { model -> model.modelId == Constraint.Dezgo.DEFAULT_MODEL } ?: models.firstOrNull()
            updateUiModel(model)
        }

        styleDao.getAllLive().observeOnce(viewLifecycleOwner) { styles ->
            val style = styles.find { style -> style.display == "No Style" } ?: styles.firstOrNull()
            updateUiStyle(style)
        }
    }

    override fun onResume() {
        initObservable()
        super.onResume()
    }

    private fun updateUiModel(model: Model?) {
        binding.viewNoModel.isVisible = model == null
        binding.viewHadModel.isVisible = model != null

        binding.displayModel.text = when (model) {
            null -> "Pick a Model"
            else -> model.display
        }
    }

    private fun initObservable() {
        sheetPhoto
            .clicks
            .autoDispose(scope())
            .subscribe { photo ->
                when {
                    photo.preview != null -> configApp.resPhoto = photo.preview
                    photo.photoStorage != null -> configApp.pairUriPhoto = tryOrNull { photo.photoStorage.uriString.toUri() to photo.photoStorage.ratio }
                }
            }

        configApp
            .subjectUriPhotoChanges
            .autoDispose(scope())
            .subscribe {
                binding.viewPhoto.isVisible = configApp.pairUriPhoto != null || configApp.resPhoto != null

                when {
                    configApp.pairUriPhoto != null -> {
                        val pair = configApp.pairUriPhoto ?: return@subscribe
                        binding.previewPhoto.load(pair.first, errorRes = R.drawable.place_holder_image)
                        configApp.subjectRatioClicks.onNext(pair.second)
                    }
                    configApp.resPhoto != null -> {
                        val res = configApp.resPhoto ?: return@subscribe
                        binding.previewPhoto.load(res, errorRes = R.drawable.place_holder_image)
                        configApp.subjectRatioClicks.onNext(Ratio.Ratio1x1)
                    }
                }
            }

        previewExploreAdapter
            .clicks
            .autoDispose(scope())
            .subscribe { explore ->
                activity?.let { activity -> exploreDialog.show(activity, explore, useExploreClicks) }
            }

        subjectFirstView
            .filter { it }
            .debounce(500, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribe {
                subjectFirstView.onNext(false)

                binding.recyclerViewExplore.animate().alpha(1f).setDuration(500).start()
            }

        aspectRatioAdapter
            .clicks
            .autoDispose(scope())
            .subscribe {
                when {
                    it == Ratio.Ratio1x1 -> configApp.subjectRatioClicks.onNext(it)
                    !prefs.isUpgraded.get() -> activity?.startIap()
                    else -> configApp.subjectRatioClicks.onNext(it)
                }
            }

        configApp
            .subjectRatioClicks
            .autoDispose(scope())
            .subscribe { ratio ->
                aspectRatioAdapter.ratio = ratio
            }

        configApp
            .subjectExploreClicks
            .filter { it != -1L }
            .autoDispose(scope())
            .subscribe { id ->
                configApp.subjectExploreClicks.onNext(-1)

                val explore = exploreDao.findById(id)

                when {
                    prefs.isUpgraded.get() -> {
                        val findRatio = Ratio.values().find { it.ratio == explore?.ratio } ?: Ratio.Ratio1x1
                        configApp.subjectRatioClicks.onNext(findRatio)
                    }
                }

                updateUiExplore(explore)
            }

        binding
            .editPrompt
            .textChanges()
            .autoDispose(scope())
            .subscribe { prompt ->
                binding.viewClear.isVisible = !prompt.isNullOrEmpty()
                binding.viewActionPrompt.isVisible = prompt.isNullOrEmpty()

                binding.count.text = "${prompt.length}/1000"
            }

        useExploreClicks
            .autoDispose(scope())
            .subscribe {
                exploreDialog.dismiss()

                configApp.subjectExploreClicks.onNext(it.id)
            }

        prefs
            .isUpgraded
            .asObservable()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribe { isUpgraded ->
                binding.viewPro.isVisible = !isUpgraded
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
            null -> "Pick a style"
            else -> style.display
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun listenerView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            binding.nestedScrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
                val alpha = scrollY.toFloat() / binding.viewShadow.height.toFloat()
                val alphaBottom = 1 - scrollY.toFloat() / binding.cardGenerate.height.toFloat()

                binding.viewShadow.alpha = alpha
                binding.viewShadowBottom.alpha = alphaBottom
            }
        }
        binding.viewPro.clicks { activity?.startIap() }
        binding.cardGenerate.clicks(withAnim = false) { generateClicks() }
        binding.viewModel.clicks(withAnim = false) { modelClicks() }
        binding.viewStyle.clicks(withAnim = false) { styleClicks() }
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
            sheetAdvanced.step = if (prefs.isUpgraded.get()) configApp.stepPremium else configApp.stepDefault
            sheetAdvanced.show(this)
        }
        binding.viewSeeAllExplore.clicks { activity?.startExplore() }
        binding.closePhoto.clicks {
            configApp.resPhoto = null
            configApp.pairUriPhoto = null

            tryOrNull { sheetPhoto.photoAdapter.photo = null }
        }
        binding.photo.clicks {
            if (sheetPhoto.isAdded){
                return@clicks
            }

            sheetPhoto.show(this)
        }
        binding.random.clicks { binding.editPrompt.setText(tryOrNull { exploreDao.getAll().random().prompt } ?: listOf("Girl", "Boy").random()) }
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
                this.adapter = previewExploreAdapter
            }

            binding.editPrompt.disableEnter()
        }
    }

}