package com.sola.anime.ai.generator.domain.model.config.userPurchased

import androidx.annotation.Keep
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.util.Locale

@Keep
class UserPurchased {
    @SerializedName("locale")
    @Expose
    var locale: String = Locale.getDefault().country
    @SerializedName("deviceId")
    @Expose
    var deviceId: String = ""
    @SerializedName("deviceModel")
    @Expose
    var deviceModel: String = ""
    @SerializedName("credits")
    @Expose
    var credits: Float = 0f
    @SerializedName("number_created_artwork")
    @Expose
    var numberCreatedArtwork: Long = 0
    @SerializedName("lasted_time_created_artwork")
    @Expose
    var latestTimeCreatedArtwork: String = ""
    @SerializedName("products_purchased")
    @Expose
    var productsPurchased: ArrayList<ProductPurchased?> = arrayListOf()
}

@Keep
class ProductPurchased {
    @SerializedName("package_purchased")
    @Expose
    var packagePurchased: String = ""
    @SerializedName("time_purchased")
    @Expose
    var timePurchased: String = ""
    @SerializedName("time_expired")
    @Expose
    var timeExpired: String = ""
}