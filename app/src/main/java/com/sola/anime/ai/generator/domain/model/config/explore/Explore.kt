package com.sola.anime.ai.generator.domain.model.config.explore

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Keep
@Entity(tableName = "Explores")
class Explore{
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
    @SerializedName("preview")
    @Expose
    var preview: String = ""
    @SerializedName("prompt")
    @Expose
    var prompt: String = ""
    @SerializedName("negative")
    @Expose
    var negative: String = ""
    @SerializedName("guidance")
    @Expose
    var guidance: Float = 7.5f
    @SerializedName("upscale")
    @Expose
    var upscale: Int = 1
    @SerializedName("sampler")
    @Expose
    var sampler: String = ""
    @SerializedName("steps")
    @Expose
    var steps: String = "45"
    @SerializedName("modelId")
    @Expose
    var modelId: Int = -1
    @SerializedName("seed")
    @Expose
    var seed: Long? = null
    @SerializedName("isPremium")
    @Expose
    var isPremium: Boolean = false
    @SerializedName("ratio")
    @Expose
    var ratio: String = "1:1"
}