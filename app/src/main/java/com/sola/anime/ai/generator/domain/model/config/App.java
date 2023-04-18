package com.sola.anime.ai.generator.domain.model.config;

import androidx.annotation.Keep;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

@Keep
public class App {

    @SerializedName("iap")
    @Expose
    public Iap iap;
    @SerializedName("models")
    @Expose
    public List<Model> models;
    @SerializedName("explores")
    @Expose
    public List<Explore> explores;
    @SerializedName("characters")
    @Expose
    public List<Character> characters;
    @SerializedName("randoms")
    @Expose
    public List<Random> randoms;

}