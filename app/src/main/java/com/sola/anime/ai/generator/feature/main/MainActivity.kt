package com.sola.anime.ai.generator.feature.main

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.basic.common.base.LsActivity
import com.basic.common.base.LsPageAdapter
import com.basic.common.extension.*
import com.jakewharton.rxbinding2.view.clicks
import com.sola.anime.ai.generator.BuildConfig
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.SingletonOpenManager
import com.sola.anime.ai.generator.common.ConfigApp
import com.sola.anime.ai.generator.common.Navigator
import com.sola.anime.ai.generator.common.extension.startArt
import com.sola.anime.ai.generator.common.extension.startArtResult
import com.sola.anime.ai.generator.common.extension.startDetailExplore
import com.sola.anime.ai.generator.common.extension.startDetailModelOrLoRA
import com.sola.anime.ai.generator.common.extension.startPickAvatar
import com.sola.anime.ai.generator.common.extension.startSearch
import com.sola.anime.ai.generator.common.extension.startSetting
import com.sola.anime.ai.generator.common.ui.dialog.FeatureVersionDialog
import com.sola.anime.ai.generator.common.ui.dialog.RatingDialog
import com.sola.anime.ai.generator.common.ui.dialog.WarningPremiumDialog
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.databinding.ActivityMainBinding
import com.sola.anime.ai.generator.databinding.LayoutBottomMainBinding
import com.sola.anime.ai.generator.domain.repo.ServerApiRepository
import com.sola.anime.ai.generator.feature.main.mine.MineFragment
import com.sola.anime.ai.generator.feature.main.batch.BatchFragment
import com.sola.anime.ai.generator.feature.main.discover.DiscoverFragment
import com.sola.anime.ai.generator.feature.main.explore.ExploreFragment
import com.trello.rxlifecycle2.kotlin.bindToLifecycle
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import timber.log.Timber
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : LsActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {

    @Inject lateinit var configApp: ConfigApp
    @Inject lateinit var prefs: Preferences
    @Inject lateinit var warningPremiumDialog: WarningPremiumDialog
    @Inject lateinit var serverApiRepo: ServerApiRepository
    @Inject lateinit var featureVersionDialog: FeatureVersionDialog
    @Inject lateinit var ratingDialog: RatingDialog
    @Inject lateinit var navigator: Navigator
    @Inject lateinit var singletonOpenManager: SingletonOpenManager

    private val fragments by lazy { listOf(ExploreFragment(), BatchFragment(), DiscoverFragment(), MineFragment()) }
    private val bottomTabs by lazy { binding.initTabBottom() }
    private val subjectTabClicks: Subject<Int> = BehaviorSubject.createDefault(0) // Default tab home
    private var tabIndex = 0 // Default tab home
    private var ratingDialogShowing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transparent()
        setContentView(binding.root)

        when {
            configApp.version > BuildConfig.VERSION_CODE && !prefs.isShowFeatureDialog(configApp.version).get() -> {
                featureVersionDialog.show(this, configApp.version, configApp.feature)
            }
        }

        when {
            !prefs.isUpgraded.get() && isNetworkAvailable() && configApp.isOpenSplashOrBackground -> {
                singletonOpenManager.loadAd(this, getString(R.string.key_open_splash))
            }
        }

        initView()
        initObservable()
        listenerView()
    }

    fun startDetailModelOrLoRA(modelId: Long, isFull: Boolean) {
        startDetailModelOrLoRA(viewLoadingAds = binding.viewLoadingAds, modelId = modelId, isFull = isFull) { }
    }

    fun startDetailModelOrLoRA(loRAGroupId: Long, loRAId: Long, isFull: Boolean) {
        startDetailModelOrLoRA(viewLoadingAds = binding.viewLoadingAds, loRAGroupId = loRAGroupId, loRAId = loRAId, isFull = isFull) { }
    }

    fun startDetailExplore(exploreId: Long, isFull: Boolean) {
        startDetailExplore(viewLoadingAds = binding.viewLoadingAds, exploreId = exploreId, isFull = isFull) { }
    }

    fun startPickAvatar(isFull: Boolean) {
        startPickAvatar(viewLoadingAds = binding.viewLoadingAds, isFull = isFull) { }
    }

    fun startArt(isFull: Boolean) {
        startArt(viewLoadingAds = binding.viewLoadingAds, isFull = isFull) { }
    }

    fun startSearch(isFull: Boolean) {
        startSearch(viewLoadingAds = binding.viewLoadingAds, isFull = isFull) { }
    }

    fun startSetting(isFull: Boolean) {
        startSetting(viewLoadingAds = binding.viewLoadingAds, isFull = isFull) { }
    }

    fun startArt(exploreId: Long, isFull: Boolean) {
        startArt(viewLoadingAds = binding.viewLoadingAds, exploreId = exploreId, isFull = isFull) { }
    }

    fun startArtResult(historyId: Long, childHistoryIndex: Int, isGallery: Boolean, isFull: Boolean) {
        startArtResult(viewLoadingAds = binding.viewLoadingAds, historyId = historyId, childHistoryIndex = childHistoryIndex, isGallery = isGallery, isFull = isFull) { }
    }

    private fun listenerView() {
        binding.viewBottom.viewArt.clicks { startArt(viewLoadingAds = binding.viewLoadingAds, isFull = true) { } }
    }

    @SuppressLint("AutoDispose", "CheckResult")
    private fun initObservable() {
        bottomTabs.forEachIndexed { index, tab ->
            tab
                .viewClicks
                .clicks()
                .bindToLifecycle(binding.root)
                .subscribe { subjectTabClicks.onNext(index) }
        }

        subjectTabClicks
            .take(1)
            .delay(250, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
            .bindToLifecycle(binding.root)
            .subscribe { index ->
                scrollToPage(index)
            }

        subjectTabClicks
            .skip(1)
            .distinctUntilChanged()
            .bindToLifecycle(binding.root)
            .subscribe { index ->
                scrollToPage(index)

                when (index) {
                    0 -> darkStatusBar()
                    else -> lightStatusBar()
                }
            }

        prefs
            .isPurchasedCredit
            .asObservable()
            .filter { isPurchasedCredit -> isPurchasedCredit && !prefs.isShowedWaringPremiumDialog.get() && !prefs.isUpgraded.get() }
            .delay(1, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
            .bindToLifecycle(binding.root)
            .subscribe { isPurchasedCredit ->
                when {
                    isPurchasedCredit && !prefs.isShowedWaringPremiumDialog.get() && !prefs.isUpgraded.get() -> warningPremiumDialog.show(this)
                }
            }

        prefs
            .isUpgraded
            .asObservable()
            .filter { isUpgraded -> isUpgraded && !prefs.isShowedWaringPremiumDialog.get() }
            .delay(1, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
            .bindToLifecycle(binding.root)
            .subscribe { isUpgraded ->
                when {
                    isUpgraded && !prefs.isShowedWaringPremiumDialog.get() -> warningPremiumDialog.show(this)
                }
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

    private fun scrollToPage(index: Int) {
        val oldTab = bottomTabs.getOrNull(tabIndex)
        oldTab?.image?.isActivated = index == oldTab?.index
        oldTab?.image?.setTint(resolveAttrColor(if (index == oldTab.index) android.R.attr.colorAccent else android.R.attr.textColorPrimary))
        oldTab?.display?.setTextColor(resolveAttrColor(if (index == oldTab.index) android.R.attr.colorAccent else android.R.attr.textColorPrimary))

        val newTab = bottomTabs.getOrNull(index)
        newTab?.image?.isActivated = index == newTab?.index
        newTab?.image?.setTint(resolveAttrColor(if (index == newTab.index) android.R.attr.colorAccent else android.R.attr.textColorPrimary))
        newTab?.display?.setTextColor(resolveAttrColor(if (index == newTab.index) android.R.attr.colorAccent else android.R.attr.textColorPrimary))

        binding.viewPager.currentItem = index
        tabIndex = index
    }

    private fun initView() {
        binding.viewPager.apply {
            this.adapter = LsPageAdapter(supportFragmentManager, lifecycle).apply {
                this.addFragment(fragments = fragments.toTypedArray())
            }
            this.isUserInputEnabled = false
            this.offscreenPageLimit = this.adapter?.itemCount ?: 0
        }
    }

    private data class Tab(val viewClicks: View, val image: ImageView, val display: TextView, val index: Int)
    private fun ActivityMainBinding.initTabBottom() = listOf(viewBottom.initTabExplore(), viewBottom.initTabBatch(), viewBottom.initTabDiscover(), viewBottom.initTabMine())
    private fun LayoutBottomMainBinding.initTabExplore() = Tab(viewClicks = viewTab1, image = imageTab1, display = textTab1, index = 0)
    private fun LayoutBottomMainBinding.initTabBatch() = Tab(viewClicks = viewTab2, image = imageTab2, display = textTab2, index = 1)
    private fun LayoutBottomMainBinding.initTabDiscover() = Tab(viewClicks = viewTab4, image = imageTab4, display = textTab4, index = 2)
    private fun LayoutBottomMainBinding.initTabMine() = Tab(viewClicks = viewTab5, image = imageTab5, display = textTab5, index = 3)

    @Deprecated("Deprecated in Java", ReplaceWith("finish()"))
    override fun onBackPressed() {
        when {
            !prefs.isUpgraded.get() && !prefs.isRatedApp.get() && !ratingDialogShowing -> {
                ratingDialogShowing = true
                ratingDialog.show(this) { rating ->
                    when {
                        rating < 4 -> navigator.showSupport()
                        else -> navigator.showRating()
                    }
                    prefs.isRatedApp.set(true)
                    finish()
                }
            }
            else -> finish()
        }
    }

}