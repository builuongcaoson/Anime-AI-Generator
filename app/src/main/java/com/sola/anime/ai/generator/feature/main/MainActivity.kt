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
import com.sola.anime.ai.generator.common.App
import com.sola.anime.ai.generator.common.ConfigApp
import com.sola.anime.ai.generator.common.Constraint
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.databinding.ActivityMainBinding
import com.sola.anime.ai.generator.databinding.LayoutBottomMainBinding
import com.sola.anime.ai.generator.domain.manager.AdmobManager
import com.sola.anime.ai.generator.feature.main.batch.BatchFragment
import com.sola.anime.ai.generator.feature.main.discover.DiscoverFragment
import com.sola.anime.ai.generator.feature.main.mine.MineFragment
import com.sola.anime.ai.generator.feature.main.art.ArtFragment
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import dagger.hilt.android.AndroidEntryPoint
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

//    private val fragments by lazy { listOf(ArtFragment(), BatchFragment(), DiscoverFragment(), MineFragment()) }
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

        syncUserPurchased()

        initView()
        initObservable()
    }

    @SuppressLint("SimpleDateFormat")
    private fun syncUserPurchased() {
        Purchases.sharedInstance.getCustomerInfoWith { customerInfo ->
            customerInfo.entitlements.all.forEach {
                Timber.tag("Main12345").e("Key: ${it.key} --- ${it.value.isActive}")
            }
            customerInfo.latestExpirationDate?.let { date ->
                Timber.tag("Main12345").e("Time Expired: ${SimpleDateFormat("dd/MM/yyyy - hh:mm:ss").format(date)}")
            }
            for (i in customerInfo.allPurchaseDatesByProduct){
                i.value?.let { date ->
                    Timber.tag("Main12345").e("Product: ${i.key} --- Purchased: ${SimpleDateFormat("dd/MM/yyyy - hh:mm:ss").format(date)}")
                }
            }
            val isActive = customerInfo.entitlements["premium"]?.isActive ?: false
            Timber.tag("Main12345").e("Premium is active: $isActive")
            if (isActive){
                customerInfo.allPurchaseDatesByProduct.filter { it.value != null }.takeIf { it.isNotEmpty() }?.maxBy { it.value!! }?.let { map ->
                    if (map.value == null) return@let

                    val latestPurchasedProduct = map.key
                    val timeExpired = when {
                        prefs.isUpgraded.get() -> when {
                            latestPurchasedProduct.contains(Constraint.Iap.SKU_WEEK) -> 604800016L // 1 Week
                            latestPurchasedProduct.contains(Constraint.Iap.SKU_MONTH) -> 2629800000L // 1 Month
                            latestPurchasedProduct.contains(Constraint.Iap.SKU_YEAR) -> 31557600000L // 1 Year
                            else -> 21600000L // 6 Hour
                        }
                        else -> 21600000L // 6 Hour
                    }

                    val isUpgraded = Date().time - map.value!!.time <= timeExpired
                    prefs.isUpgraded.set(isUpgraded)
                    prefs.timeExpiredIap.delete()

                    Timber.tag("Main12345").e("Time Purchased: ${SimpleDateFormat("dd/MM/yyyy - hh:mm:ss").format(map.value!!)} --- isUpgraded: $isUpgraded")
                }

                when {
                    prefs.isUpgraded.get() && !DateUtils.isToday(prefs.latestTimeCreatedArtwork.get()) -> {
                        prefs.numberCreatedArtwork.delete()
                        prefs.latestTimeCreatedArtwork.delete()
                    }
                }
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