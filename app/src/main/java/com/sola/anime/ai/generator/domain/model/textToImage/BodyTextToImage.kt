package com.sola.anime.ai.generator.domain.model.textToImage

data class BodyTextToImage(
    val prompt: String,
    val negative_prompt: String,
    val guidance: String,
    val upscale: String,
    val sampler: String,
    val steps: String,
    val model: String,
    val width: String,
    val height: String,
    val seed: String?
)