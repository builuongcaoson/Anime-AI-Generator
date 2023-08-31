package com.sola.anime.ai.generator.domain.model.config.userPurchased

import androidx.annotation.Keep
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Keep
class UserPurchased {
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
    var lastedTimeCreatedArtwork: String = ""
    @SerializedName("products_purchased")
    @Expose
    var productsPurchased: ArrayList<ProductPurchased> = arrayListOf()
}

@Keep
class ProductPurchased {
    @SerializedName("id")
    @Expose
    var id: Long = 0
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