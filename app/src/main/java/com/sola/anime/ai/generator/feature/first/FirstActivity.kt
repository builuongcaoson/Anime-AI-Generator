package com.sola.anime.ai.generator.feature.first

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.text.format.DateUtils
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
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
import com.sola.anime.ai.generator.common.extension.*
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.databinding.ActivityFirstBinding
import com.sola.anime.ai.generator.domain.repo.ServerApiRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
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

        when {
            !prefs.isSyncUserPurchased.get() && Purchases.isConfigured -> {
                syncUserPurchased()
            }
        }

        initView()
        initObservable()
        initData()
        listenerView()
    }

    private fun syncUserPurchased() {
        Purchases.sharedInstance.getCustomerInfoWith { customerInfo ->
            val isActive = customerInfo.entitlements["premium"]?.isActive ?: false
            Timber.tag("Main12345").e("##### FIRST #####")
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

    private fun listenerView() {
        binding.cardStart.clicks(withAnim = false){
            lifecycleScope.launch {
                delay(100)

                prefs.isFirstTime.set(false)
                when {
//                    !prefs.isViewTutorial.get() -> startTutorial()
                    !prefs.isUpgraded.get() -> startIap(isKill = false)
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
        tryOrNull { binding.textPrivacy.makeLinks("Privacy Policy" to View.OnClickListener { navigator.showPrivacy() }, "Terms of Use" to View.OnClickListener { navigator.showTerms() }) }

        listOf<Pair<ImageView, Int>>(
            binding.image1 to R.drawable.first_preview_zzz2xxx3zzz_1,
            binding.image2 to R.drawable.first_preview_zzz1xxx1zzz_2,
            binding.image3 to R.drawable.first_preview_zzz4xxx3zzz_3,
            binding.image4 to R.drawable.first_preview_zzz3xxx4zzz_4,
            binding.image5 to R.drawable.first_preview_zzz16xxx9zzz_5,
            binding.image6 to R.drawable.first_preview_zzz1xxx1zzz_6,
            binding.image7 to R.drawable.first_preview_zzz9xxx16zzz_7,
            binding.image8 to R.drawable.first_preview_zzz2xxx3zzz_8
        ).forEach { pair ->
            pair.first.load(pair.second, errorRes = R.drawable.place_holder_image)
        }

        listOf(
            binding.viewAnim1,
            binding.viewAnim2,
            binding.viewAnim3,
            binding.viewAnim4,
            binding.viewAnim5,
            binding.viewAnim6,
            binding.viewAnim7,
            binding.viewAnim8
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