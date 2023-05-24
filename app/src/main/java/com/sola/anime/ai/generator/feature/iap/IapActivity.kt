package com.sola.anime.ai.generator.feature.iap

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.BillingClient
import com.basic.common.base.LsActivity
import com.basic.common.extension.*
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.common.Constraint
import com.sola.anime.ai.generator.common.extension.backTopToBottom
import com.sola.anime.ai.generator.common.extension.startMain
import com.sola.anime.ai.generator.common.util.AutoScrollLayoutManager
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.data.db.query.IapPreviewDao
import com.sola.anime.ai.generator.databinding.ActivityIapBinding
import com.sola.anime.ai.generator.domain.manager.BillingManager
import com.sola.anime.ai.generator.feature.iap.adapter.PreviewAdapter
import com.sola.anime.ai.generator.feature.iap.billing.model.Status
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import java.text.DecimalFormat
import javax.inject.Inject

@AndroidEntryPoint
class IapActivity : LsActivity() {

    companion object {
        const val IS_KILL_EXTRA = "IS_KILL_EXTRA"
    }

    @Inject lateinit var previewAdapter1: PreviewAdapter
    @Inject lateinit var previewAdapter2: PreviewAdapter
    @Inject lateinit var previewAdapter3: PreviewAdapter
    @Inject lateinit var iapPreviewDao: IapPreviewDao
    @Inject lateinit var billingManager: BillingManager
    @Inject lateinit var prefs: Preferences

    private val binding by lazy { ActivityIapBinding.inflate(layoutInflater) }
    private val isKill by lazy { intent.getBooleanExtra(IS_KILL_EXTRA, true) }
    private val subjectSkuChoose: Subject<String> = BehaviorSubject.createDefault(Constraint.Iap.SKU_LIFE_TIME)
    private val pricesWithSku = arrayListOf<PriceFormat>()

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
        iapPreviewDao.getAllLive().observe(this){ data ->
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

        billingManager.init()
    }

    private fun listenerView() {
        binding.back.clicks { onBackPressed() }
        binding.viewLifeTime.clicks { subjectSkuChoose.onNext(Constraint.Iap.SKU_LIFE_TIME) }
        binding.viewWeekly.clicks { subjectSkuChoose.onNext(Constraint.Iap.SKU_WEEK) }
        binding.viewYearly.clicks { subjectSkuChoose.onNext(Constraint.Iap.SKU_YEAR) }
        binding.viewContinue.clicks {
            when (val sku = subjectSkuChoose.blockingFirst()){
                Constraint.Iap.SKU_LIFE_TIME -> billingManager.buy(this, sku, BillingClient.ProductType.INAPP)
                else -> billingManager.buy(this, sku, BillingClient.ProductType.SUBS)
            }
        }
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

                pricesWithSku
                    .find { it.sku == sku
                    }?.let {

                        binding.textDescription.text = it.description

                        binding.textPrice.text = it.priceFormat
                } ?: run {
                    when (sku) {
                        Constraint.Iap.SKU_WEEK -> {
                            binding.textDescription.text = getString(R.string.price_week)
                        }
                        Constraint.Iap.SKU_YEAR -> {
                            binding.textDescription.text = getString(R.string.price_year)
                        }
                        Constraint.Iap.SKU_LIFE_TIME -> {
                            binding.textDescription.text = getString(R.string.price_lifetime)
                        }
                    }
                    binding.textPrice.text = "0đ"
                }
            }

        prefs
            .isUpgraded
            .asObservable()
            .filter { it }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribe { onBackPressed() }

        billingManager
            .subscriptionPrices()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribe { response ->
                when (response.status) {
                    Status.SUCCESS -> {
                        response.data?.forEachIndexed { _, pair ->
                            val priceCurrencyCode = pair.second.priceCurrencyCodes ?: "USD"
                            val priceAmount = pair.second.sumPriceAmountMicros ?: 0

                            val priceFormat = formatPriceWithCurrentCode(priceCurrencyCode, priceAmount.toFloat() / 1000000f)

                            when (pair.first) {
                                Constraint.Iap.SKU_WEEK -> {
                                    when {
                                        priceCurrencyCode == "VND" && pricesWithSku.none { it.sku == pair.first } -> pricesWithSku.add(PriceFormat(getString(R.string.price_week_vnd), priceFormat, pair.first))
                                        pricesWithSku.none { it.sku == pair.first } -> pricesWithSku.add(PriceFormat(getString(R.string.price_week), priceFormat, pair.first))
                                    }
                                }
                                Constraint.Iap.SKU_YEAR -> {
                                    when {
                                        priceCurrencyCode == "VND" && pricesWithSku.none { it.sku == pair.first } -> pricesWithSku.add(PriceFormat(getString(R.string.price_year_vnd), priceFormat, pair.first))
                                        pricesWithSku.none { it.sku == pair.first } -> pricesWithSku.add(PriceFormat(getString(R.string.price_year), priceFormat, pair.first))
                                    }
                                }
                            }
                        }
                    }
                    else -> {}
                }
            }

