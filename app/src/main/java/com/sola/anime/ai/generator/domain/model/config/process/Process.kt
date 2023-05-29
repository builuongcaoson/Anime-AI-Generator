package com.sola.anime.ai.generator.domain.model.config.process

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.Expose

@Keep
@Entity(tableName = "Process")
data class Process(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @SerializedName("preview")
    @Expose
    var preview: String? = null,
    @SerializedName("title")
    @Expose
    var title: String,
    @SerializedName("artist")
    @Expose
    var artist: String
)