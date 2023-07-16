package com.sola.anime.ai.generator.feature.iap

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.format.DateUtils
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.basic.common.base.LsActivity
import com.basic.common.extension.*
import com.revenuecat.purchases.*
import com.revenuecat.purchases.interfaces.GetStoreProductsCallback
import com.revenuecat.purchases.models.StoreProduct
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.common.ConfigApp
import com.sola.anime.ai.generator.common.Constraint
import com.sola.anime.ai.generator.common.extension.backTopToBottom
import com.sola.anime.ai.generator.common.extension.startMain
import com.sola.anime.ai.generator.common.ui.dialog.FeatureDialog
import com.sola.anime.ai.generator.common.ui.dialog.NetworkDialog
import com.sola.anime.ai.generator.common.util.AutoScrollLayoutManager
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.data.db.query.IAPDao
import com.sola.anime.ai.generator.databinding.ActivityIapBinding
import com.sola.anime.ai.generator.domain.manager.AnalyticManager
import com.sola.anime.ai.generator.domain.repo.ServerApiRepository
import com.sola.anime.ai.generator.feature.iap.adapter.PreviewAdapter
import com.sola.anime.ai.generator.feature.iap.billing.LsBilling
import com.sola.anime.ai.generator.feature.iap.billing.listener.BillingListener
import com.sola.anime.ai.generator.feature.iap.billing.model.DataWrappers
import com.sola.anime.ai.generator.feature.iap.billing.model.Response
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
import javax.inject.Inject

@AndroidEntryPoint
class IapActivity : LsActivity<ActivityIapBinding>(ActivityIapBinding::inflate) {

    companion object {
        const val IS_KILL_EXTRA = "IS_KILL_EXTRA"
    }

    @Inject lateinit var previewAdapter1: PreviewAdapter
    @Inject lateinit var previewAdapter2: PreviewAdapter
    @Inject lateinit var previewAdapter3: PreviewAdapter
    @Inject lateinit var iapDao: IAPDao
    @Inject lateinit var prefs: Preferences
    @Inject lateinit var configApp: ConfigApp
    @Inject lateinit var featureDialog: FeatureDialog
    @Inject lateinit var networkDialog: NetworkDialog
    @Inject lateinit var serverApiRepo: ServerApiRepository
    @Inject lateinit var analyticManager: AnalyticManager

    private val isKill by lazy { intent.getBooleanExtra(IS_KILL_EXTRA, true) }
    private val sku1 by lazy {
        when (configApp.scriptIap) {
            "0" -> Constraint.Iap.SKU_LIFE_TIME
            "1" -> Constraint.Iap.SKU_LIFE_TIME
            else -> Constraint.Iap.SKU_LIFE_TIME
        }
    }
    private val sku2 by lazy {
        when (configApp.scriptIap) {
            "0" -> Constraint.Iap.SKU_WEEK
            "1" -> Constraint.Iap.SKU_WEEK_3D_TRIAl
            else -> Constraint.Iap.SKU_MONTH
        }
    }
    private val sku3 by lazy {
        when (configApp.scriptIap) {
            "0" -> Constraint.Iap.SKU_YEAR
            "1" -> Constraint.Iap.SKU_YEAR
            else -> Constraint.Iap.SKU_YEAR
        }
    }
    private val subjectSkuChoose: Subject<String> by lazy { BehaviorSubject.createDefault(sku1) }
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

