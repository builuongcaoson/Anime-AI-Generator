package com.sola.anime.ai.generator.domain.model.config.style

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Keep
@Entity(tableName = "Styles")
data class Style(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @SerializedName("display")
    @Expose
    val display: String,
    @SerializedName("prompt")
    @Expose
    val prompt: String,
    @SerializedName("negativePrompt")
    @Expose
    val negativePrompt: String,
    @SerializedName("preview")
    @Expose
    val preview: String
)