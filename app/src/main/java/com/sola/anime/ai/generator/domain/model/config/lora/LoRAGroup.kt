package com.sola.anime.ai.generator.domain.model.config.lora

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Keep
@Entity(tableName = "LoRAGroups")
class LoRAGroup{
    @PrimaryKey
    @SerializedName("id")
    @Expose
    var id: Long = 0
    @SerializedName("display")
    @Expose
    var display: String = ""
    @SerializedName("childs")
    @Expose
    var childs: List<LoRA> = listOf()
}


@Keep
class LoRA {
    @SerializedName("id")
    @Expose
    var id: Long = 0
    @SerializedName("previews")
    @Expose
    var previews: List<String> = listOf()
    @SerializedName("display")
    @Expose
    var display: String = ""
    @SerializedName("trigger_words")
    @Expose
    var triggerWords: List<String> = listOf()
    @SerializedName("favourite_count")
    @Expose
    var favouriteCount: Long = 0
    @SerializedName("sha256")
    @Expose
    var sha256: String = ""
    @SerializedName("ratio")
    @Expose
    var ratio: String = "1:1"
    @SerializedName("is_favourite")
    @Expose
    var isFavourite: Boolean = false
    @SerializedName("is_dislike")
    @Expose
    var isDislike: Boolean = false
    @SerializedName("sortOrder")
    @Expose
    var sortOrder: Int = (-1000..1000).random()
}
