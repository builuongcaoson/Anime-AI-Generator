package com.sola.anime.ai.generator.feature.result.art

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.core.view.drawToBitmap
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.afollestad.materialdialogs.MaterialDialog
import com.basic.common.base.LsActivity
import com.basic.common.extension.clicks
import com.basic.common.extension.getDimens
import com.basic.common.extension.isNetworkAvailable
import com.basic.common.extension.lightStatusBar
import com.basic.common.extension.makeToast
import com.basic.common.extension.transparent
import com.basic.common.extension.tryOrNull
import com.sola.anime.ai.generator.common.ConfigApp
import com.sola.anime.ai.generator.common.Constraint
import com.sola.anime.ai.generator.common.Navigator
import com.sola.anime.ai.generator.common.extension.*
import com.sola.anime.ai.generator.common.ui.dialog.NetworkDialog
import com.sola.anime.ai.generator.common.ui.dialog.RatingDialog
import com.sola.anime.ai.generator.common.ui.sheet.download.DownloadSheet
import com.sola.anime.ai.generator.common.ui.sheet.share.ShareSheet
import com.sola.anime.ai.generator.common.ui.sheet.upscale.UpscaleSheet
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.data.db.query.HistoryDao
import com.sola.anime.ai.generator.data.db.query.StyleDao
import com.sola.anime.ai.generator.databinding.ActivityArtResultBinding
import com.sola.anime.ai.generator.domain.manager.AdmobManager
import com.sola.anime.ai.generator.domain.manager.AnalyticManager
import com.sola.anime.ai.generator.domain.model.Ratio
import com.sola.anime.ai.generator.domain.model.history.ChildHistory
import com.sola.anime.ai.generator.domain.repo.FileRepository
import com.sola.anime.ai.generator.domain.repo.ServerApiRepository
import com.sola.anime.ai.generator.domain.repo.UpscaleApiRepository
import com.sola.anime.ai.generator.feature.result.art.adapter.PagePreviewAdapter
import com.sola.anime.ai.generator.feature.result.art.adapter.PreviewAdapter
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

@AndroidEntryPoint
class ArtResultActivity : LsActivity<ActivityArtResultBinding>(ActivityArtResultBinding::inflate) {

    companion object {
        const val HISTORY_ID_EXTRA = "HISTORY_ID_EXTRA"
        const val CHILD_HISTORY_INDEX_EXTRA = "CHILD_HISTORY_INDEX_EXTRA"
        const val IS_GALLERY_EXTRA = "IS_GALLERY_EXTRA"
    }

    @Inject lateinit var previewAdapter: PreviewAdapter
    @Inject lateinit var pagePreviewAdapter: PagePreviewAdapter
    @Inject lateinit var historyDao: HistoryDao
    @Inject lateinit var styleDao: StyleDao
    @Inject lateinit var fileRepo: FileRepository
    @Inject lateinit var configApp: ConfigApp
    @Inject lateinit var admobManager: AdmobManager
    @Inject lateinit var prefs: Preferences
    @Inject lateinit var networkDialog: NetworkDialog
    @Inject lateinit var analyticManager: AnalyticManager
    @Inject lateinit var serverApiRepo: ServerApiRepository
    @Inject lateinit var upscaleApiRepo: UpscaleApiRepository
    @Inject lateinit var ratingDialog: RatingDialog
    @Inject lateinit var navigator: Navigator

    private val subjectPageChanges: Subject<ChildHistory> = PublishSubject.create()

    private val historyId by lazy { intent.getLongExtra(HISTORY_ID_EXTRA, -1L) }
    private val childHistoryIndex by lazy { intent.getIntExtra(CHILD_HISTORY_INDEX_EXTRA, -1) }
    private val isGallery by lazy { intent.getBooleanExtra(IS_GALLERY_EXTRA, true) }

    private var childHistories = arrayListOf<ChildHistory>()
    private val upscaleSheet by lazy { UpscaleSheet() }
    private val downloadSheet by lazy { DownloadSheet() }
    private val shareSheet by lazy { ShareSheet() }

