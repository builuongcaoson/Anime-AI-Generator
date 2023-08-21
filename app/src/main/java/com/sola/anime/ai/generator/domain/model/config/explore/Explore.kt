package com.sola.anime.ai.generator.domain.model.config.explore

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Keep
@Entity(tableName = "Explores")
class Explore {
    @PrimaryKey
    @SerializedName("id")
    @Expose
    var id: Long = 0
    @SerializedName("prompt")
    @Expose
    var prompt: String = ""
    @SerializedName("previews")
    @Expose
    var previews: List<String> = listOf()
    @SerializedName("modelIds")
    @Expose
    var modelIds: List<Long> = listOf()
    @SerializedName("ratio")
    @Expose
    var ratio: String = "1:1"
    @SerializedName("favourite_count")
    @Expose
    var favouriteCount: Long = 0
    @SerializedName("is_favourite")
    @Expose
    var isFavourite: Boolean = false
}