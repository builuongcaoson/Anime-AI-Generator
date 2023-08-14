package com.sola.anime.ai.generator.domain.model.config.model

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Keep
@Entity(tableName = "Models")
class Model{
    @PrimaryKey
    @SerializedName("id")
    @Expose
    var id: Long = 0
    @SerializedName("preview")
    @Expose
    var preview: String = ""
    @SerializedName("display")
    @Expose
    var display: String = ""
    @SerializedName("description")
    @Expose
    var description: String = ""
    @SerializedName("favourite_count")
    @Expose
    var favouriteCount: Long = 0
    @SerializedName("modelId")
    @Expose
    var modelId: String = ""
    @SerializedName("active")
    @Expose
    var active: String = ""
}