package com.sola.anime.ai.generator.feature.credit

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.basic.common.base.LsActivity
import com.basic.common.base.LsAdapter
import com.basic.common.extension.*
import com.revenuecat.purchases.PurchaseParams
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.interfaces.GetStoreProductsCallback
import com.revenuecat.purchases.models.StoreProduct
import com.revenuecat.purchases.purchaseWith
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.common.Constraint
import com.sola.anime.ai.generator.common.Navigator
import com.sola.anime.ai.generator.common.extension.*
import com.sola.anime.ai.generator.common.ui.dialog.NetworkDialog
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.databinding.ActivityCreditBinding
import com.sola.anime.ai.generator.databinding.ItemPreviewCreditBinding
import com.sola.anime.ai.generator.domain.manager.AnalyticManager
import com.sola.anime.ai.generator.domain.manager.UserPremiumManager
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
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class CreditActivity : LsActivity<ActivityCreditBinding>(ActivityCreditBinding::inflate) {

    companion object {
        const val IS_KILL_EXTRA = "IS_KILL_EXTRA"
    }

    @Inject lateinit var prefs: Preferences
    @Inject lateinit var networkDialog: NetworkDialog
    @Inject lateinit var previewAdapter: PreviewAdapter
    @Inject lateinit var analyticManager: AnalyticManager
    @Inject lateinit var userPremiumManager: UserPremiumManager
    @Inject lateinit var navigator: Navigator

    private val isKill by lazy { intent.getBooleanExtra(IS_KILL_EXTRA, true) }
    private val subjectSkuChoice: Subject<String> = BehaviorSubject.createDefault(Constraint.Iap.SKU_CREDIT_10000)
    private var products = listOf<StoreProduct>()

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
        binding.viewPremium.clicks { startIap() }
        binding.view4.clicks(withAnim = false) { subjectSkuChoice.onNext(Constraint.Iap.SKU_CREDIT_1000) }
        binding.view3.clicks(withAnim = false) { subjectSkuChoice.onNext(Constraint.Iap.SKU_CREDIT_3000) }
        binding.view2.clicks(withAnim = false) { subjectSkuChoice.onNext(Constraint.Iap.SKU_CREDIT_5000) }
        binding.view1.clicks(withAnim = false) { subjectSkuChoice.onNext(Constraint.Iap.SKU_CREDIT_10000) }
        binding.viewContinue.clicks(withAnim = false) { purchaseClicks() }
    }

    private fun syncQueryProduct() {
        Purchases.sharedInstance.getProducts(listOf(Constraint.Iap.SKU_CREDIT_1000, Constraint.Iap.SKU_CREDIT_3000, Constraint.Iap.SKU_CREDIT_5000, Constraint.Iap.SKU_CREDIT_10000), object:
            GetStoreProductsCallback {
            override fun onError(error: PurchasesError) {

            }

            override fun onReceived(storeProducts: List<StoreProduct>) {
                products = storeProducts

                for (i in storeProducts){
                    Timber.e("Title: ${i.title} --- ${i.id} --- ${i.price.formatted} --- ${i.id.contains(Constraint.Iap.SKU_MONTH)}")
                }

                updateUIPrice()
            }
        })
    }

    private fun updateUIPrice() {
        products.forEach {
            when {
                it.id == Constraint.Iap.SKU_CREDIT_1000 -> binding.price4.text = it.price.formatted
                it.id == Constraint.Iap.SKU_CREDIT_3000 -> binding.price3.text = it.price.formatted
                it.id == Constraint.Iap.SKU_CREDIT_5000 -> binding.price2.text = it.price.formatted
                it.id == Constraint.Iap.SKU_CREDIT_10000 -> binding.price1.text = it.price.formatted
            }
        }
    }

    private fun purchaseClicks() {
        when {
            !isNetworkAvailable() -> networkDialog.show(this){
                networkDialog.dismiss()

                initData()
            }
            else -> {
                products.find { it.id == subjectSkuChoice.blockingFirst() }?.let { product ->
                    purchaseProduct(product)
                } ?: run {
                    makeToast("Something wrong, please try again!")
                }
            }
        }
    }

    private fun purchaseProduct(item: StoreProduct) {
        binding.viewLoading.isVisible = true

        Purchases.sharedInstance.purchaseWith(
            PurchaseParams.Builder(this, item).build(),
            onSuccess = { purchase, _ ->
                if (purchase == null) {
                    return@purchaseWith
                }

                analyticManager.logEvent(AnalyticManager.TYPE.PURCHASE_SUCCESS_CREDITS)

                val creditsReceived = when (item.id) {
                    Constraint.Iap.SKU_CREDIT_1000 -> if (prefs.isUpgraded.get()) 1100 else 1000
                    Constraint.Iap.SKU_CREDIT_3000 -> if (prefs.isUpgraded.get()) 3550 else 3250
                    Constraint.Iap.SKU_CREDIT_5000 -> if (prefs.isUpgraded.get()) 6000 else 5500
                    Constraint.Iap.SKU_CREDIT_10000 -> if (prefs.isFirstPurchaseCredits10000.get()) 15000 else if (prefs.isUpgraded.get()) 12000 else 11000
                    else -> 0
                }

                if (item.id == Constraint.Iap.SKU_CREDIT_10000){
                    prefs.isFirstPurchaseCredits10000.set(false)
                }

                prefs.isShowedWaringPremiumDialog.delete()
                prefs.isPurchasedCredit.set(true)
                prefs.setCredits(prefs.getCredits() + creditsReceived)

                lifecycleScope.launch(Dispatchers.Main) {
                    Timber.e("Sync user purchased")

                    val userPurchased = userPremiumManager.addOrUpdatePurchasedToDatabase(packagePurchased = item.id, timePurchased = purchase.purchaseTime, timeExpired = -3)
                    prefs.setUserPurchased(userPurchased)

                    binding.viewLoading.isVisible = false
                }
            },
            onError = { _, _ ->
                analyticManager.logEvent(AnalyticManager.TYPE.PURCHASE_CANCEL_CREDITS)

                binding.viewLoading.isVisible = false
            })
    }

    override fun onResume() {
        initPreviewView()
        initShakingPremiumView()
        super.onResume()
    }

    private fun initShakingPremiumView() {
        Observable
            .interval(3, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribe {
                binding.viewContinue.animateHorizontalShake(50f, repeatCount = 4, duration = 1000L)
            }
    }

    private fun initPreviewView() {
        Observable
            .interval(2, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribe {
                when {
                    binding.viewLoading.isVisible -> {}
                    binding.viewPager.currentItem == previewAdapter.data.lastIndex -> binding.viewPager.currentItem = 0
                    else -> binding.viewPager.currentItem = binding.viewPager.currentItem + 1
                }
            }
    }

    private fun initView() {
        binding.privacy.makeLinks(
            isUnderlineText = false,
            "Privacy Policy" to View.OnClickListener { navigator.showPrivacy() },
            "Terms of Use" to View.OnClickListener { navigator.showTerms() },
            "Restore" to View.OnClickListener { restoreClicks() }
        )
        binding.viewPager.apply {
            this.adapter = previewAdapter
            this.offscreenPageLimit = previewAdapter.data.size
            this.isUserInputEnabled = false
        }
    }

    private fun restoreClicks() {
        lifecycleScope.launch(Dispatchers.Main) {
            binding.viewLoading.isVisible = true

            userPremiumManager.syncUserPurchasedFromDatabase()

            binding.viewLoading.isVisible = false

            makeToast("Restore success!")
        }
    }

    private fun initData() {
        lifecycleScope.launch(Dispatchers.Main) {
            // Query products
            syncQueryProduct()
        }
    }

    private fun initObservable() {
        subjectSkuChoice
            .autoDispose(scope())
            .subscribe {
//                binding.view4.strokeWidth = if (it == Constraint.Iap.SKU_CREDIT_1000) getDimens(com.intuit.sdp.R.dimen._2sdp).toInt() else 0
//                binding.view3.strokeWidth = if (it == Constraint.Iap.SKU_CREDIT_3000) getDimens(com.intuit.sdp.R.dimen._2sdp).toInt() else 0
//                binding.view2.strokeWidth = if (it == Constraint.Iap.SKU_CREDIT_5000) getDimens(com.intuit.sdp.R.dimen._2sdp).toInt() else 0
//                binding.view1.strokeWidth = if (it == Constraint.Iap.SKU_CREDIT_10000) getDimens(com.intuit.sdp.R.dimen._2sdp).toInt() else 0

                binding.view4.strokeColor = resolveAttrColor(if (it == Constraint.Iap.SKU_CREDIT_1000) android.R.attr.textColorPrimary else android.R.attr.textColorTertiary)
                binding.view3.strokeColor = resolveAttrColor(if (it == Constraint.Iap.SKU_CREDIT_3000) android.R.attr.textColorPrimary else android.R.attr.textColorTertiary)
                binding.view2.strokeColor = resolveAttrColor(if (it == Constraint.Iap.SKU_CREDIT_5000) android.R.attr.textColorPrimary else android.R.attr.textColorTertiary)
                binding.view1.strokeColor = resolveAttrColor(if (it == Constraint.Iap.SKU_CREDIT_10000) android.R.attr.textColorPrimary else android.R.attr.textColorTertiary)

                when {
                    it == Constraint.Iap.SKU_CREDIT_1000 -> {
                        binding.checkbox4.setImageResource(R.drawable.circle)
                        binding.checkbox3.setImageDrawable(null)
                        binding.checkbox2.setImageDrawable(null)
                        binding.checkbox1.setImageDrawable(null)
                    }
                    it == Constraint.Iap.SKU_CREDIT_3000 -> {
                        binding.checkbox4.setImageDrawable(null)
                        binding.checkbox3.setImageResource(R.drawable.circle)
                        binding.checkbox2.setImageDrawable(null)
                        binding.checkbox1.setImageDrawable(null)
                    }
                    it == Constraint.Iap.SKU_CREDIT_5000 -> {
                        binding.checkbox4.setImageDrawable(null)
                        binding.checkbox3.setImageDrawable(null)
                        binding.checkbox2.setImageResource(R.drawable.circle)
                        binding.checkbox1.setImageDrawable(null)
                    }
                    it == Constraint.Iap.SKU_CREDIT_10000 -> {
                        binding.checkbox4.setImageDrawable(null)
                        binding.checkbox3.setImageDrawable(null)
                        binding.checkbox2.setImageDrawable(null)
                        binding.checkbox1.setImageResource(R.drawable.circle)
                    }
                }

                val creditsReceived = when (it) {
                    Constraint.Iap.SKU_CREDIT_1000 -> if (prefs.isUpgraded.get()) 1100 else 1000
                    Constraint.Iap.SKU_CREDIT_3000 -> if (prefs.isUpgraded.get()) 3550 else 3250
                    Constraint.Iap.SKU_CREDIT_5000 -> if (prefs.isUpgraded.get()) 6000 else 5500
                    Constraint.Iap.SKU_CREDIT_10000 -> if (prefs.isFirstPurchaseCredits10000.get()) 15000 else if (prefs.isUpgraded.get()) 12000 else 11000
                    else -> 0
                }

                binding.creditsReceived.text = "Get $creditsReceived Credits"
            }

        prefs
            .isFirstPurchaseCredits10000
            .asObservable()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribe { isFirst ->
                binding.firstPurchase1.isVisible = isFirst
                binding.description1.isVisible = !isFirst
            }
        
        prefs
            .isUpgraded
            .asObservable()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribe { isUpgraded ->
                binding.viewPremium.isVisible = !isUpgraded
            }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (!isKill){
            startMain()
            finish()
        } else {
            backTopToBottom()
        }
    }

    class PreviewAdapter @Inject constructor(): LsAdapter<Int, ItemPreviewCreditBinding>(ItemPreviewCreditBinding::inflate){

        init {
            data = listOf(
                R.drawable.preview_credit_1,
                R.drawable.preview_credit_2,
                R.drawable.preview_credit_3,
                R.drawable.preview_credit_4,
                R.drawable.preview_credit_5
            )
        }

        override fun bindItem(item: Int, binding: ItemPreviewCreditBinding, position: Int) {
            binding.preview.load(item)
        }

    }

}