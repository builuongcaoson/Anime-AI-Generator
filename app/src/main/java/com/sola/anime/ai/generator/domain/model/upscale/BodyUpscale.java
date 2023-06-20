package com.sola.anime.ai.generator.domain.model.upscale;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class BodyUpscale {

    @SerializedName("upscale")
    @Expose
    public Integer upscale;
    @SerializedName("image")
    @Expose
    public String image;

}