package com.sola.anime.ai.generator.feature.setting

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import com.basic.common.base.LsActivity
import com.basic.common.extension.clicks
import com.basic.common.extension.getDimens
import com.basic.common.extension.isNetworkAvailable
import com.basic.common.extension.lightStatusBar
import com.basic.common.extension.makeToast
import com.basic.common.extension.transparent
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.getCustomerInfoWith
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.common.Constraint
import com.sola.anime.ai.generator.common.Navigator
import com.sola.anime.ai.generator.common.extension.back
import com.sola.anime.ai.generator.common.extension.getStatusBarHeight
import com.sola.anime.ai.generator.common.extension.startIap
import com.sola.anime.ai.generator.common.ui.dialog.NetworkDialog
import com.sola.anime.ai.generator.common.ui.dialog.PromoCodeDialog
import com.sola.anime.ai.generator.common.widget.switchview.LsSwitchView
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.databinding.ActivitySettingBinding
import com.sola.anime.ai.generator.domain.repo.ServerApiRepository
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class SettingActivity : LsActivity<ActivitySettingBinding>(ActivitySettingBinding::inflate) {

    @Inject lateinit var navigator: Navigator
    @Inject lateinit var prefs: Preferences
    @Inject lateinit var promoCodeDialog: PromoCodeDialog
    @Inject lateinit var serverApiRepo: ServerApiRepository
    @Inject lateinit var networkDialog: NetworkDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transparent()
        lightStatusBar()
        setContentView(binding.root)

        initView()
        initObservable()
        initData()
        listenerView()
    }

    private fun listenerView() {
        binding.back.clicks { onBackPressed() }
        binding.viewGetPremium.clicks(withAnim = false) { startIap() }
        binding.viewSupport.clicks(withAnim = false) { navigator.showSupport() }
        binding.viewShare.clicks(withAnim = false) { navigator.showInvite() }
        binding.viewRate.clicks(withAnim = false) { navigator.showRating() }
        binding.viewPrivacy.clicks(withAnim = false) { navigator.showPrivacy() }
        binding.viewTerms.clicks(withAnim = false) { navigator.showTerms() }
//        binding.viewNsfw.clicks(withAnim = false) {
//            when {
//                !prefs.isUpgraded.get() -> startIap()
//                else -> prefs.isEnableNsfw.set(!prefs.isEnableNsfw.get())
//            }
//        }
        binding.viewPromoCode.clicks(withAnim = false) { promoCodeDialog.show(this) }
    }

    private fun initData() {

    }

    private fun initObservable() {
        prefs
            .isUpgraded
            .asObservable()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribe { isUpgraded ->
                binding.viewGetPremium.isVisible = !isUpgraded
            }

//        prefs
//            .isEnableNsfw
//            .asObservable()
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribeOn(AndroidSchedulers.mainThread())
//            .autoDispose(scope())
//            .subscribe { isEnable ->
//                binding.viewNsfw.binding.widgetFrame.findViewById<LsSwitchView>(R.id.switchView).setNewChecked(isEnable)
//            }

        promoCodeDialog
            .confirmClicks
            .autoDispose(scope())
            .subscribe { promoCode ->
                promoCodeDialog.dismiss()

                requestPromoCode(promoCode)
            }
    }

    private fun requestPromoCode(promoCode: String){
        when {
            !isNetworkAvailable() -> networkDialog.show(this){
                lifecycleScope.launch {
                    delay(500)
                    requestPromoCode(promoCode)
                }
            }
            else -> {
                binding.viewLoading.isVisible = true

                lifecycleScope.launch(Dispatchers.Main) {
                    serverApiRepo.promoCode(
                        promoCode = promoCode,
                        success = { isActive, promo ->
                            when {
                                isActive -> {
                                    when (promo) {
                                        Constraint.Promo.USER_PURCHASED_LIFE_TIME -> {
                                            prefs.isUpgraded.set(true)
                                            prefs.timeExpiredPremium.set(-2)

                                            binding.viewLoading.isVisible = false
                                        }
                                        Constraint.Promo.TRY_USER_PURCHASED -> {
//                                            syncUserPurchased {
//                                                binding.viewLoading.isVisible = false
//                                            }
                                        }
                                    }

                                    makeToast("Promo code activation successful!")
                                }
                                else -> makeToast("Promo code has been used, please check again!")
                            }
                        },
                        failed = {
                            binding.viewLoading.isVisible = false

                            makeToast("Promo code could not be found, please check again!")
                        }
                    )
                }
            }
        }
    }

//    private fun syncUserPurchased(done: () -> Unit) {
//        Purchases.sharedInstance.getCustomerInfoWith { customerInfo ->
//            val isActive = customerInfo.entitlements["premium"]?.isActive ?: false
//            Timber.tag("Main12345").e("##### SETTING #####")
//            Timber.tag("Main12345").e("Is upgraded: ${prefs.isUpgraded.get()}")
//            Timber.tag("Main12345").e("Is active: $isActive")
//
//            if (isActive){
//                lifecycleScope.launch(Dispatchers.Main) {
//                    serverApiRepo.syncUser { userPremium ->
//                        userPremium?.let {
//                            if (userPremium.timeExpired == Constraint.Iap.SKU_LIFE_TIME){
//                                prefs.isUpgraded.set(true)
//                                prefs.timeExpiredPremium.set(-2)
//
//                                done()
//                                return@syncUser
//                            }
//
//                            customerInfo
//                                .latestExpirationDate
//                                ?.takeIf { it.time > System.currentTimeMillis() }
//                                ?.let { expiredDate ->
//                                    prefs.isUpgraded.set(true)
//                                    prefs.timeExpiredPremium.set(expiredDate.time)
//                                }
//
//                            done()
//                        }
//                    }
//                }
//            } else done()
//        }
//    }

    private fun initView() {
        binding.viewTop.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            this.topMargin = when(val statusBarHeight = getStatusBarHeight()) {
                0 -> getDimens(com.intuit.sdp.R.dimen._30sdp).toInt()
                else -> statusBarHeight
            }
        }
    }

    @Deprecated("Deprecated in Java", ReplaceWith("finish()"))
    override fun onBackPressed() {
        back()
    }

}