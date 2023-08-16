package com.sola.anime.ai.generator.feature.main

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.basic.common.base.LsActivity
import com.basic.common.base.LsPageAdapter
import com.basic.common.extension.*
import com.jakewharton.rxbinding2.view.clicks
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.getCustomerInfoWith
import com.sola.anime.ai.generator.BuildConfig
import com.sola.anime.ai.generator.common.App
import com.sola.anime.ai.generator.common.ConfigApp
import com.sola.anime.ai.generator.common.Constraint
import com.sola.anime.ai.generator.common.ui.dialog.FeatureVersionDialog
import com.sola.anime.ai.generator.common.ui.dialog.WarningPremiumDialog
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.databinding.ActivityMainBinding
import com.sola.anime.ai.generator.databinding.LayoutBottomMainBinding
import com.sola.anime.ai.generator.domain.manager.AdmobManager
import com.sola.anime.ai.generator.domain.repo.ServerApiRepository
import com.sola.anime.ai.generator.feature.main.mine.MineFragment
import com.sola.anime.ai.generator.feature.main.batch.BatchFragment
import com.sola.anime.ai.generator.feature.main.discover.DiscoverFragment
import com.sola.anime.ai.generator.feature.main.explore.ExploreFragment
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : LsActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {

    @Inject lateinit var configApp: ConfigApp
    @Inject lateinit var prefs: Preferences
    @Inject lateinit var admobManager: AdmobManager
    @Inject lateinit var warningPremiumDialog: WarningPremiumDialog
    @Inject lateinit var serverApiRepo: ServerApiRepository
    @Inject lateinit var featureVersionDialog: FeatureVersionDialog

    private val fragments by lazy { listOf(ExploreFragment(), BatchFragment(), DiscoverFragment(), MineFragment()) }
    private val bottomTabs by lazy { binding.initTabBottom() }
    private val subjectTabClicks: Subject<Int> = BehaviorSubject.createDefault(0) // Default tab home
    private var tabIndex = 0 // Default tab home

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transparent()
        setContentView(binding.root)

        when {
            !prefs.isUpgraded.get() -> admobManager.loadRewardCreate()
        }

        when {
            !prefs.isSyncUserPurchased.get() && Purchases.isConfigured -> {
                syncUserPurchased()
            }
        }

        when {
            configApp.version > BuildConfig.VERSION_CODE && !prefs.isShowFeatureDialog(configApp.version).get() -> {
                featureVersionDialog.show(this, configApp.version, configApp.feature)
            }
        }

        initView()
        initObservable()
    }

    private fun syncUserPurchased() {
        Purchases.sharedInstance.getCustomerInfoWith { customerInfo ->
            val isActive = customerInfo.entitlements["premium"]?.isActive ?: false
            Timber.tag("Main12345").e("##### MAIN #####")
            Timber.tag("Main12345").e("Is upgraded: ${prefs.isUpgraded.get()}")
            Timber.tag("Main12345").e("Is active: $isActive")

            if (isActive){
                lifecycleScope.launch(Dispatchers.Main) {
                    serverApiRepo.syncUser { userPremium ->
                        userPremium?.let {
                            if (userPremium.timeExpired == Constraint.Iap.SKU_LIFE_TIME){
                                prefs.isUpgraded.set(true)
                                prefs.timeExpiredPremium.set(-2)
                                return@syncUser
                            }

                            customerInfo
                                .latestExpirationDate
                                ?.takeIf { it.time > System.currentTimeMillis() }
                                ?.let { expiredDate ->
                                    prefs.isUpgraded.set(true)
                                    prefs.timeExpiredPremium.set(expiredDate.time)
                                }
                        }
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

                when (index) {
                    0 -> darkStatusBar()
                    else -> lightStatusBar()
                }
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
            .isPurchasedCredit
            .asObservable()
            .filter { isPurchasedCredit -> isPurchasedCredit && !prefs.isShowedWaringPremiumDialog.get() && !prefs.isUpgraded.get() }
            .delay(1, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
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
            .autoDispose(scope())
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
            this.adapter = LsPageAdapter(supportFragmentManager).apply {
                this.addFragment(fragments = fragments.toTypedArray())
            }
            this.offscreenPageLimit = this.adapter?.count ?: 0
        }
    }

    private data class Tab(val viewClicks: View, val image: ImageView, val display: TextView, val index: Int)
    private fun ActivityMainBinding.initTabBottom() = listOf(viewBottom.initTabExplore(), viewBottom.initTabBatch(), viewBottom.initTabDiscover(), viewBottom.initTabMine())
    private fun LayoutBottomMainBinding.initTabExplore() = Tab(viewClicks = viewTab1, image = imageTab1, display = textTab1, index = 0)
    private fun LayoutBottomMainBinding.initTabBatch() = Tab(viewClicks = viewTab2, image = imageTab2, display = textTab2, index = 1)
    private fun LayoutBottomMainBinding.initTabDiscover() = Tab(viewClicks = viewTab4, image = imageTab4, display = textTab4, index = 2)
    private fun LayoutBottomMainBinding.initTabMine() = Tab(viewClicks = viewTab5, image = imageTab5, display = textTab5, index = 3)

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        when {
            App.app.reviewInfo != null -> {
                tryOrNull {
                    val flow = App.app.manager.launchReviewFlow(this, App.app.reviewInfo!!)
                    flow.addOnCompleteListener { task2 ->
                        if (task2.isSuccessful){

                            finish()
                        } else {
                            finish()
                        }
                    }
                }
            }
            else -> finish()
        }
    }

}