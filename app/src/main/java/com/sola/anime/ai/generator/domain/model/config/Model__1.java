package com.sola.anime.ai.generator.domain.model.config;

import androidx.annotation.Keep;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Keep
public class Model__1 {

    @SerializedName("id")
    @Expose
    public Integer id;
    @SerializedName("display")
    @Expose
    public String display;
    @SerializedName("model")
    @Expose
    public String model;

}