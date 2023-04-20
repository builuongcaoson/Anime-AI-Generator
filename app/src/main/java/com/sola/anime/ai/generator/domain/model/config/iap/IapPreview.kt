package com.sola.anime.ai.generator.domain.model.config.iap

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Keep
@Entity(tableName = "IapPreviews")
data class IapPreview(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @SerializedName("preview")
    @Expose
    val preview: String,
    @SerializedName("ratio")
    @Expose
    var ratio: String = "1:1"
)