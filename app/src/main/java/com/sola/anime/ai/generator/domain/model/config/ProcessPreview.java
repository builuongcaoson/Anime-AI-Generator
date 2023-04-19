package com.sola.anime.ai.generator.domain.model.config;

import androidx.annotation.Keep;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Keep
public class ProcessPreview {

    public Integer previewRes;
    @SerializedName("preview")
    @Expose
    public String preview;
    @SerializedName("title")
    @Expose
    public String title;
    @SerializedName("artist")
    @Expose
    public String artist;

}