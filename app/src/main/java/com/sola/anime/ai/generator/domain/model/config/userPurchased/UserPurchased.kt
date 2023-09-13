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
    var credits: Int = 0
    @SerializedName("numberCreatedArtwork")
    @Expose
    var numberCreatedArtwork: Long = 0
    @SerializedName("latestTimeCreatedArtwork")
    @Expose
    var latestTimeCreatedArtwork: String = ""
    @SerializedName("productsPurchased")
    @Expose
    var productsPurchased: ArrayList<ProductPurchased?> = arrayListOf()
}

@Keep
class ProductPurchased {
    @SerializedName("packagePurchased")
    @Expose
    var packagePurchased: String = ""
    @SerializedName("timePurchased")
    @Expose
    var timePurchased: String = ""
    @SerializedName("timeExpired")
    @Expose
    var timeExpired: String = ""
}