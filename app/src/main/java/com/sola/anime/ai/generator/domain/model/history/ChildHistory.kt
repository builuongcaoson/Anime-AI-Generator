package com.sola.anime.ai.generator.domain.model.history

data class ChildHistory(
    val pathPreview: String,
    val prompt: String,
    val negativePrompt: String,
    val guidance: String,
    val upscale: String,
    val sampler: String,
    val steps: String,
    val model: String,
    val width: String,
    val height: String,
    val seed: String?
){

    var styleId: Long = -1L

    var createAt: Long = System.currentTimeMillis()
    var updateAt: Long = System.currentTimeMillis()

}