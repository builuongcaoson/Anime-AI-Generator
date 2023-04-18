package com.sola.anime.ai.generator.domain.model.config;

import androidx.annotation.Keep;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Keep
public class DataConfigApp {

    @SerializedName("app")
    @Expose
    public App app;
    @SerializedName("art")
    @Expose
    public Art art;
    @SerializedName("batch")
    @Expose
    public Batch batch;
    @SerializedName("avatar")
    @Expose
    public Avatar avatar;

}