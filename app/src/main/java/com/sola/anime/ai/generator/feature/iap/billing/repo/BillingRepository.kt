package com.sola.anime.ai.generator.feature.iap.billing.repo

import android.app.Activity
import com.android.billingclient.api.BillingClient

public abstract class BillingRepository {

    abstract fun init()

    abstract fun disconnect()

    abstract fun buy(activity: Activity, productId: String, @BillingClient.ProductType type: String)

    abstract fun enableDebugLogging(enable: Boolean)

}
