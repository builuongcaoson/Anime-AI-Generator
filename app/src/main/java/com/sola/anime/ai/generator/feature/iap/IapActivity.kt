package com.sola.anime.ai.generator.feature.iap

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.basic.common.base.LsActivity
import com.basic.common.extension.*
import com.revenuecat.purchases.*
import com.revenuecat.purchases.interfaces.GetStoreProductsCallback
import com.revenuecat.purchases.models.StoreProduct
import com.sola.anime.ai.generator.BuildConfig
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.common.App
import com.sola.anime.ai.generator.common.ConfigApp
import com.sola.anime.ai.generator.common.Constraint
import com.sola.anime.ai.generator.common.Navigator
import com.sola.anime.ai.generator.common.extension.*
import com.sola.anime.ai.generator.common.ui.dialog.NetworkDialog
import com.sola.anime.ai.generator.common.util.AutoScrollLayoutManager
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.data.db.query.IAPDao
import com.sola.anime.ai.generator.databinding.ActivityIapBinding
import com.sola.anime.ai.generator.domain.manager.AnalyticManager
import com.sola.anime.ai.generator.domain.manager.UserPremiumManager
import com.sola.anime.ai.generator.domain.repo.ServerApiRepository
import com.sola.anime.ai.generator.feature.iap.adapter.PreviewAdapter
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class IapActivity : LsActivity<ActivityIapBinding>(ActivityIapBinding::inflate) {

    companion object {
        const val IS_KILL_EXTRA = "IS_KILL_EfXTRA"
    }

    @Inject lateinit var previewAdapter1: PreviewAdapter
    @Inject lateinit var previewAdapter2: PreviewAdapter
    @Inject lateinit var previewAdapter3: PreviewAdapter
    @Inject lateinit var iapDao: IAPDao
    @Inject lateinit var prefs: Preferences
    @Inject lateinit var configApp: ConfigApp
    @Inject lateinit var networkDialog: NetworkDialog
    @Inject lateinit var analyticManager: AnalyticManager
    @Inject lateinit var navigator: Navigator
    @Inject lateinit var userPremiumManager: UserPremiumManager

    private val isKill by lazy { intent.getBooleanExtra(IS_KILL_EXTRA, true) }
    private val sku1 by lazy { Constraint.Iap.SKU_LIFE_TIME }
    private val sku2 by lazy { Constraint.Iap.SKU_WEEK }
    private val sku3 by lazy { Constraint.Iap.SKU_YEAR }
    private val subjectSkuChoose: Subject<String> by lazy { BehaviorSubject.createDefault(sku3) }
    private var products = listOf<StoreProduct>()
    private val skus by lazy { listOf(sku1, sku2, sku3) }
    private var skuChoose = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transparent()
        lightStatusBar()
        setContentView(binding.root)

        Timber.tag("MainXXXXXX").e("Iap Start")

        initView()
        initObservable()
        initData()
        listenerView()
    }

    private fun initData() {
        lifecycleScope.launch(Dispatchers.Main) {
            delay(500L)

            iapDao.getAllLive().observeAndRemoveWhenNotEmpty(this@IapActivity){ data ->
                val dataAfterChunked = data.chunked(10)

                previewAdapter1.let { adapter ->
                    adapter.data = dataAfterChunked.getOrNull(0) ?: listOf()
                    adapter.totalCount = adapter.data.size
                    binding.recyclerPreview1.apply {
                        this.post { this.smoothScrollToPosition(adapter.data.size - 1) }
                    }
                }
                previewAdapter2.let { adapter ->
                    adapter.data = dataAfterChunked.getOrNull(1) ?: listOf()
                    adapter.totalCount = adapter.data.size
                    binding.recyclerPreview2.apply {
                        this.post { this.smoothScrollToPosition(adapter.data.size - 1) }
                    }
                }
                previewAdapter3.let { adapter ->
                    adapter.data = dataAfterChunked.getOrNull(2) ?: listOf()
                    adapter.totalCount = adapter.data.size
                    binding.recyclerPreview3.apply {
                        this.post { this.smoothScrollToPosition(adapter.data.size - 1) }
                    }
                }
            }

            delay(250L)

            binding.viewPreview.animate().alpha(1f).setDuration(250L).start()
        }
    }

    private fun syncQueryProduct() {
        networkDialog.dismiss()

        when {
            isNetworkAvailable() -> {
                Purchases.sharedInstance.getProducts(skus, object: GetStoreProductsCallback {
                    override fun onError(error: PurchasesError) {

                    }

                    override fun onReceived(storeProducts: List<StoreProduct>) {
                        products = storeProducts

                        storeProducts.forEach { product ->
                            when {
                                product.id.contains(Constraint.Iap.SKU_YEAR) -> {
                                    binding.priceYear.text = "${formatPrice(product.price.currencyCode, product.price.amountMicros / 1000000f)} per year"
                                    binding.priceWeekOfYear.text = "${formatPrice(product.price.currencyCode, product.price.amountMicros / 1000000f / 52f)}/week"
                                }
                                product.id.contains(Constraint.Iap.SKU_WEEK) -> {
                                    binding.priceWeek.text = "${formatPrice(product.price.currencyCode, product.price.amountMicros / 1000000f)} per week"
                                }
                                product.id.contains(Constraint.Iap.SKU_LIFE_TIME) -> {
                                    binding.priceLifeTime.text = formatPrice(product.price.currencyCode, product.price.amountMicros / 1000000f)
                                }
                            }
                        }
                        updateDescriptionCredits(subjectSkuChoose.blockingFirst())
                    }
                })
            }
            else -> {
                lifecycleScope.launch(Dispatchers.Main) {
                    delay(500L)

                    networkDialog.show(this@IapActivity){
                        syncQueryProduct()
                    }
                }
            }
        }
    }

    private fun formatPrice(priceCurrencyCode: String, priceValue: Float): String {
        val locale = Locale.getDefault()
        val currency = Currency.getInstance(priceCurrencyCode)
        val numberFormat = NumberFormat.getCurrencyInstance(locale).apply {
            this.currency = currency
        }
        val formattedPrice = numberFormat.format(priceValue)
        return if (!formattedPrice.contains(",")) {
            formattedPrice.replace('\u00A0', ' ')
        } else {
            formattedPrice.replace('\u00A0', ' ').replace("\\..*".toRegex(), "")
        }
    }

    private fun updateDescriptionCredits(sku: String) {
        val description = when (sku) {
            Constraint.Iap.SKU_LIFE_TIME -> ""
            Constraint.Iap.SKU_WEEK -> getString(R.string.billed_weekly_auto_renewable_cancel_anytime)
            else -> getString(R.string.billed_yearly_auto_renewable_cancel_anytime)
        }
        binding.description.text = description

        val description3 = when (sku) {
            Constraint.Iap.SKU_LIFE_TIME -> "Get 2000 credits"
            Constraint.Iap.SKU_WEEK -> "Get 200 credits"
            else -> "Get 1000 credits"
        }
        binding.description3.text = description3

    }

    private fun listenerView() {
        binding.back.clicks(withAnim = false) { onBackPressed() }
        binding.viewYear.clicks(withAnim = false) { subjectSkuChoose.onNext(sku3) }
        binding.viewWeek.clicks(withAnim = false) { subjectSkuChoose.onNext(sku2) }
        binding.viewLifetime.clicks(withAnim = false) { subjectSkuChoose.onNext(sku1) }
        binding.viewContinue.clicks(withAnim = false) { purchaseClicks() }
        binding.privacy.makeLinks(
            isUnderlineText = false,
            "Privacy Policy" to View.OnClickListener { navigator.showPrivacy() },
            "Terms of Use" to View.OnClickListener { navigator.showTerms() },
            "Restore" to View.OnClickListener { restoreClicks() }
        )
    }

    private fun restoreClicks() {
        analyticManager.logEvent(AnalyticManager.TYPE.RESTORE_CLICKED)

        lifecycleScope.launch(Dispatchers.Main) {
            binding.viewLoading.isVisible = true
            userPremiumManager.syncUserPurchasedFromDatabase()
            binding.viewLoading.isVisible = false
            makeToast("Restore success!")
        }
    }

    private fun purchaseClicks() {
        when {
            !isNetworkAvailable() -> networkDialog.show(this){
                networkDialog.dismiss()

                initData()
            }
            else -> {
                products.find { it.id.contains(subjectSkuChoose.blockingFirst()) }?.let { product ->
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
            onSuccess = { purchase, customerInfo ->
                val orderId = when {
//                    BuildConfig.DEBUG -> "ABC"
                    else -> purchase?.orderId ?: run {
                        analyticManager.logEvent(AnalyticManager.TYPE.PURCHASE_CANCEL)

                        binding.viewLoading.isVisible = false
                        return@purchaseWith
                    }
                }

                if (orderId.isEmpty() || !orderId.contains("GPA.") || orderId.length < 24) {
                    analyticManager.logEvent(AnalyticManager.TYPE.PURCHASE_CANCEL)

                    binding.viewLoading.isVisible = false
                    return@purchaseWith
                }

                Timber.e("Order id: $orderId")

                prefs.purchasedOrderLastedId.set(orderId)

                val timeExpiredWithPremium = customerInfo
                    .latestExpirationDate
                    ?.takeIf { it.time > System.currentTimeMillis() }?.time ?: 0

                val timeExpired = when {
                    item.id.contains(Constraint.Iap.SKU_LIFE_TIME) -> -2L
                    item.id.contains(Constraint.Iap.SKU_WEEK) -> timeExpiredWithPremium
                    item.id.contains(Constraint.Iap.SKU_YEAR) -> timeExpiredWithPremium
                    else -> -1L
                }

                val creditsReceived = when {
                    item.id.contains(Constraint.Iap.SKU_LIFE_TIME) -> 2000
                    item.id.contains(Constraint.Iap.SKU_WEEK) -> 200
                    item.id.contains(Constraint.Iap.SKU_YEAR) -> 1000
                    else -> 0
                }

                prefs.setCredits(prefs.getCredits() + creditsReceived)
                prefs.timeExpiredPremium.set(timeExpired)
                prefs.isShowedWaringPremiumDialog.delete()
                prefs.numberCreatedArtwork.delete()
                prefs.isUpgraded.set(true)

                binding.viewLoading.isVisible = false

                analyticManager.logEvent(AnalyticManager.TYPE.PURCHASE_SUCCESS)
            },
            onError = { _, _ ->
                analyticManager.logEvent(AnalyticManager.TYPE.PURCHASE_CANCEL)

                binding.viewLoading.isVisible = false
            })
    }

    private fun initObservable() {
        subjectSkuChoose
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribe { sku ->
                if (skuChoose == sku){
                    purchaseClicks()
                    return@subscribe
                }

                skuChoose = sku

                val colorSelected = resolveAttrColor(android.R.attr.textColorPrimary)
                val colorUnselected = Color.TRANSPARENT
                val textColorSelected = resolveAttrColor(com.google.android.material.R.attr.colorOnPrimary)
                val textColorUnselected = resolveAttrColor(android.R.attr.textColorPrimary)

                binding.viewYear.setCardBackgroundColor(if (sku == Constraint.Iap.SKU_YEAR) colorSelected else colorUnselected)
                binding.viewWeek.setCardBackgroundColor(if (sku == Constraint.Iap.SKU_WEEK) colorSelected else colorUnselected)
                binding.viewLifetime.setCardBackgroundColor(if (sku == Constraint.Iap.SKU_LIFE_TIME) colorSelected else colorUnselected)
                binding.checkboxYear.setTint(if (sku == Constraint.Iap.SKU_YEAR) textColorSelected else textColorUnselected)
                binding.checkboxWeek.setTint(if (sku == Constraint.Iap.SKU_WEEK) textColorSelected else textColorUnselected)
                binding.checkboxLifetime.setTint(if (sku == Constraint.Iap.SKU_LIFE_TIME) textColorSelected else textColorUnselected)
                binding.checkboxYear.setBackgroundTint(if (sku == Constraint.Iap.SKU_YEAR) textColorSelected else textColorUnselected)
                binding.checkboxWeek.setBackgroundTint(if (sku == Constraint.Iap.SKU_WEEK) textColorSelected else textColorUnselected)
                binding.checkboxLifetime.setBackgroundTint(if (sku == Constraint.Iap.SKU_LIFE_TIME) textColorSelected else textColorUnselected)
                binding.checkboxYear.setImageDrawable(if (sku == Constraint.Iap.SKU_YEAR) ContextCompat.getDrawable(this, R.drawable.circle) else null)
                binding.checkboxWeek.setImageDrawable(if (sku == Constraint.Iap.SKU_WEEK) ContextCompat.getDrawable(this, R.drawable.circle) else null)
                binding.checkboxLifetime.setImageDrawable(if (sku == Constraint.Iap.SKU_LIFE_TIME) ContextCompat.getDrawable(this, R.drawable.circle) else null)
                binding.yearly.setTextColor(if (sku == Constraint.Iap.SKU_YEAR) textColorSelected else textColorUnselected)
                binding.priceYear.setTextColor(if (sku == Constraint.Iap.SKU_YEAR) textColorSelected else textColorUnselected)
                binding.priceWeekOfYear.setTextColor(if (sku == Constraint.Iap.SKU_YEAR) textColorSelected else textColorUnselected)
                binding.weekly.setTextColor(if (sku == Constraint.Iap.SKU_WEEK) textColorSelected else textColorUnselected)
                binding.priceWeek.setTextColor(if (sku == Constraint.Iap.SKU_WEEK) textColorSelected else textColorUnselected)
                binding.bestSeller.setTextColor(if (sku == Constraint.Iap.SKU_WEEK) textColorSelected else textColorUnselected)
                binding.lifetime.setTextColor(if (sku == Constraint.Iap.SKU_LIFE_TIME) textColorSelected else textColorUnselected)
                binding.priceLifeTime.setTextColor(if (sku == Constraint.Iap.SKU_LIFE_TIME) textColorSelected else textColorUnselected)
                binding.textLifeTime.setTextColor(if (sku == Constraint.Iap.SKU_LIFE_TIME) textColorSelected else textColorUnselected)

                updateDescriptionCredits(sku)
            }

        prefs
            .isUpgraded
            .asObservable()
            .filter { it }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribe { onBackPressed() }

        App
            .app
            .subjectNetworkChanges
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribe { syncQueryProduct() }
    }

    private fun initView() {
        binding.recyclerPreview1.apply {
            this.layoutManager = LinearLayoutManager(this@IapActivity, LinearLayoutManager.HORIZONTAL, false)
            this.adapter = previewAdapter1
        }
        binding.recyclerPreview2.apply {
            this.layoutManager =  LinearLayoutManager(this@IapActivity, LinearLayoutManager.HORIZONTAL, false).apply {
                this.reverseLayout = true
            }
            this.adapter = previewAdapter2
        }
        binding.recyclerPreview3.apply {
            this.layoutManager =  LinearLayoutManager(this@IapActivity, LinearLayoutManager.HORIZONTAL, false)
            this.adapter = previewAdapter3
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (!isKill){
            startMain(viewLoadingAds = binding.viewLoadingAds, isFull = false) { finish() }
        } else {
            backTopToBottom()
        }
    }

}