package com.sola.anime.ai.generator.domain.manager

import android.app.Activity
import com.android.billingclient.api.BillingClient
import com.sola.anime.ai.generator.feature.iap.billing.model.DataWrappers
import com.sola.anime.ai.generator.feature.iap.billing.model.Response
import io.reactivex.Observable

interface BillingManager {

    fun init()

    fun nonConsumablePrices(): Observable<Response<List<Pair<String, DataWrappers.ProductDetail>>>>

    fun consumablePrices(): Observable<Response<List<Pair<String, DataWrappers.ProductDetail>>>>

    fun subscriptionPrices(): Observable<Response<List<Pair<String, DataWrappers.ProductDetail>>>>

    fun buy(activity: Activity, productId: String, @BillingClient.ProductType type: String)

}