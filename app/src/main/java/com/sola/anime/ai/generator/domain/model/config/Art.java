package com.sola.anime.ai.generator.domain.model.config;

import androidx.annotation.Keep;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

@Keep
public class Art {

    @SerializedName("processPreviews")
    @Expose
    public List<ProcessPreview> processPreviews;

}

