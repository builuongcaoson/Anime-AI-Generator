package com.sola.anime.ai.generator.data.manager

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import com.google.firebase.analytics.FirebaseAnalytics
import com.sola.anime.ai.generator.common.Constraint
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.domain.manager.BillingManager
import com.sola.anime.ai.generator.feature.iap.billing.LsBilling
import com.sola.anime.ai.generator.feature.iap.billing.listener.BillingListener
import com.sola.anime.ai.generator.feature.iap.billing.model.DataWrappers
import com.sola.anime.ai.generator.feature.iap.billing.model.Response
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingManagerImpl @Inject constructor(
    private val context: Context,
    private val prefs: Preferences,
    private val firebaseAnalytics: FirebaseAnalytics
): BillingManager {

    private val subscriptionKeys by lazy {
        listOf(Constraint.Iap.SKU_WEEK, Constraint.Iap.SKU_YEAR)
    }

    private val nonConsumableKeys by lazy {
        listOf(Constraint.Iap.SKU_LIFE_TIME)
    }

    private lateinit var billing: LsBilling

    private val nonConsumablePrices: Subject<Response<List<Pair<String, DataWrappers.ProductDetail>>>> = BehaviorSubject.createDefault(Response.loading())
    override fun nonConsumablePrices(): Subject<Response<List<Pair<String, DataWrappers.ProductDetail>>>> = nonConsumablePrices

    private val consumablePrices: Subject<Response<List<Pair<String, DataWrappers.ProductDetail>>>> = BehaviorSubject.createDefault(Response.loading())
    override fun consumablePrices(): Subject<Response<List<Pair<String, DataWrappers.ProductDetail>>>> = consumablePrices

    private val subscriptionPrices: Subject<Response<List<Pair<String, DataWrappers.ProductDetail>>>> = BehaviorSubject.createDefault(Response.loading())
    override fun subscriptionPrices(): Subject<Response<List<Pair<String, DataWrappers.ProductDetail>>>> = subscriptionPrices

    override fun init() {
        billing = LsBilling(context, subscriptionKeys = subscriptionKeys, nonConsumableKeys = nonConsumableKeys, billingListener = object: BillingListener {
            override fun onPurchasesUpdated(purchase: Purchase?) {
                purchase?.let {
                    when {
                        purchase.products.joinToString { it }.contains(Constraint.Iap.SKU_WEEK) ->  prefs.isUpgraded.set(true)
                        purchase.products.joinToString { it }.contains(Constraint.Iap.SKU_YEAR) ->  prefs.isUpgraded.set(true)
                        purchase.products.joinToString { it }.contains(Constraint.Iap.SKU_LIFE_TIME) ->  prefs.isUpgraded.set(true)
                        else -> {
                            prefs.isUpgraded.set(false)
                        }
                    }
                } ?: run {
                    prefs.isUpgraded.set(false)
                }
            }

            override fun onPurchasesUpdate(
                billingResult: BillingResult,
                purchaseResults: List<Purchase>?
            ) {
                purchaseResults?.forEach { purchase ->
                    purchase.let {
                        when {
                            purchase.products.joinToString { it }.contains(Constraint.Iap.SKU_WEEK) ->  prefs.isUpgraded.set(true)
                            purchase.products.joinToString { it }.contains(Constraint.Iap.SKU_YEAR) ->  prefs.isUpgraded.set(true)
                            purchase.products.joinToString { it }.contains(Constraint.Iap.SKU_LIFE_TIME) ->  prefs.isUpgraded.set(true)
                            else -> {
                                prefs.isUpgraded.set(false)
                            }
                        }
                    }
                }
            }

            override fun disconnected() {
                Timber.e("disconnected")
            }

            override fun connected() {
                Timber.e("connected")
            }

            override fun failed() {
                Timber.e("failed")
            }

            override fun updateNonConsumablePrices(response: Response<List<Pair<String, DataWrappers.ProductDetail>>>) {
                nonConsumablePrices.onNext(response)
            }

            override fun updateConsumablePrices(response: Response<List<Pair<String, DataWrappers.ProductDetail>>>) {
                consumablePrices.onNext(response)
            }

            override fun updateSubscriptionPrices(response: Response<List<Pair<String, DataWrappers.ProductDetail>>>) {
                subscriptionPrices.onNext(response)
            }
        })

        billing.init()
    }

    override fun buy(activity: Activity, productId: String, @BillingClient.ProductType type: String) {
        billing.buy(activity, productId, type)
    }

}