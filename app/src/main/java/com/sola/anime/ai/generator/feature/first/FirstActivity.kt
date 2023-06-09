package com.sola.anime.ai.generator.feature.first

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.text.format.DateUtils
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.lifecycle.lifecycleScope
import com.basic.common.base.LsActivity
import com.basic.common.extension.clicks
import com.basic.common.extension.lightStatusBar
import com.basic.common.extension.transparent
import com.basic.common.extension.tryOrNull
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.getCustomerInfoWith
import com.sola.anime.ai.generator.BuildConfig
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.common.ConfigApp
import com.sola.anime.ai.generator.common.Constraint
import com.sola.anime.ai.generator.common.Navigator
import com.sola.anime.ai.generator.common.extension.back
import com.sola.anime.ai.generator.common.extension.makeLinks
import com.sola.anime.ai.generator.common.extension.startMain
import com.sola.anime.ai.generator.common.extension.startTutorial
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.databinding.ActivityFirstBinding
import com.sola.anime.ai.generator.domain.repo.ServerApiRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class FirstActivity : LsActivity<ActivityFirstBinding>(ActivityFirstBinding::inflate) {

    @Inject lateinit var configApp: ConfigApp
    @Inject lateinit var prefs: Preferences
    @Inject lateinit var navigator: Navigator
    @Inject lateinit var serverApiRepo: ServerApiRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transparent()
        lightStatusBar()
        setContentView(binding.root)

        initView()
        syncUserPurchased()
        initObservable()
        initData()
        listenerView()
    }

    @SuppressLint("SimpleDateFormat")
    private fun syncUserPurchased() {
        Purchases.sharedInstance.getCustomerInfoWith { customerInfo ->
            val isActive = customerInfo.entitlements["premium"]?.isActive ?: false
            Timber.tag("Main12345").e("##### FIRST #####")
            Timber.tag("Main12345").e("Is upgraded: ${prefs.isUpgraded.get()}")
            Timber.tag("Main12345").e("Is active: $isActive")

            if (isActive){
                when {
                    !prefs.isSyncUserPurchased.get() -> {
                        lifecycleScope.launch(Dispatchers.Main) {
                            serverApiRepo.syncUser(appUserId = customerInfo.originalAppUserId) { userPremium ->
                                if (userPremium != null && userPremium.timeExpired == Constraint.Iap.SKU_LIFE_TIME){
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
                    prefs.isSyncUserPurchasedFailed.get() -> {
                        if (configApp.skipSyncPremium){
                            customerInfo
                                .latestExpirationDate
                                ?.takeIf { it.time > System.currentTimeMillis() }
                                ?.let { expiredDate ->
                                    prefs.isUpgraded.set(true)
                                    prefs.timeExpiredPremium.set(expiredDate.time)
                                } ?: run {
                                prefs.isUpgraded.set(true)
                                prefs.timeExpiredPremium.set(-2)
                            }
                            return@getCustomerInfoWith
                        }

                        customerInfo
                            .allPurchaseDatesByProduct
                            .filter { it.value != null }
                            .filter { it.key.contains(Constraint.Iap.SKU_WEEK) || it.key.contains(
                                Constraint.Iap.SKU_MONTH) ||it.key.contains(Constraint.Iap.SKU_YEAR) }
                            .takeIf { it.isNotEmpty() }
                            ?.maxByOrNull { it.value!! }
                            ?.let { map ->
                                val latestPurchasedProduct = map.key
                                val latestDatePurchased = map.value ?: return@let
                                val expiredDate = customerInfo.getExpirationDateForProductId(latestPurchasedProduct) ?: return@let

                                val expiredDateTime = expiredDate.time

                                val differenceInMillis = expiredDateTime - Date().time
                                if (differenceInMillis > 0){
                                    val days = TimeUnit.MILLISECONDS.toDays(differenceInMillis)
                                    val hours = TimeUnit.MILLISECONDS.toHours(differenceInMillis) % 24
                                    val minutes = TimeUnit.MILLISECONDS.toMinutes(differenceInMillis) % 60
                                    val seconds = TimeUnit.MILLISECONDS.toSeconds(differenceInMillis) % 60

                                    when {
                                        days <= 0 && hours <= 0 && minutes <= 0 && seconds <= 0 -> {
                                            prefs.isUpgraded.delete()
                                            prefs.timeExpiredPremium.delete()
                                        }
                                        else -> {
                                            prefs.isUpgraded.set(true)
                                            prefs.timeExpiredPremium.set(expiredDate.time)
                                        }
                                    }

                                    Timber.tag("Main12345").e("Time Purchased: ${SimpleDateFormat("dd/MM/yyyy - hh:mm:ss").format(latestDatePurchased)}")
                                    Timber.tag("Main12345").e("Time Expired: ${SimpleDateFormat("dd/MM/yyyy - hh:mm:ss").format(expiredDate)}")
                                    Timber.tag("Main12345").e("Date: $days --- $hours:$minutes:$seconds")
                                } else {
                                    prefs.isUpgraded.delete()
                                    prefs.timeExpiredPremium.delete()
                                }
                                Timber.tag("Main12345").e("DifferenceInMillis: $differenceInMillis --- ${latestDatePurchased.time} --- ${Date().time}")
                            }
                        when {
                            prefs.isUpgraded.get() && !DateUtils.isToday(prefs.latestTimeCreatedArtwork.get()) -> {
                                prefs.numberCreatedArtwork.delete()
                            }
                        }
                    }
                }
            } else {
                customerInfo
                    .nonSubscriptionTransactions
                    .find { transaction -> transaction.productIdentifier.contains(Constraint.Iap.SKU_LIFE_TIME) }
                    ?.let {
                        prefs.isUpgraded.set(true)
                        prefs.timeExpiredPremium.set(-2)
                    } ?: run {
                    prefs.isUpgraded.delete()
                    prefs.timeExpiredPremium.delete()
                }
            }
        }
    }

    private fun listenerView() {
        binding.cardStart.clicks(withAnim = true){
            lifecycleScope.launch {
                delay(100)

                prefs.isFirstTime.set(false)
                when {
//                    !prefs.isViewTutorial.get() -> startTutorial()
                    else -> startMain()
                }
                finish()
            }
        }
    }

    private fun initData() {

    }

    private fun initObservable() {

    }

    private fun initView() {
        binding.textPrivacy.makeLinks(
            "Privacy Policy" to View.OnClickListener {
                navigator.showPrivacy()
            },
            "Terms of Use" to View.OnClickListener {
                navigator.showTerms()
            }
        )

        tryOrNull {
            binding.image2.setImageResource(R.drawable.first_preview_zzz1xxx1zzz_2)
        } ?: run {
            binding.image2.setImageResource(R.drawable.place_holder_image)
        }

        listOf(
            binding.viewAnim1,
            binding.viewAnim2,
            binding.viewAnim3,
            binding.viewAnim4,
            binding.viewAnim5,
            binding.viewAnim6,
            binding.viewAnim7,
            binding.viewAnim8,
            binding.viewAnim9
        ).forEachIndexed { index, view ->
            view.animScale(index)
        }
    }

    private fun View.animScale(index: Int) {
        val animation = ObjectAnimator.ofFloat(0.95f, 1.05f)
        animation.apply {
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
            duration = 1000
            interpolator = AccelerateDecelerateInterpolator()
            startDelay = if (index % 2 == 0) 250 else 0
            addUpdateListener {
                val animatedValue = it.animatedValue as Float

                this@animScale.scaleX = animatedValue
                this@animScale.scaleY = animatedValue
            }
        }
        animation.start()
    }

    @Deprecated("Deprecated in Java", ReplaceWith("finish()"))
    override fun onBackPressed() {
        back()
    }

}