    private var totalCreditsDeducted = 0f
    private var creditsPerImage = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transparent()
        lightStatusBar()
        setContentView(binding.root)

        when (historyId) {
            -1L -> {
                finish()
                return
            }
        }

        if (!isGallery){
            analyticManager.logEvent(AnalyticManager.TYPE.GENERATE_SUCCESS)

            prefs.numberCreatedArtwork.set(prefs.numberCreatedArtwork.get() + 1)
            prefs.totalNumberCreatedArtwork.set(prefs.totalNumberCreatedArtwork.get() + 1)
            prefs.latestTimeCreatedArtwork.set(System.currentTimeMillis())
        }

        lifecycleScope.launch(Dispatchers.Main) {
            delay(2000L)

            when {
                !isGallery && !prefs.isUpgraded.get() && !prefs.isRatedApp.get() && ((prefs.totalNumberCreatedArtwork.get() - 1L) % 3L) == 0L -> {
                    ratingDialog.show(this@ArtResultActivity) { rating ->
                        when {
                            rating < 4 -> navigator.showSupport()
                            else -> navigator.showRating()
                        }

                        prefs.isRatedApp.set(true)
                        ratingDialog.dismiss()
                    }
                }
            }
        }

        initView()
        initObservable()
        initData()
        listenerView()
    }

    private fun listenerView() {
        binding.back.clicks { onBackPressed() }
        binding.viewPro.clicks { startIap() }
        binding.viewCredit.clicks { startCredit() }
        binding.viewPager.registerOnPageChangeCallback(pageChanges)
        binding.cardShare.clicks(withAnim = false) { tryOrNull { shareClicks() } }
        binding.cardDownload.clicks(withAnim = false) { tryOrNull { downloadClicks() } }
        binding.cardGenerate.clicks(withAnim = false) { tryOrNull { generateAgainClicks() } }
    }

    private fun generateAgainClicks() {
        val history = historyDao.findById(historyId) ?: return
        val childHistory = tryOrNull { history.childs.getOrNull(binding.viewPager.currentItem) } ?: history.childs.firstOrNull() ?: return

        val task = {
            val photoUri = tryOrNull { childHistory.photoUriString?.toUri() }
            when {
                photoUri != null -> {
                    configApp.creditsRemaining = prefs.getCredits()
                    configApp.dezgoBodiesTextsToImages = listOf()
                    configApp.dezgoBodiesImagesToImages = initDezgoBodyImagesToImages(
                        context = this,
                        prefs = prefs,
                        configApp = configApp,
                        creditsPerImage = creditsPerImage,
                        groupId = 0,
                        maxChildId = 0,
                        initImage = photoUri,
                        prompt = history.prompt,
                        negative = childHistory.negativePrompt.takeIf { it.isNotEmpty() } ?: Constraint.Dezgo.DEFAULT_NEGATIVE,
                        guidance = childHistory.guidance,
                        steps = childHistory.steps,
                        model = childHistory.model,
                        sampler = childHistory.sampler,
                        upscale = childHistory.upscale,
                        styleId = history.styleId,
                        ratio = Ratio.values().firstOrNull { it.width == childHistory.width && it.height == childHistory.height } ?: Ratio.Ratio1x1,
                        strength = childHistory.strength ?: Constraint.Dezgo.DEFAULT_STRENGTH_IMG_TO_IMG,
                        seed = null,
                        loRAs = childHistory.loRAs,
                        type = childHistory.type
                    )
                }
                else -> {
                    configApp.creditsRemaining = prefs.getCredits()
                    configApp.dezgoBodiesTextsToImages = initDezgoBodyTextsToImages(
                        context = this,
                        prefs = prefs,
                        configApp = configApp,
                        creditsPerImage = creditsPerImage,
                        groupId = 0,
                        maxChildId = 0,
                        prompt = history.prompt,
                        negative = childHistory.negativePrompt.takeIf { it.isNotEmpty() } ?: Constraint.Dezgo.DEFAULT_NEGATIVE,
                        guidance = childHistory.guidance,
                        steps = childHistory.steps,
                        model = childHistory.model,
                        sampler = childHistory.sampler,
                        upscale = childHistory.upscale,
                        styleId = history.styleId,
                        ratio = Ratio.values().firstOrNull { it.width == childHistory.width && it.height == childHistory.height } ?: Ratio.Ratio1x1,
                        seed = null,
                        loRAs = childHistory.loRAs,
                        type = childHistory.type
                    )
                    configApp.dezgoBodiesImagesToImages = listOf()
                }
            }

            startArtProcessing(totalCreditsDeducted = totalCreditsDeducted, creditsPerImage = creditsPerImage)
            finish()
        }

        when {
            !isNetworkAvailable() -> networkDialog.show(this)
            !prefs.isUpgraded.get() -> {
                when {
                    totalCreditsDeducted == 0f && prefs.numberCreatedArtwork.get() < configApp.maxNumberGenerateFree && configApp.scriptIap == "0" -> task()
                    totalCreditsDeducted == 0f && binding.description.text.contains("Watch an Ad") && prefs.numberCreatedArtwork.get() < configApp.maxNumberGenerateReward && configApp.scriptIap == "1" -> {
                        admobManager.showReward(
                            this,
                            success = {
                                task()
                                admobManager.loadReward()
                            },
                            failed = {
                                makeToast("Please watch all ads to perform the function!")
                                admobManager.loadReward()
                            }
                        )
                    }
                    totalCreditsDeducted != 0f && totalCreditsDeducted < prefs.getCredits() -> task()
                    else -> startIap()
                }
            }
            prefs.isUpgraded.get() -> {
                when {
                    totalCreditsDeducted >= prefs.getCredits() -> startCredit()
                    else -> task()
                }
            }
            else -> task()
        }
    }

    private fun downloadClicks() {
        if (downloadSheet.isAdded){
            return
        }

        childHistories
            .getOrNull(binding.viewPager.currentItem)
            ?.let { childHistory ->
                File(childHistory.upscalePathPreview ?: childHistory.pathPreview)
            }?.takeIf { file -> file.exists() }
            ?.let { file ->
                downloadSheet.file = file
                downloadSheet.ratio = "${childHistories.getOrNull(binding.viewPager.currentItem)?.width ?: "1"}:${childHistories.getOrNull(binding.viewPager.currentItem)?.height ?: "1"}"
                downloadSheet.show(this)
        } ?: run {
            makeToast("Something wrong, please try again!")
        }
    }

    private fun shareClicks() {
        if (shareSheet.isAdded){
            return
        }

        childHistories
            .getOrNull(binding.viewPager.currentItem)
            ?.let { childHistory ->
                File(childHistory.upscalePathPreview ?: childHistory.pathPreview)
            }?.takeIf { file -> file.exists() }
            ?.let { file ->
                shareSheet.file = file
                shareSheet.ratio = "${childHistories.getOrNull(binding.viewPager.currentItem)?.width ?: "1"}:${childHistories.getOrNull(binding.viewPager.currentItem)?.height ?: "1"}"
                shareSheet.show(this)
            } ?: run {
            makeToast("Something wrong, please try again!")
        }
    }

    override fun onDestroy() {
        binding.viewPager.unregisterOnPageChangeCallback(pageChanges)
        super.onDestroy()
    }

    private val pageChanges = object: ViewPager2.OnPageChangeCallback() {
        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
            tryOrNull { childHistories.getOrNull(position)?.let { childHistory -> subjectPageChanges.onNext(childHistory) } }
        }
    }

    private fun initData() {
        historyDao.getWithIdLive(id = historyId).observe(this){ history ->
            history?.let {
                tryOrNull { binding.displayStyle.text = styleDao.findById(history.styleId)?.display ?: "No Style" }
                tryOrNull { binding.textPrompt.text = history.prompt }

                tryOrNull { childHistories = ArrayList(history.childs) }
                tryOrNull { previewAdapter.data = ArrayList(history.childs) }
                tryOrNull { pagePreviewAdapter.data = ArrayList(history.childs) }

                childHistoryIndex.takeIf { it != -1 }?.let {
                    tryOrNull { binding.viewPager.post { binding.viewPager.setCurrentItem(childHistoryIndex, false) } }
                    tryOrNull { binding.recyclerPreview.scrollToPosition(childHistoryIndex) }
                } ?: run {
                    tryOrNull { binding.viewPager.post { binding.viewPager.setCurrentItem(history.childs.lastIndex, false) } }
                    tryOrNull { binding.recyclerPreview.scrollToPosition(history.childs.lastIndex) }
                }

                lifecycleScope.launch(Dispatchers.Main) {
                    delay(250L)

                    binding.viewPager.animate().alpha(1f).setDuration(250L).start()
                    binding.recyclerPreview.animate().alpha(1f).setDuration(250L).start()
                }

                tryOrNull { updateUiCredits() }
            }
        }
    }

    private fun initObservable() {
        prefs
            .creditsChanges
            .asObservable()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribe {
                binding.credits.text = prefs.getCredits().roundToInt().toString()
            }

        subjectPageChanges
            .debounce(100, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribe {
                tryOrNull { previewAdapter.childHistory = it }

                tryOrNull { updateUiCredits() }
            }

        previewAdapter
            .clicks
            .map { previewAdapter.data.indexOf(it) }
            .filter { it != -1 }
            .autoDispose(scope())
            .subscribe { index ->
                tryOrNull { binding.viewPager.post { binding.viewPager.currentItem = index } }
            }

        pagePreviewAdapter
            .upscaleClicks
            .map { previewAdapter.data.indexOf(it) }
            .filter { it != -1 }
            .autoDispose(scope())
            .subscribe {
                upscaleSheet.show(this)
            }

        pagePreviewAdapter
            .longClicks
            .autoDispose(scope())
            .subscribe { childHistory ->
                MaterialDialog(this)
                    .show {
                        title(text = "Delete artwork?")
                        message(text = "Are you sure you want to delete artwork? You can't undo this action.")
                        positiveButton(text = "Delete") { dialog ->
                            dialog.dismiss()

                            val history = historyDao.findById(historyId) ?: return@positiveButton
                            tryOrNull {
                                history.childs.remove(childHistory)

                                when {
                                    history.childs.isNotEmpty() -> historyDao.update(history)
                                    else -> {
                                        historyDao.delete(history)
                                        back()
                                    }
                                }
                            }
                        }
                        negativeButton(text = "Cancel") { dialog ->
                            dialog.dismiss()
                        }
                    }
            }

        pagePreviewAdapter
            .clicks
            .map { previewAdapter.data.indexOf(it) }
            .filter { it != -1 }
            .autoDispose(scope())
            .subscribe { index ->
                tryOrNull { startPreview(historyId = historyId, childHistoryIndex = index) }
            }

        prefs
            .isUpgraded
            .asObservable()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribe { isUpgraded ->
                binding.viewPro.isVisible = !isUpgraded
            }

        upscaleSheet
            .upscaleClicks
            .debounce(250, TimeUnit.MILLISECONDS)
            .map { binding.viewPager.currentItem }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribe { index ->
                tryOrNull { upscaleSheet.dismiss() }

                val task = {
                    binding.viewLoading.isVisible = true
                    upscaleClicks(index)
                }

                when {
                    !prefs.isUpgraded.get() -> {
                        lifecycleScope.launch(Dispatchers.Main) {
                            delay(250)
                            admobManager.showReward(
                                this@ArtResultActivity,
                                success = {
                                    task()
                                    admobManager.loadReward()
                                },
                                failed = {
                                    makeToast("Please watch all ads to perform the function!")
                                    admobManager.loadReward()
                                }
                            )
                        }
                    }
                    else -> task()
                }
            }

        downloadSheet
            .downloadFrameClicks
            .autoDispose(scope())
            .subscribe { view ->
                tryOrNull {
                    analyticManager.logEvent(AnalyticManager.TYPE.DOWNLOAD_CLICKED)

                    lifecycleScope.launch(Dispatchers.IO) {
                        val bitmap = tryOrNull { view.drawToBitmap() } ?: return@launch
                        fileRepo.downloads(bitmap)
                        launch(Dispatchers.Main) {
                            tryOrNull { downloadSheet.dismiss() }

                            makeToast("Download successfully!")
                        }
                    }
                }
            }

        downloadSheet
            .downloadOriginalClicks
            .autoDispose(scope())
            .subscribe { file ->

                val task = {
                    analyticManager.logEvent(AnalyticManager.TYPE.DOWNLOAD_ORIGINAL_CLICKED)

                    tryOrNull {
                        lifecycleScope.launch {
                            fileRepo.downloads(file)
                            launch(Dispatchers.Main) {
                                prefs.numberDownloadedOriginal.set(prefs.numberDownloadedOriginal.get() + 1)
                                tryOrNull { downloadSheet.dismiss() }
                                makeToast("Download successfully!")
                            }
                        }
                    }
                }

                task()
            }

        shareSheet
            .shareFrameClicks
            .autoDispose(scope())
            .subscribe { view ->
                tryOrNull {
                    analyticManager.logEvent(AnalyticManager.TYPE.SHARE_CLICKED)

                    lifecycleScope.launch(Dispatchers.IO) {
                        val file = tryOrNull { view.drawToBitmap().toFile(this@ArtResultActivity) } ?: return@launch
                        launch(Dispatchers.Main){ fileRepo.shares(file) }
                    }
                }
            }

        shareSheet
            .shareOriginalClicks
            .autoDispose(scope())
            .subscribe { file ->
                val task = {
                    analyticManager.logEvent(AnalyticManager.TYPE.SHARE_ORIGINAL_CLICKED)

                    lifecycleScope.launch(Dispatchers.Main) { fileRepo.shares(file) }
                }

                when {
                    else -> task()
                }
            }

        Observable
            .timer(1, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribe {
                binding.viewCredit.animate().alpha(1f).setDuration(250L).start()
                binding.viewPro.animate().alpha(1f).setDuration(250L).start()
            }
    }

    private fun upscaleClicks(index: Int){
        val doOnSuccess = {
            lifecycleScope.launch(Dispatchers.Main) {
                binding.viewLoading.isVisible = false
                makeToast("Upscale success!")
            }
        }

        val doOnFailed = {
            lifecycleScope.launch(Dispatchers.Main) {
                delay(1000)
                binding.viewLoading.isVisible = false
                makeToast("An error occurred, please try again or report it to us!")
            }
        }

        previewAdapter
            .data
            .getOrNull(index)
            ?.pathPreview
            ?.let { path -> File(path) }
            ?.takeIf { file -> file.exists() }
            ?.let { file ->
                lifecycleScope.launch {
                    upscaleApiRepo.upscale(file){ fileUpscale ->
                        when {
                            fileUpscale != null && fileUpscale.exists() -> {
                                val history = historyDao.findById(historyId) ?: return@upscale
                                history.childs.getOrNull(index)?.upscalePathPreview = fileUpscale.path
                                historyDao.update(history)

                                doOnSuccess()
                            }
                            else -> doOnFailed()
                        }
                    }
                }
        } ?: run {
            doOnFailed()
        }
    }

    override fun onResume() {
        initPremiumObservable()
        super.onResume()
    }

    private fun initPremiumObservable() {
        Observable
            .interval(1, TimeUnit.SECONDS)
            .filter { prefs.isUpgraded.get() }
            .map { prefs.timeExpiredPremium.get() }
            .filter { timeExpired -> timeExpired != -1L && timeExpired != -2L }
            .map { timeExpired -> timeExpired - Date().time }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribe { differenceInMillis ->
                val days = TimeUnit.MILLISECONDS.toDays(differenceInMillis)
                val hours = TimeUnit.MILLISECONDS.toHours(differenceInMillis) % 24
                val minutes = TimeUnit.MILLISECONDS.toMinutes(differenceInMillis) % 60
                val seconds = TimeUnit.MILLISECONDS.toSeconds(differenceInMillis) % 60

                when {
                    days <= 0 && hours <= 0 && minutes <= 0 && seconds <= 0 -> {
                        prefs.isUpgraded.delete()
                        prefs.timeExpiredPremium.delete()
                    }
                }

                Timber.tag("Main12345").e("Date: $days --- $hours:$minutes:$seconds")
            }
    }

    private fun initView() {
        binding.viewTop.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            this.topMargin = when(val statusBarHeight = getStatusBarHeight()) {
                0 -> getDimens(com.intuit.sdp.R.dimen._30sdp).toInt()
                else -> statusBarHeight
            }
        }

        binding.viewPager.adapter = pagePreviewAdapter

        binding.recyclerPreview.apply {
            this.itemAnimator = null
            this.adapter = previewAdapter
        }

        binding.textPrompt.movementMethod = ScrollingMovementMethod()
    }

    private fun updateUiCredits() {
        childHistories.getOrNull(binding.viewPager.currentItem)?.let { childHistory ->
            val description = when {
                !prefs.isUpgraded.get() && prefs.numberCreatedArtwork.get() >= configApp.maxNumberGenerateReward -> {
                    totalCreditsDeducted = 10f + childHistory.loRAs.size * 5
                    creditsPerImage = 10f + childHistory.loRAs.size * 5

                    "Generate Again (${totalCreditsDeducted.roundToInt()} Credits)"
                }
                !prefs.isUpgraded.get() && prefs.numberCreatedArtwork.get() < configApp.maxNumberGenerateReward -> {
                    totalCreditsDeducted = when {
                        prefs.getCredits() >= 10f + childHistory.loRAs.size * 5 -> 10f + childHistory.loRAs.size * 5
                        childHistory.loRAs.isNotEmpty() -> 10f + childHistory.loRAs.size * 5
                        else -> 0f
                    }
                    creditsPerImage = when {
                        prefs.getCredits() >= 10f + childHistory.loRAs.size * 5 -> 10f + childHistory.loRAs.size * 5
                        childHistory.loRAs.isNotEmpty() -> 10f + childHistory.loRAs.size * 5
                        else -> 0f
                    }

                    when {
                        creditsPerImage == 0f && prefs.numberCreatedArtwork.get() < configApp.maxNumberGenerateFree && configApp.scriptIap == "0" -> "Generate Again"
                        creditsPerImage == 0f && prefs.numberCreatedArtwork.get() < configApp.maxNumberGenerateReward && configApp.scriptIap == "1"-> "Generate Again (Watch an Ad)"
                        else -> "Generate Again (${(if (totalCreditsDeducted >= 10f) totalCreditsDeducted else 10f).roundToInt()} Credits)"
                    }
                }
                prefs.isUpgraded.get() && prefs.numberCreatedArtwork.get() >= configApp.maxNumberGeneratePremium -> {
                    totalCreditsDeducted = 10f + childHistory.loRAs.size * 5
                    creditsPerImage = 10f + childHistory.loRAs.size * 5

                    "Generate Again (${totalCreditsDeducted.roundToInt()} Credits)"
                }
                prefs.isUpgraded.get() && prefs.numberCreatedArtwork.get() < configApp.maxNumberGeneratePremium -> {
                    totalCreditsDeducted = 0f + childHistory.loRAs.size * 5
                    creditsPerImage = 0f + childHistory.loRAs.size * 5

                    "Generate Again (${totalCreditsDeducted.roundToInt()} Credits)"
                }
                else -> {
                    totalCreditsDeducted = 0f
                    creditsPerImage = 0f

                    "Generate Again"
                }
            }
            binding.description.text = description
        } ?: run {
            binding.description.text = "Generate Again"
        }
    }

    @Deprecated("Deprecated in Java", ReplaceWith("finish()"))
    override fun onBackPressed() {
        when {
            !prefs.isUpgraded.get() && isNetworkAvailable() && !isGallery -> admobManager.showReward(
                this,
                success = {
                    back()
                    admobManager.loadReward()
                },
                failed = {
                    back()
                    admobManager.loadReward()
                }
            )
            else -> back()
        }
    }

}