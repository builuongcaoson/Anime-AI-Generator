package com.sola.anime.ai.generator.feature.iap.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.BillingClient
import com.sola.anime.ai.generator.feature.iap.billing.listener.BillingListener
import com.sola.anime.ai.generator.feature.iap.billing.repo.BillingRepository
import com.sola.anime.ai.generator.feature.iap.billing.repo.BillingRepositoryImpl

public class LsBilling constructor(
    context: Context,
    nonConsumableKeys: List<String> = emptyList(),
    consumableKeys: List<String> = emptyList(),
    subscriptionKeys: List<String> = emptyList(),
    billingListener: BillingListener,
    enableLogging: Boolean = true
) {

    private val billingRepo: BillingRepository

    init {
        val contextLocal = context.applicationContext ?: context
        billingRepo = BillingRepositoryImpl(contextLocal, nonConsumableKeys, consumableKeys, subscriptionKeys, billingListener)
        billingRepo.enableDebugLogging(enableLogging)
    }

    fun init(){
        billingRepo.init()
    }

    fun disconnect(){
        billingRepo.disconnect()
    }

    fun enableLogging(enableLogging: Boolean){
        billingRepo.enableDebugLogging(enableLogging)
    }

    fun buy(activity: Activity, productId: String, @BillingClient.ProductType type: String){
        billingRepo.buy(activity, productId, type)
    }

}