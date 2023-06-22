package com.sola.anime.ai.generator.domain.model

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey

data class CategoryBatch(val display: String)

data class PreviewCategoryBatch(val display: String, val preview: Int, val modelId: String, val isPremium: Boolean = false, val description: String = "")

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

data class PreviewIap(val preview: String, val ratio: String)