    private fun initData() {
        iapDao.getAllLive().observe(this){ data ->
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

        lifecycleScope.launch(Dispatchers.Main) {
            // Query products
            syncQueryProduct()

            delay(500)

            // Sync user purchased
            when {
                !prefs.isSyncUserPurchased.get() && Purchases.isConfigured -> {
                    syncUserPurchased()
                }
            }
        }
    }

    private fun syncUserPurchased() {
        Purchases.sharedInstance.getCustomerInfoWith { customerInfo ->
            val isActive = customerInfo.entitlements["premium"]?.isActive ?: false
            Timber.tag("Main12345").e("##### IAP #####")
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

    private fun syncQueryProduct() {
        Purchases.sharedInstance.getProducts(listOf(sku1, sku2, sku3), object: GetStoreProductsCallback {
            override fun onError(error: PurchasesError) {

            }

            override fun onReceived(storeProducts: List<StoreProduct>) {
                products = storeProducts

                for (i in storeProducts){
                    Timber.e("Title: ${i.title} --- ${i.id} --- ${i.price.formatted} --- ${i.id.contains(Constraint.Iap.SKU_MONTH)}")
                }

                updateUIPrice(subjectSkuChoose.blockingFirst())
            }
        })
    }

    private fun updateUIPrice(sku: String) {
        val namePackage = when (sku) {
            Constraint.Iap.SKU_LIFE_TIME -> ""
            Constraint.Iap.SKU_WEEK -> ""
            Constraint.Iap.SKU_WEEK_3D_TRIAl -> "/Week"
            Constraint.Iap.SKU_MONTH -> ""
            else -> ""
        }
        val description = when (sku) {
            Constraint.Iap.SKU_LIFE_TIME -> getString(R.string.description_price_lifetime)
            Constraint.Iap.SKU_WEEK -> getString(R.string.description_price_week)
            Constraint.Iap.SKU_WEEK_3D_TRIAl -> getString(R.string.description_price_week_3d_trial)
            Constraint.Iap.SKU_MONTH -> getString(R.string.description_price_month)
            else -> getString(R.string.description_price_year)
        }
        val description3 = when (sku) {
            Constraint.Iap.SKU_LIFE_TIME -> "Get 2000 credits"
            Constraint.Iap.SKU_WEEK -> "Get 200 credits"
            Constraint.Iap.SKU_WEEK_3D_TRIAl -> "Get 200 credits"
            Constraint.Iap.SKU_MONTH -> "Get 500 credits"
            else -> "Get 1000 credits"
        }
        binding.textDescription.text = description
        binding.description3.text = description3

        products.find { it.id.contains(sku) }?.let { product ->
            binding.textPrice.text = "${product.price.formatted}$namePackage"
        } ?: run {
            binding.textPrice.text = "0 $$namePackage"
        }
    }

    private fun listenerView() {
        binding.back.clicks { onBackPressed() }
        binding.viewLifeTime.clicks { subjectSkuChoose.onNext(sku1) }
        binding.viewWeekly.clicks { subjectSkuChoose.onNext(sku2) }
        binding.viewYearly.clicks { subjectSkuChoose.onNext(sku3) }
        binding.viewContinue.clicks { purchaseClicks() }
        binding.viewMoreOffers.clicks {
            featureDialog.show(this){
                lifecycleScope.launch(Dispatchers.Main) {
                    featureDialog.dismiss()
                    delay(250)
                    purchaseClicks()
                }
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
                if (purchase == null) {
                    return@purchaseWith
                }

                analyticManager.logEvent(AnalyticManager.TYPE.PURCHASE_SUCCESS)

                binding.viewLoading.isVisible = false

                val timeExpiredWithPremium = customerInfo
                    .latestExpirationDate
                    ?.takeIf { it.time > System.currentTimeMillis() }?.time ?: 0

                val timeExpired = when {
                    item.id.contains(Constraint.Iap.SKU_LIFE_TIME) -> -2L
                    item.id.contains(Constraint.Iap.SKU_WEEK) -> timeExpiredWithPremium
                    item.id.contains(Constraint.Iap.SKU_WEEK_3D_TRIAl) -> timeExpiredWithPremium
                    item.id.contains(Constraint.Iap.SKU_MONTH) -> timeExpiredWithPremium
                    item.id.contains(Constraint.Iap.SKU_YEAR) -> timeExpiredWithPremium
                    else -> -3L
                }

                val creditsReceived = when {
                    item.id.contains(Constraint.Iap.SKU_LIFE_TIME) -> 2000
                    item.id.contains(Constraint.Iap.SKU_WEEK) -> 200
                    item.id.contains(Constraint.Iap.SKU_WEEK_3D_TRIAl) -> 200
                    item.id.contains(Constraint.Iap.SKU_MONTH) -> 500
                    else -> 0
                }

                prefs.setCredits(prefs.getCredits() + creditsReceived)
                prefs.timeExpiredPremium.set(timeExpired)
                prefs.isShowedWaringPremiumDialog.delete()
                prefs.numberCreatedArtwork.delete()
                prefs.isSyncUserPurchased.set(true)
                prefs.isUpgraded.set(true)
            },
            onError = { _, _ ->
                analyticManager.logEvent(AnalyticManager.TYPE.PURCHASE_CANCEL)

                binding.viewLoading.isVisible = false
            })
    }

    override fun onResume() {
        registerScrollListener()
        super.onResume()
    }

    override fun onDestroy() {
        unregisterScrollListener()
        super.onDestroy()
    }

    private fun registerScrollListener(){
        binding.recyclerPreview1.addOnScrollListener(scrollListener)
        binding.recyclerPreview2.addOnScrollListener(scrollListener)
        binding.recyclerPreview3.addOnScrollListener(scrollListener)
    }

    private fun unregisterScrollListener(){
        binding.recyclerPreview1.removeOnScrollListener(scrollListener)
        binding.recyclerPreview2.removeOnScrollListener(scrollListener)
        binding.recyclerPreview3.removeOnScrollListener(scrollListener)
    }

    private val scrollListener = object: RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            tryOrNull {
                val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return@tryOrNull

                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val pastVisibleItems = layoutManager.findFirstVisibleItemPosition()

                tryOrNull {
                    if (pastVisibleItems + visibleItemCount >= totalItemCount) {
                        when (recyclerView) {
                            binding.recyclerPreview1 -> {
                                tryOrNull { recyclerView.post { previewAdapter1.insert() } }
                            }
                            binding.recyclerPreview2 -> {
                                tryOrNull { recyclerView.post { previewAdapter2.insert() } }
                            }
                            binding.recyclerPreview3 -> {
                                tryOrNull { recyclerView.post { previewAdapter3.insert() } }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun initObservable() {
        subjectSkuChoose
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribe { sku ->
                updateLifeTimeUi(sku)
                updateWeeklyUi(sku)
                updateYearlyUi(sku)

                updateUIPrice(sku)
            }

        prefs
            .isUpgraded
            .asObservable()
            .filter { it }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribe { onBackPressed() }
    }

    private fun updateWeeklyUi(sku: String) {
        binding.viewWeekly.setCardBackgroundColor(
            when (sku) {
                sku2 -> resolveAttrColor(android.R.attr.colorAccent)
                else -> resolveAttrColor(android.R.attr.colorBackground)
            }
        )
        binding.imageWeekly.setImageResource(
            when (sku) {
                sku2 -> R.drawable.ic_circle_check
                else -> R.drawable.circle_stroke_1dp
            }
        )
        binding.imageWeekly.setTint(
            when (sku){
                sku2 -> resolveAttrColor(com.google.android.material.R.attr.colorOnPrimary)
                else -> resolveAttrColor(android.R.attr.textColorPrimary)
            }
        )
        binding.textTitle2.setTextColor(
            when (sku){
                sku2 -> resolveAttrColor(com.google.android.material.R.attr.colorOnPrimary)
                else -> resolveAttrColor(android.R.attr.textColorPrimary)
            }
        )
    }

    private fun updateYearlyUi(sku: String) {
        binding.viewYearly.setCardBackgroundColor(
            when (sku) {
                sku3 -> resolveAttrColor(android.R.attr.colorAccent)
                else -> resolveAttrColor(android.R.attr.colorBackground)
            }
        )
        binding.imageYearly.setImageResource(
            when (sku) {
                sku3 -> R.drawable.ic_circle_check
                else -> R.drawable.circle_stroke_1dp
            }
        )
        binding.imageYearly.setTint(
            when (sku){
                sku3 -> resolveAttrColor(com.google.android.material.R.attr.colorOnPrimary)
                else -> resolveAttrColor(android.R.attr.textColorPrimary)
            }
        )
        binding.textTitle3.setTextColor(
            when (sku){
                sku3 -> resolveAttrColor(com.google.android.material.R.attr.colorOnPrimary)
                else -> resolveAttrColor(android.R.attr.textColorPrimary)
            }
        )
    }

    private fun updateLifeTimeUi(sku: String) {
        binding.viewLifeTime.setCardBackgroundColor(
            when (sku) {
                sku1 -> resolveAttrColor(android.R.attr.colorAccent)
                else -> resolveAttrColor(android.R.attr.colorBackground)
            }
        )
        binding.imageLifeTime.setImageResource(
            when (sku) {
                sku1 -> R.drawable.ic_circle_check
                else -> R.drawable.circle_stroke_1dp
            }
        )
        binding.imageLifeTime.setTint(
            when (sku){
                sku1 -> resolveAttrColor(com.google.android.material.R.attr.colorOnPrimary)
                else -> resolveAttrColor(android.R.attr.textColorPrimary)
            }
        )
        binding.textTitle1.setTextColor(
            when (sku){
                sku1 -> resolveAttrColor(com.google.android.material.R.attr.colorOnPrimary)
                else -> resolveAttrColor(android.R.attr.textColorPrimary)
            }
        )
    }

    private fun initView() {
        binding.recyclerPreview1.apply {
            this.layoutManager = AutoScrollLayoutManager(this@IapActivity)
            this.adapter = previewAdapter1
        }
        binding.recyclerPreview2.apply {
            this.layoutManager =  AutoScrollLayoutManager(this@IapActivity).apply {
                this.reverseLayout = true
            }
            this.adapter = previewAdapter2
        }
        binding.recyclerPreview3.apply {
            this.layoutManager =  AutoScrollLayoutManager(this@IapActivity)
            this.adapter = previewAdapter3
        }

        binding.textTitle1.text = when (sku1){
            Constraint.Iap.SKU_LIFE_TIME -> "Lifetime"
            Constraint.Iap.SKU_WEEK -> "Weekly"
            Constraint.Iap.SKU_WEEK_3D_TRIAl -> "Weekly"
            Constraint.Iap.SKU_MONTH -> "Monthly"
            else -> "Yearly"
        }
        binding.textTitle2.text = when (sku2){
            Constraint.Iap.SKU_LIFE_TIME -> "Lifetime"
            Constraint.Iap.SKU_WEEK -> "Weekly"
            Constraint.Iap.SKU_WEEK_3D_TRIAl -> "Weekly"
            Constraint.Iap.SKU_MONTH -> "Monthly"
            else -> "Yearly"
        }
        binding.textTitle3.text = when (sku3){
            Constraint.Iap.SKU_LIFE_TIME -> "Lifetime"
            Constraint.Iap.SKU_WEEK -> "Weekly"
            Constraint.Iap.SKU_WEEK_3D_TRIAl -> "Weekly"
            Constraint.Iap.SKU_MONTH -> "Monthly"
            else -> "Yearly"
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

}