        billingManager
            .nonConsumablePrices()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribe { response ->
                when (response.status) {
                    Status.SUCCESS -> {
                        response.data?.forEachIndexed { _, pair ->
                            val priceAmount = pair.second.oneTimePurchaseOfferDetails?.priceAmountMicros ?: 0
                            val priceCurrencyCode = pair.second.oneTimePurchaseOfferDetails?.priceCurrencyCode ?: "USD"

                            val priceFormat = formatPriceWithCurrentCode(priceCurrencyCode, priceAmount.toFloat() / 1000000f)

                            when (pair.first) {
                                Constraint.Iap.SKU_LIFE_TIME -> {
                                    when {
                                        priceCurrencyCode == "VND" && pricesWithSku.none { it.sku == pair.first } -> pricesWithSku.add(PriceFormat(getString(R.string.price_lifetime_vnd), priceFormat, pair.first))
                                        pricesWithSku.none { it.sku == pair.first } -> pricesWithSku.add(PriceFormat(getString(R.string.price_lifetime), priceFormat, pair.first))
                                    }

                                    subjectSkuChoose.onNext(pair.first)
                                }
                            }
                        }
                    }
                    else -> {}
                }
            }
    }

    private fun formatPriceWithCurrentCode(priceCurrentCode: String, amount: Float): String {
        if (priceCurrentCode == "VND") {
            val formatter = DecimalFormat("#,###")
            return formatter.format(amount) + "đ"
        }
        return String.format("%.2f", amount) + "$"
    }

    private fun updateWeeklyUi(sku: String) {
        binding.viewWeekly.setCardBackgroundColor(
            when (sku) {
                Constraint.Iap.SKU_WEEK -> resolveAttrColor(android.R.attr.colorAccent)
                else -> resolveAttrColor(android.R.attr.colorBackground)
            }
        )
        binding.imageWeekly.setImageResource(
            when (sku) {
                Constraint.Iap.SKU_WEEK -> R.drawable.ic_circle_check
                else -> R.drawable.circle_stroke_1dp
            }
        )
        binding.imageWeekly.setTint(
            when (sku){
                Constraint.Iap.SKU_WEEK -> resolveAttrColor(com.google.android.material.R.attr.colorOnPrimary)
                else -> resolveAttrColor(android.R.attr.textColorPrimary)
            }
        )
        binding.textWeekly.setTextColor(
            when (sku){
                Constraint.Iap.SKU_WEEK -> resolveAttrColor(com.google.android.material.R.attr.colorOnPrimary)
                else -> resolveAttrColor(android.R.attr.textColorPrimary)
            }
        )
    }

    private fun updateYearlyUi(sku: String) {
        binding.viewYearly.setCardBackgroundColor(
            when (sku) {
                Constraint.Iap.SKU_YEAR -> resolveAttrColor(android.R.attr.colorAccent)
                else -> resolveAttrColor(android.R.attr.colorBackground)
            }
        )
        binding.imageYearly.setImageResource(
            when (sku) {
                Constraint.Iap.SKU_YEAR -> R.drawable.ic_circle_check
                else -> R.drawable.circle_stroke_1dp
            }
        )
        binding.imageYearly.setTint(
            when (sku){
                Constraint.Iap.SKU_YEAR -> resolveAttrColor(com.google.android.material.R.attr.colorOnPrimary)
                else -> resolveAttrColor(android.R.attr.textColorPrimary)
            }
        )
        binding.textYearly.setTextColor(
            when (sku){
                Constraint.Iap.SKU_YEAR -> resolveAttrColor(com.google.android.material.R.attr.colorOnPrimary)
                else -> resolveAttrColor(android.R.attr.textColorPrimary)
            }
        )
    }

    private fun updateLifeTimeUi(sku: String) {
        binding.viewLifeTime.setCardBackgroundColor(
            when (sku) {
                Constraint.Iap.SKU_LIFE_TIME -> resolveAttrColor(android.R.attr.colorAccent)
                else -> resolveAttrColor(android.R.attr.colorBackground)
            }
        )
        binding.imageLifeTime.setImageResource(
            when (sku) {
                Constraint.Iap.SKU_LIFE_TIME -> R.drawable.ic_circle_check
                else -> R.drawable.circle_stroke_1dp
            }
        )
        binding.imageLifeTime.setTint(
            when (sku){
                Constraint.Iap.SKU_LIFE_TIME -> resolveAttrColor(com.google.android.material.R.attr.colorOnPrimary)
                else -> resolveAttrColor(android.R.attr.textColorPrimary)
            }
        )
        binding.textLifeTime.setTextColor(
            when (sku){
                Constraint.Iap.SKU_LIFE_TIME -> resolveAttrColor(com.google.android.material.R.attr.colorOnPrimary)
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

    data class PriceFormat(val description: String, val priceFormat: String, val sku: String)

}