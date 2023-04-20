package com.sola.anime.ai.generator.domain.model.config.explore

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Keep
@Entity(tableName = "Explores")
data class Explore(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @SerializedName("preview")
    @Expose
    val preview: String,
    @SerializedName("prompt")
    @Expose
    var prompt: String,
    @SerializedName("negative")
    @Expose
    var negative: String,
    @SerializedName("guidance")
    @Expose
    var guidance: Float,
    @SerializedName("upscale")
    @Expose
    var upscale: Int,
    @SerializedName("sampler")
    @Expose
    var sampler: String,
    @SerializedName("steps")
    @Expose
    var steps: String,
    @SerializedName("modelId")
    @Expose
    var modelId: Int,
    @SerializedName("seed")
    @Expose
    var seed: Long? = null,
    @SerializedName("isPremium")
    @Expose
    var isPremium: Boolean,
    @SerializedName("ratio")
    @Expose
    var ratio: String = "1:1"
)