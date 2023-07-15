package com.sola.anime.ai.generator.domain.model.history

data class ChildHistory(
    val photoUriString: String? = null,
    val pathPreview: String,
    var upscalePathPreview: String? = null,
    val prompt: String,
    val negativePrompt: String,
    val guidance: String,
    val upscale: String,
    val sampler: String,
    val steps: String,
    val model: String,
    val width: String,
    val height: String,
    val strength: String?,
    val seed: String?
){

    var isPremium = false
    var styleId: Long = -1L
    var type: Int = 0 // 0: Artwork, 1: Batch, 2: Avatar

    var createAt: Long = System.currentTimeMillis()
    var updateAt: Long = System.currentTimeMillis()

}