package com.sola.anime.ai.generator.feature.main

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.text.format.DateUtils
import android.view.View
import com.basic.common.base.LsActivity
import com.basic.common.base.LsPageAdapter
import com.basic.common.extension.getDimens
import com.basic.common.extension.lightStatusBar
import com.basic.common.extension.transparent
import com.jakewharton.rxbinding2.view.clicks
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.getCustomerInfoWith
import com.sola.anime.ai.generator.BuildConfig
import com.sola.anime.ai.generator.common.App
import com.sola.anime.ai.generator.common.ConfigApp
import com.sola.anime.ai.generator.common.Constraint
import com.sola.anime.ai.generator.common.ui.dialog.WarningPremiumDialog
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.databinding.ActivityMainBinding
import com.sola.anime.ai.generator.databinding.LayoutBottomMainBinding
import com.sola.anime.ai.generator.domain.manager.AdmobManager
import com.sola.anime.ai.generator.feature.main.mine.MineFragment
import com.sola.anime.ai.generator.feature.main.art.ArtFragment
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : LsActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {

    @Inject lateinit var configApp: ConfigApp
    @Inject lateinit var prefs: Preferences
    @Inject lateinit var admobManager: AdmobManager
    @Inject lateinit var warningPremiumDialog: WarningPremiumDialog

//    private val fragments by lazy { listOf(ArtFragment(), BatchFragment(), DiscoverFragment(), MineFragment()) }
//    private val fragments by lazy { listOf(ArtFragment(), BatchFragment(), MineFragment()) }
    private val fragments by lazy { listOf(ArtFragment(), MineFragment()) }
    private val bottomTabs by lazy { binding.initTabBottom() }
    private val subjectTabClicks: Subject<Int> = BehaviorSubject.createDefault(0) // Default tab home
    private var tabIndex = 0 // Default tab home

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transparent()
        lightStatusBar()
        setContentView(binding.root)

        when {
            !prefs.isUpgraded.get() -> admobManager.loadRewardCreate()
        }

        initView()
        syncUserPurchased()
        initObservable()
    }

    @SuppressLint("SimpleDateFormat")
    private fun syncUserPurchased() {
        Purchases.sharedInstance.getCustomerInfoWith { customerInfo ->
            val isActive = customerInfo.entitlements["premium"]?.isActive ?: false
            Timber.tag("Main12345").e("##### SPLASH #####")
            Timber.tag("Main12345").e("Is upgraded: ${prefs.isUpgraded.get()}")

            if (isActive){
                if (configApp.skipSyncPremium){
                    customerInfo
                        .latestExpirationDate
                        ?.let { expiredDate ->
                            prefs.isUpgraded.set(true)
                            prefs.timeExpiredIap.set(expiredDate.time)
                        }
                    return@getCustomerInfoWith
                }

                customerInfo
                    .allPurchaseDatesByProduct
                    .filter { it.value != null }
                    .filter { it.key.contains(Constraint.Iap.SKU_WEEK) || it.key.contains(Constraint.Iap.SKU_MONTH) ||it.key.contains(Constraint.Iap.SKU_YEAR) }
                    .takeIf { it.isNotEmpty() }
                    ?.maxByOrNull { it.value!! }
                    ?.let { map ->
                        val latestPurchasedProduct = map.key
                        val latestDatePurchased = map.value ?: return@let
                        val expiredDate = customerInfo.getExpirationDateForProductId(latestPurchasedProduct) ?: return@let

                        val expiredDateTime = when {
                            prefs.isUpgraded.get() -> expiredDate.time
                            else -> latestDatePurchased.time + if (BuildConfig.DEBUG) 0 else 21600000L // Day time purchased + 6 hours
                        }

                        val differenceInMillis = expiredDateTime - Date().time
                        if (differenceInMillis > 0){
                            val days = TimeUnit.MILLISECONDS.toDays(differenceInMillis)
                            val hours = TimeUnit.MILLISECONDS.toHours(differenceInMillis) % 24
                            val minutes = TimeUnit.MILLISECONDS.toMinutes(differenceInMillis) % 60
                            val seconds = TimeUnit.MILLISECONDS.toSeconds(differenceInMillis) % 60

                            when {
                                days <= 0 && hours <= 0 && minutes <= 0 && seconds <= 0 -> {
                                    prefs.isUpgraded.delete()
                                    prefs.timeExpiredIap.delete()
                                }
                                else -> {
                                    prefs.isUpgraded.set(true)
                                    prefs.timeExpiredIap.set(expiredDate.time)
                                }
                            }

                            Timber.tag("Main12345").e("Time Purchased: ${SimpleDateFormat("dd/MM/yyyy - hh:mm:ss").format(latestDatePurchased)}")
                            Timber.tag("Main12345").e("Time Expired: ${SimpleDateFormat("dd/MM/yyyy - hh:mm:ss").format(expiredDate)}")
                            Timber.tag("Main12345").e("Date: $days --- $hours:$minutes:$seconds")
                        } else {
                            prefs.isUpgraded.delete()
                            prefs.timeExpiredIap.delete()
                        }
                        Timber.tag("Main12345").e("DifferenceInMillis: $differenceInMillis --- ${latestDatePurchased.time} --- ${Date().time}")
                    }
                when {
                    prefs.isUpgraded.get() && !DateUtils.isToday(prefs.latestTimeCreatedArtwork.get()) -> {
                        prefs.numberCreatedArtwork.delete()
                        prefs.latestTimeCreatedArtwork.delete()
                    }
                }
            } else {
                prefs.isUpgraded.delete()
                prefs.timeExpiredIap.delete()
            }
        }
    }

    private fun initObservable() {
        bottomTabs.forEachIndexed { index, tab ->
            tab
                .viewClicks
                .clicks()
                .autoDispose(scope())
                .subscribe { subjectTabClicks.onNext(index) }
        }

        subjectTabClicks
            .take(1)
            .delay(250, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribe { index ->
                scrollToPage(index)
            }

        subjectTabClicks
            .skip(1)
            .distinctUntilChanged()
            .autoDispose(scope())
            .subscribe { index ->
                scrollToPage(index)
            }

        configApp
            .subjectExploreClicks
            .filter { it != -1L }
            .filter { binding.viewPager.currentItem != 0 }
            .autoDispose(scope())
            .subscribe { _ ->
                subjectTabClicks.onNext(0)
            }

        prefs
            .isUpgraded
            .asObservable()
            .filter { isUpgraded -> isUpgraded && !prefs.isShowedWaringPremiumDialog.get() }
            .delay(1, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribe { isUpgraded ->
                when {
                    isUpgraded && !prefs.isShowedWaringPremiumDialog.get() -> warningPremiumDialog.show(this)
                }
            }

        Observable
            .interval(1, TimeUnit.SECONDS)
            .filter { prefs.isUpgraded.get() }
            .map { prefs.timeExpiredIap.get() }
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
                        prefs.timeExpiredIap.delete()
                    }
                }

                Timber.tag("Main12345").e("Date: $days --- $hours:$minutes:$seconds")
            }
    }

    private fun scrollToPage(index: Int) {
        if (tabIndex != index){
            bottomTabs.getOrNull(tabIndex)?.viewHide?.animShowTop()
            bottomTabs.getOrNull(tabIndex)?.viewShow?.animHideBottom()
        }

        bottomTabs.getOrNull(index)?.viewHide?.animHideTop()
        bottomTabs.getOrNull(index)?.viewShow?.animShowBottom()
        bottomTabs.getOrNull(index)?.let { binding.viewBottom.viewDividerTab.animToCenterView(it.viewClicks, it.viewShow.width) }

        binding.viewPager.currentItem = index
        tabIndex = index
    }

    private fun initView() {
        binding.viewPager.apply {
            this.adapter = LsPageAdapter(supportFragmentManager).apply {
                this.addFragment(fragments = fragments.toTypedArray())
            }
            this.offscreenPageLimit = this.adapter?.count ?: 0
        }
    }

    private data class Tab(val viewClicks: View, val viewHide: View, val viewShow: View)
