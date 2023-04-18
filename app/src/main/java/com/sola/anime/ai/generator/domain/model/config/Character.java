package com.sola.anime.ai.generator.domain.model.config;

import androidx.annotation.Keep;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Keep
public class Character {

    @SerializedName("id")
    @Expose
    public Integer id;
    @SerializedName("preview")
    @Expose
    public String preview;
    @SerializedName("prompt")
    @Expose
    public String prompt;
    @SerializedName("negative")
    @Expose
    public String negative;
    @SerializedName("guidance")
    @Expose
    public Float guidance;
    @SerializedName("upscale")
    @Expose
    public Integer upscale;
    @SerializedName("sampler")
    @Expose
    public String sampler;
    @SerializedName("steps")
    @Expose
    public String steps;
    @SerializedName("modelId")
    @Expose
    public Integer modelId;
    @SerializedName("width")
    @Expose
    public Integer width;
    @SerializedName("height")
    @Expose
    public Integer height;
    @SerializedName("seed")
    @Expose
    public Long seed;
    @SerializedName("isPremium")
    @Expose
    public Boolean isPremium;

}