package com.sola.anime.ai.generator.domain.model.textToImage

import androidx.annotation.Keep
import com.sola.anime.ai.generator.domain.model.Ratio
import okhttp3.ResponseBody

data class ResponseTextToImage(
    val groupId: Long,
    val childId: Long,
    val response: ResponseBody? = null
)

data class ResponseImageToImage(
    val groupId: Long,
    val childId: Long,
    val response: ResponseBody
)

@Keep
data class BodyTextToImage(
    val id: Long,
    val groupId: Long,
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
) {
    var styleId: Long = -1L
}

@Keep
data class BodyImageToImage(
    val id: Long,
    val groupId: Long,
    val initImage: String,
    val prompt: String,
    val negativePrompt: String,
    val guidance: String,
    val upscale: String,
    val sampler: String,
    val steps: String,
    val model: String,
    val seed: String?,
    val strength: String
) {
    var styleId: Long = -1L
}

data class DezgoBodyTextToImage(
    val id: Long,
    val bodies: List<BodyTextToImage>
)

data class DezgoBodyImageToImage(
    val id: Long,
    val bodies: List<BodyImageToImage>
)