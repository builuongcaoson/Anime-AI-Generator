package com.sola.anime.ai.generator.domain.model.server;

import androidx.annotation.Keep;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Keep
public class UserPremium {

    @SerializedName("id")
    @Expose
    public String id;
    @SerializedName("appUserId")
    @Expose
    public String appUserId;
    @SerializedName("deviceId")
    @Expose
    public String deviceId;
    @SerializedName("isUpgraded")
    @Expose
    public String timePurchased;
    @SerializedName("timeExpired")
    @Expose
    public String timeExpired;
    @SerializedName("numberCreatedArtworkInDay")
    @Expose
    public String numberCreatedArtworkInDay;
    @SerializedName("totalNumberCreatedArtwork")
    @Expose
    public String totalNumberCreatedArtwork;
    @SerializedName("latestTimeCreatedArtwork")
    @Expose
    public String latestTimeCreatedArtwork;

}