package com.sola.anime.ai.generator.domain.model.config;

import androidx.annotation.Keep;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

@Keep
public class Iap {

    @SerializedName("preview1")
    @Expose
    public List<Preview1> preview1;
    @SerializedName("preview2")
    @Expose
    public List<Preview2> preview2;
    @SerializedName("preview3")
    @Expose
    public List<Preview3> preview3;

}