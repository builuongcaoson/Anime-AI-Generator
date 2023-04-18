package com.sola.anime.ai.generator.domain.model.config;

import androidx.annotation.Keep;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Keep
public class Model {

    @SerializedName("id")
    @Expose
    public Integer id;
    @SerializedName("preview")
    @Expose
    public String preview;
    @SerializedName("model")
    @Expose
    public Model__1 model;
    @SerializedName("isPremium")
    @Expose
    public Boolean isPremium;

}