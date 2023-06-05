package com.sola.anime.ai.generator.domain.model

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey

data class CategoryBatch(val display: String)

data class PreviewCategoryBatch(val display: String, val preview: Int, val isPremium: Boolean = false)

data class PromptBatch(val prompt: String = "", var numberOfImages: NumberOfImages = NumberOfImages.NumberOfImages1, var ratio: Ratio = Ratio.Ratio1x1)

@Keep
@Entity(tableName = "Folders")
data class Folder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val display: String
)

data class PreviewIap(val preview: String, val ratio: String)