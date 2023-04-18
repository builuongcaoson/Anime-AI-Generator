package com.sola.anime.ai.generator.domain.model.config;

import androidx.annotation.Keep;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Keep
public class Preview3 {

    @SerializedName("preview")
    @Expose
    public String preview;
    @SerializedName("ratio")
    @Expose
    public String ratio;

}