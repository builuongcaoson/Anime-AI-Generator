package com.sola.anime.ai.generator.domain.model.config.iap

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Keep
@Entity(tableName = "IAPS")
class IAP{
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
    @SerializedName("preview")
    @Expose
    var preview: String = ""
    @SerializedName("ratio")
    @Expose
    var ratio: String = "1:1"
}