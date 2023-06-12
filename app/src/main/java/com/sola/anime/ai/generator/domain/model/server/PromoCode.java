package com.sola.anime.ai.generator.domain.model.server;

import androidx.annotation.Keep;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Keep
public class PromoCode {

    @SerializedName("promo")
    @Expose
    public String promo;
    @SerializedName("description")
    @Expose
    public String description;
    @SerializedName("isActive")
    @Expose
    public String isActive;

}