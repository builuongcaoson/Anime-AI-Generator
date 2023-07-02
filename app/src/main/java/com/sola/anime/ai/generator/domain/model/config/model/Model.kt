package com.sola.anime.ai.generator.domain.model.config.model

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Keep
@Entity(tableName = "Models")
class Model{
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
    @SerializedName("preview")
    @Expose
    var preview: String = ""
    @SerializedName("display")
    @Expose
    var display: String = ""
    @SerializedName("model")
    @Expose
    var model: String = ""
    @SerializedName("category")
    @Expose
    var category: String = ""
}