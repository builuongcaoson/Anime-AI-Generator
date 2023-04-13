package com.sola.anime.ai.generator.domain.model

data class CategoryBatch(val display: String)

data class PreviewCategoryBatch(val display: String, val preview: Int, val isPremium: Boolean = false)

data class PromptBatch(val prompt: String)