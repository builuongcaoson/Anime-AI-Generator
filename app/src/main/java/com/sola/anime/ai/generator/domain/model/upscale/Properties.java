package com.sola.anime.ai.generator.domain.model.upscale;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Properties {

    @SerializedName("original_height")
    @Expose
    public Integer originalHeight;
    @SerializedName("original_width")
    @Expose
    public Integer originalWidth;
    @SerializedName("upscale_height")
    @Expose
    public Integer upscaleHeight;
    @SerializedName("upscale_width")
    @Expose
    public Integer upscaleWidth;

}