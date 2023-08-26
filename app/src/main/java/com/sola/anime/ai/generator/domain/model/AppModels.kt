package com.sola.anime.ai.generator.domain.model

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sola.anime.ai.generator.domain.model.config.explore.Explore
import com.sola.anime.ai.generator.domain.model.config.lora.LoRA
import com.sola.anime.ai.generator.domain.model.config.model.Model

data class ExplorePreview(val exploreId: Long, val previewIndex: Int, val preview: String, val ratio: String)

data class ExploreOrLoRAPreview(val explore: Explore? = null, val loRAPreview: String? = null, val ratio: String, val favouriteCount: Long, var isFavourite: Boolean)

data class ModelOrLoRA(val display: String, val model: Model? = null, val loRA: LoRA? = null, val loRAGroupId: Long = -1, val favouriteCount: Long, var isFavourite: Boolean)

data class CategoryBatch(val display: String)

data class PreviewCategoryBatch(val preview: String, val display: String, val model: String, val description: String = "", )

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