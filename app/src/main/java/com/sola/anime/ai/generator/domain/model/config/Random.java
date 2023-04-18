package com.sola.anime.ai.generator.domain.model.config;

import androidx.annotation.Keep;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Keep
public class Random {

    @SerializedName("prompt")
    @Expose
    public String prompt;
    @SerializedName("negative")
    @Expose
    public String negative;

}