//    private fun ActivityMainBinding.initTabBottom() = listOf(viewBottom.initTabArt(), viewBottom.initTabBatch(), viewBottom.initTabDiscover(), viewBottom.initTabMine())
//private fun ActivityMainBinding.initTabBottom() = listOf(viewBottom.initTabArt(), viewBottom.initTabBatch(), viewBottom.initTabMine())
    private fun ActivityMainBinding.initTabBottom() = listOf(viewBottom.initTabArt(), viewBottom.initTabMine())
    private fun LayoutBottomMainBinding.initTabArt() = Tab(viewTab1, imageTab1, textTab1)
    private fun LayoutBottomMainBinding.initTabBatch() = Tab(viewTab2, imageTab2, textTab2)
    private fun LayoutBottomMainBinding.initTabDiscover() = Tab(viewTab3, imageTab3, textTab3)
    private fun LayoutBottomMainBinding.initTabMine() = Tab(viewTab4, imageTab4, textTab4)
    private fun View.animHideTop() { animate().translationY(-getDimens(com.intuit.sdp.R.dimen._30sdp)).alpha(0f).setDuration(100).start() }
    private fun View.animShowTop() { animate().translationY(0f).alpha(1f).setDuration(100).start() }
    private fun View.animHideBottom() { animate().translationY(getDimens(com.intuit.sdp.R.dimen._30sdp)).alpha(0f).setDuration(100).start() }
    private fun View.animShowBottom() { animate().translationY(0f).alpha(1f).setDuration(100).start() }
    private fun View.animToCenterView(centerView: View, newWidth: Int) {
        val newLayoutParams = this.layoutParams
        ValueAnimator.ofInt(width, newWidth).apply {
            this.duration = 100
            this.addUpdateListener {
                val value = it.animatedValue as Int
                newLayoutParams.width = value
                this@animToCenterView.layoutParams = newLayoutParams
            }
            this.start()
        }

        val endX = centerView.x + centerView.width / 2 - newWidth / 2
        animate().translationX(endX).setDuration(100).start()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        when {
            App.app.reviewInfo != null -> {
                val flow = App.app.manager.launchReviewFlow(this, App.app.reviewInfo!!)
                flow.addOnCompleteListener { task2 ->
                    if (task2.isSuccessful){

                        finish()
                    } else {
                        finish()
                    }
                }
            }
            else -> finish()
        }
    }

}