package com.sola.anime.ai.generator.domain.model.server;

import androidx.annotation.Keep;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Keep
public class Message {

    @SerializedName("Message")
    @Expose
    public String message;

}