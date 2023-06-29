package com.sola.anime.ai.generator.feature.main.art

import android.annotation.SuppressLint
import android.os.Build
import android.view.MotionEvent
import android.view.ViewGroup.MarginLayoutParams
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.basic.common.base.LsFragment
import com.basic.common.extension.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.jakewharton.rxbinding2.widget.textChanges
import com.sola.anime.ai.generator.BuildConfig
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.common.ConfigApp
import com.sola.anime.ai.generator.common.Constraint
import com.sola.anime.ai.generator.common.extension.*
import com.sola.anime.ai.generator.common.ui.dialog.BlockSensitivesDialog
import com.sola.anime.ai.generator.common.ui.dialog.ExploreDialog
import com.sola.anime.ai.generator.common.ui.dialog.NetworkDialog
import com.sola.anime.ai.generator.common.ui.sheet.advanced.AdvancedSheet
import com.sola.anime.ai.generator.common.ui.sheet.history.HistorySheet
import com.sola.anime.ai.generator.common.ui.sheet.photo.SheetPhoto
import com.sola.anime.ai.generator.common.ui.sheet.upscale.UpscaleSheet
import com.sola.anime.ai.generator.common.widget.cardSlider.CardSliderLayoutManager
import com.sola.anime.ai.generator.common.widget.cardSlider.CardSnapHelper
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.data.db.query.ExploreDao
import com.sola.anime.ai.generator.data.db.query.StyleDao
import com.sola.anime.ai.generator.databinding.FragmentArtBinding
import com.sola.anime.ai.generator.domain.manager.AdmobManager
import com.sola.anime.ai.generator.domain.manager.AnalyticManager
import com.sola.anime.ai.generator.domain.model.Ratio
import com.sola.anime.ai.generator.domain.model.config.explore.Explore
import com.sola.anime.ai.generator.domain.model.config.style.Style
import com.sola.anime.ai.generator.domain.model.server.UserPremium
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
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class ArtFragment : LsFragment<FragmentArtBinding>(FragmentArtBinding::inflate) {

    @Inject lateinit var previewAdapter: PreviewAdapter
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

    private val subjectFirstView: Subject<Boolean> = BehaviorSubject.createDefault(true)
    private val useExploreClicks: Subject<Explore> = PublishSubject.create()

    private val historySheet by lazy { HistorySheet() }
    private val advancedSheet by lazy { AdvancedSheet() }
    private val sheetPhoto by lazy { SheetPhoto() }

    override fun onViewCreated() {
        initView()
        initData()
        listenerView()
    }

    private fun initData() {
        exploreDao.getAllRatio2x3Live().observe(viewLifecycleOwner){ explores ->
            previewAdapter.data = explores.shuffled()

            explores.size.takeIf { it > 0 }?.let { it / 2 }?.let { centerIndex ->
                tryOrNull { binding.recyclerPreview.smoothScrollToPosition(centerIndex) }
            }
        }

        exploreDao.getAllOtherRatio2x3Live().observe(viewLifecycleOwner){ explores ->
            previewExploreAdapter.data = explores
        }
    }

    override fun onResume() {
        initObservable()
        initDateResult()
        super.onResume()
    }

    private fun initDateResult() {
        updateUiStyle(configApp.styleChoice)
    }

    private fun initObservable() {
        configApp
            .subjectUriPhotoChanges
            .autoDispose(scope())
            .subscribe {
                binding.viewPhoto.isVisible = configApp.uriPhoto != null
                configApp.uriPhoto?.let { uri ->
                    Glide.with(this)
                        .load(uri)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .error(R.drawable.place_holder_image)
                        .into(binding.previewPhoto)
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

                binding.recyclerPreview.animate().alpha(1f).setDuration(500).start()
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

                advancedSheet.negative = explore?.negative ?: ""
                advancedSheet.guidance = explore?.guidance ?: 7.5f

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

        previewAdapter
            .clicks
            .autoDispose(scope())
            .subscribe { explore ->
                val index = previewAdapter.data.indexOf(explore).takeIf { it != -1 } ?: return@subscribe

                (binding.recyclerPreview.layoutManager as? CardSliderLayoutManager)?.let { layoutManager ->
                    if (layoutManager.isSmoothScrolling){
                        return@subscribe
                    }

                    val activeCardPosition = layoutManager.activeCardPosition
                    if (activeCardPosition == RecyclerView.NO_POSITION) {
                        return@subscribe
                    }

                    when {
                        index != activeCardPosition -> tryOrNull { binding.recyclerPreview.smoothScrollToPosition(index) }
                        else -> activity?.let { activity -> exploreDialog.show(activity, explore, useExploreClicks) }
                    }
                }
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

//                binding.textDescription.text = when {
//                    !isUpgraded -> "Watch an ad"
//                    else -> "You have ${Preferences.MAX_NUMBER_CREATE_ARTWORK_IN_A_DAY - prefs.numberCreatedArtwork.get()} times left today"
//                }
            }

//        prefs
//            .numberCreatedArtwork
//            .asObservable()
//            .filter { prefs.isUpgraded.get() }
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribeOn(AndroidSchedulers.mainThread())
//            .autoDispose(scope())
//            .subscribe { numberCreatedInDay ->
//                binding.textDescription.text = "You have ${Preferences.MAX_NUMBER_CREATE_ARTWORK_IN_A_DAY - numberCreatedInDay} times left today"
//            }
    }

    private fun updateUiExplore(explore: Explore?) {
        explore?.let { binding.editPrompt.setText(explore.prompt) }
    }

    private fun updateUiStyle(style: Style?){
        binding.viewNoStyle.isVisible = style == null
        binding.cardStyle.isVisible = style != null
        binding.viewMoreStyle.isVisible = style != null
        binding.viewHadStyle.isVisible = style != null

        binding.displayStyle.text = when (style) {
            null -> "Pick a style"
            else -> style.display
        }

        style?.let {
            activity?.let { activity ->
                Glide
                    .with(activity)
                    .load(style.preview)
                    .thumbnail(0.7f)
                    .placeholder(R.drawable.place_holder_image)
                    .error(R.drawable.place_holder_image)
                    .into(binding.previewStyle)
            }
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
        binding.viewStyle.clicks(withAnim = false) { activity?.startStyle() }
        binding.clear.clicks { binding.editPrompt.setText("") }
        binding.history.clicks {
            if (historySheet.isAdded){
                return@clicks
            }

            historySheet.show(this)
        }
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
            if (advancedSheet.isAdded){
                return@clicks
            }

            advancedSheet.show(this)
        }
        binding.viewSeeAllExplore.clicks { activity?.startExplore() }
        binding.closePhoto.clicks { configApp.uriPhoto = null }
        binding.photo.clicks {
            if (sheetPhoto.isAdded){
                return@clicks
            }

            sheetPhoto.show(this)
        }
    }

    private fun generateClicks() {
        val prompt = tryOrNull { binding.editPrompt.text?.trim()?.takeIf { it.isNotEmpty() }?.toString() } ?: tryOrNull { exploreDao.getAll().random().prompt } ?: listOf("Girl", "Boy").random()
        val pattern = configApp.sensitiveKeywords.joinToString(separator = "|").toRegex(RegexOption.IGNORE_CASE)

        val task = {
            analyticManager.logEvent(AnalyticManager.TYPE.GENERATE_CLICKED)

            configApp.dezgoBodiesTextsToImages = initDezgoBodyTextsToImages(
                groupId = 0,
                maxChildId = 0,
                prompt = prompt,
                negativePrompt = advancedSheet.negative.takeIf { it.isNotEmpty() }?.let { Constraint.Dezgo.DEFAULT_NEGATIVE + ", $it" } ?: Constraint.Dezgo.DEFAULT_NEGATIVE,
                guidance = advancedSheet.guidance.toString(),
                steps = if (BuildConfig.DEBUG) configApp.stepDefault else if (!prefs.isUpgraded.get()) configApp.stepDefault else configApp.stepPremium,
                model = "anything_4_0",
                sampler = "euler_a",
                upscale = "2",
                styleId = configApp.styleChoice?.id ?: -1,
                ratio = aspectRatioAdapter.ratio,
                seed = null,
                type = 0
            )

            activity?.startArtProcessing()
        }

        when {
            prompt.contains(pattern) && !prefs.isEnableNsfw.get() -> activity?.let { activity -> blockSensitivesDialog.show(activity) }
            !isNetworkAvailable() -> activity?.let { activity -> networkDialog.show(activity) }
            !prefs.isUpgraded.get() && prefs.numberCreatedArtwork.get() >= configApp.maxNumberGenerateFree -> activity?.startIap()
            !prefs.isUpgraded.get() -> {
                activity?.let { activity ->
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
            }
            prefs.isUpgraded.get() && prefs.numberCreatedArtwork.get() >= configApp.maxNumberGeneratePremium -> activity?.makeToast("You have requested more than ${Preferences.MAX_NUMBER_CREATE_ARTWORK_IN_A_DAY} times a day")
            else -> task()
        }
    }

//    private val pickLauncherResult = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
//        uri?.let {
//            CoroutineScope(Dispatchers.IO).launch {
//                dezgoApiRepo.generateImagesToImages(uri)
//            }
//        }
//    }
//
//    private fun launchPickPhoto() {
//        pickLauncherResult.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
//    }

    private fun initView() {
        activity?.let { activity ->
            binding.viewTop.updateLayoutParams<MarginLayoutParams> {
                this.topMargin = when(val statusBarHeight = activity.getStatusBarHeight()) {
                    0 -> activity.getDimens(com.intuit.sdp.R.dimen._30sdp).toInt()
                    else -> statusBarHeight
                }
            }

            binding.recyclerPreview.apply {
                this.adapter = previewAdapter
                this.setHasFixedSize(true)

                CardSnapHelper().attachToRecyclerView(this)
            }

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