package com.sola.anime.ai.generator.domain.model

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ModelOrLoRA(val display: String, val preview: String, val favouriteCount: Long, val description: String, val isFavourite: Boolean, val isModel: Boolean)

data class CategoryBatch(val display: String)

data class PreviewCategoryBatch(val preview: String, val display: String, val model: String, val description: String = "", val isPremium: Boolean = false)

data class PromptBatch(
    var prompt: String = "",
    var negativePrompt: String = "",
    var numberOfImages: NumberOfImages = NumberOfImages.NumberOfImages1,
    var ratio: Ratio = Ratio.Ratio1x1,
    var guidance: Float = 7.5f, // Min 5 Max 10 Step 0.5
    var step: Int = 45, // Min 30 Max 60 Step 5
    var sampler: Sampler = Sampler.EulerA,
    var isFullHd: Boolean = false
)

@Keep
@Entity(tableName = "Folders")
data class Folder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val display: String
)

@Entity(tableName = "PhotoStorages")
data class PhotoStorage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val uriString: String,
    val ratio: Ratio
)