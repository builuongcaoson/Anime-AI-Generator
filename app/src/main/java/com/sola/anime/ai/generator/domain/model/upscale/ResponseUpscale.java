package com.sola.anime.ai.generator.domain.model.upscale;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ResponseUpscale {

    @SerializedName("job_id")
    @Expose
    public String jobId;
    @SerializedName("output_url")
    @Expose
    public String outputUrl;
    @SerializedName("parameters")
    @Expose
    public Parameters parameters;
    @SerializedName("properties")
    @Expose
    public Properties properties;
    @SerializedName("url_expiry")
    @Expose
    public String urlExpiry;

}



