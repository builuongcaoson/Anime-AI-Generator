package com.sola.anime.ai.generator.feature.iap

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.basic.common.base.LsActivity
import com.basic.common.extension.*
import com.google.gson.Gson
import com.revenuecat.purchases.*
import com.revenuecat.purchases.interfaces.GetStoreProductsCallback
import com.revenuecat.purchases.models.StoreProduct
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.common.App
import com.sola.anime.ai.generator.common.ConfigApp
import com.sola.anime.ai.generator.common.Constraint
import com.sola.anime.ai.generator.common.Navigator
import com.sola.anime.ai.generator.common.extension.*
import com.sola.anime.ai.generator.common.ui.dialog.FeatureDialog
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
    @Inject lateinit var navigator: Navigator
    @Inject lateinit var userPremiumManager: UserPremiumManager

    private val isKill by lazy { intent.getBooleanExtra(IS_KILL_EXTRA, true) }
    private val sku1 by lazy { Constraint.Iap.SKU_LIFE_TIME }
    private val sku2 by lazy { Constraint.Iap.SKU_WEEK }
    private val sku3 by lazy { Constraint.Iap.SKU_YEAR }
    private val subjectSkuChoose: Subject<String> by lazy { BehaviorSubject.createDefault(sku3) }
    private var products = listOf<StoreProduct>()
    private val skus by lazy { listOf(sku1, sku2, sku3) }

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

                        updateUIPrice(subjectSkuChoose.blockingFirst())
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

    private fun updateUIPrice(sku: String) {
        val description = when (sku) {
            Constraint.Iap.SKU_LIFE_TIME -> getString(R.string.description_price_lifetime)
            Constraint.Iap.SKU_WEEK -> getString(R.string.description_price_week)
            else -> getString(R.string.description_price_year)
        }
        val description3 = when (sku) {
            Constraint.Iap.SKU_LIFE_TIME -> "Gift 2000 credits"
            Constraint.Iap.SKU_WEEK -> "Gift 200 credits"
            else -> "Gift 1000 credits"
        }
        binding.textDescription.text = description
        binding.description3.text = description3

        products.find { it.id.contains(sku) }?.let { product ->
            binding.textPrice.text = "${product.price.formatted}"
        } ?: run {
            binding.textPrice.text = "0 $"
        }
    }

    private fun listenerView() {
        registerScrollListener()

        binding.back.clicks(withAnim = false) { onBackPressed() }
        binding.viewLifeTime.clicks(withAnim = false) { subjectSkuChoose.onNext(sku1) }
        binding.viewWeekly.clicks(withAnim = false) { subjectSkuChoose.onNext(sku2) }
        binding.viewYearly.clicks(withAnim = false) { subjectSkuChoose.onNext(sku3) }
        binding.viewContinue.clicks(withAnim = false) { purchaseClicks() }
        binding.viewMoreOffers.clicks {
            featureDialog.show(this){
                lifecycleScope.launch(Dispatchers.Main) {
                    featureDialog.dismiss()

                    delay(250)

                    purchaseClicks()
                }
            }
        }
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
                val orderId = purchase?.orderId ?: "null"

                if (orderId.isEmpty() || !orderId.contains("GPA.") || orderId.length < 24) {
                    analyticManager.logEvent(AnalyticManager.TYPE.PURCHASE_CANCEL)

                    binding.viewLoading.isVisible = false
                    return@purchaseWith
                }

                Timber.e("Order id: ${purchase?.orderId}")

                prefs.purchasedOrderLastedId.set(purchase?.orderId ?: "null")

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

    private fun registerScrollListener(){
        binding.recyclerPreview1.addOnScrollListener(scrollListener)
        binding.recyclerPreview2.addOnScrollListener(scrollListener)
        binding.recyclerPreview3.addOnScrollListener(scrollListener)
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

        App
            .app
            .subjectNetworkChanges
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribe { syncQueryProduct() }
    }

    private fun updateWeeklyUi(sku: String) {
        binding.viewWeekly.setCardBackgroundColor(
            when (sku) {
                sku2 -> resolveAttrColor(com.google.android.material.R.attr.colorSecondary)
                else -> resolveAttrColor(R.attr.cardBackgroundColor)
            }
        )
        binding.imageWeekly.setImageResource(
            when (sku) {
                sku2 -> R.drawable.ic_circle_check
                else -> R.drawable.circle_stroke_1dp
            }
        )
    }

    private fun updateYearlyUi(sku: String) {
        binding.viewYearly.setCardBackgroundColor(
            when (sku) {
                sku3 -> resolveAttrColor(com.google.android.material.R.attr.colorSecondary)
                else -> resolveAttrColor(R.attr.cardBackgroundColor)
            }
        )
        binding.imageYearly.setImageResource(
            when (sku) {
                sku3 -> R.drawable.ic_circle_check
                else -> R.drawable.circle_stroke_1dp
            }
        )
    }

    private fun updateLifeTimeUi(sku: String) {
        binding.viewLifeTime.setCardBackgroundColor(
            when (sku) {
                sku1 -> resolveAttrColor(com.google.android.material.R.attr.colorSecondary)
                else -> resolveAttrColor(R.attr.cardBackgroundColor)
            }
        )
        binding.imageLifeTime.setImageResource(
            when (sku) {
                sku1 -> R.drawable.ic_circle_check
                else -> R.drawable.circle_stroke_1dp
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
            else -> "Yearly"
        }
        binding.textTitle2.text = when (sku2){
            Constraint.Iap.SKU_LIFE_TIME -> "Lifetime"
            Constraint.Iap.SKU_WEEK -> "Weekly"
            else -> "Yearly"
        }
        binding.textTitle3.text = when (sku3){
            Constraint.Iap.SKU_LIFE_TIME -> "Lifetime"
            Constraint.Iap.SKU_WEEK -> "Weekly"
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