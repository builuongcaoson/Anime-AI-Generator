package com.sola.anime.ai.generator.domain.model.config.style

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Keep
@Entity(tableName = "Styles")
class Style{
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
    @SerializedName("preview")
    @Expose
    var preview: String = ""
    @SerializedName("display")
    @Expose
    var display: String = ""
    @SerializedName("prompts")
    @Expose
    var prompts: List<String> = listOf()
}