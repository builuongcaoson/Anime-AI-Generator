package com.sola.anime.ai.generator.feature.credit

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.basic.common.base.LsActivity
import com.basic.common.base.LsAdapter
import com.basic.common.extension.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.revenuecat.purchases.PurchaseParams
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.interfaces.GetStoreProductsCallback
import com.revenuecat.purchases.models.StoreProduct
import com.revenuecat.purchases.purchaseWith
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.common.Constraint
import com.sola.anime.ai.generator.common.extension.animateHorizontalShake
import com.sola.anime.ai.generator.common.extension.backTopToBottom
import com.sola.anime.ai.generator.common.extension.startIap
import com.sola.anime.ai.generator.common.extension.startMain
import com.sola.anime.ai.generator.common.ui.dialog.NetworkDialog
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.databinding.ActivityCreditBinding
import com.sola.anime.ai.generator.databinding.ItemPreviewCreditBinding
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
        binding.view4.clicks { subjectSkuChoice.onNext(Constraint.Iap.SKU_CREDIT_1000) }
        binding.view3.clicks { subjectSkuChoice.onNext(Constraint.Iap.SKU_CREDIT_3000) }
        binding.view2.clicks { subjectSkuChoice.onNext(Constraint.Iap.SKU_CREDIT_5000) }
        binding.view1.clicks { subjectSkuChoice.onNext(Constraint.Iap.SKU_CREDIT_10000) }
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

                binding.viewLoading.isVisible = false

                val creditsReceived = when (item.id) {
                    Constraint.Iap.SKU_CREDIT_1000 -> if (prefs.isUpgraded.get()) 1100 else 1000
                    Constraint.Iap.SKU_CREDIT_3000 -> if (prefs.isUpgraded.get()) 3450 else 3150
                    Constraint.Iap.SKU_CREDIT_5000 -> if (prefs.isUpgraded.get()) 6000 else 5500
                    Constraint.Iap.SKU_CREDIT_10000 -> if (prefs.isFirstPurchaseCredits10000.get()) 15000 else if (prefs.isUpgraded.get()) 12000 else 11000
                    else -> 0
                }

                if (item.id == Constraint.Iap.SKU_CREDIT_10000){
                    prefs.isFirstPurchaseCredits10000.set(false)
                }

                prefs.isShowedWaringPremiumDialog.delete()
                prefs.isSyncUserPurchased.set(true)
                prefs.setCredits(prefs.getCredits() + creditsReceived)
            },
            onError = { _, _ ->
                binding.viewLoading.isVisible = false
            })
    }

    override fun onResume() {
        binding.viewPager.registerOnPageChangeCallback(pageChanges)
        initPreviewView()
        initShakingPremiumView()
        super.onResume()
    }

    private fun initShakingPremiumView() {
        Observable
            .interval(3, TimeUnit.SECONDS)
            .filter { !prefs.isUpgraded.get() }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribe {
                binding.viewPremium.apply {
                    this.animateHorizontalShake(50f, repeatCount = 4, duration = 1000L)
                }
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
                    binding.viewPager.currentItem == previewAdapter.data.lastIndex -> binding.viewPager.currentItem = 0
                    else -> binding.viewPager.currentItem = binding.viewPager.currentItem + 1
                }
            }
    }

    override fun onPause() {
        binding.viewPager.unregisterOnPageChangeCallback(pageChanges)
        super.onPause()
    }

    private val pageChanges = object: ViewPager2.OnPageChangeCallback() {
        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
            binding.pageIndicatorView.setSelected(position)
        }
    }

    private fun initView() {
        binding.viewPager.apply {
            this.adapter = previewAdapter
            this.offscreenPageLimit = previewAdapter.data.size
            this.isUserInputEnabled = false
        }
        binding.pageIndicatorView.apply {
            this.count = previewAdapter.data.size
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
            Glide.with(binding.root)
                .load(item)
                .centerCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(binding.preview)
        }

    }

}