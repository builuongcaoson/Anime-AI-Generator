package com.sola.anime.ai.generator.domain.model.textToImage

import android.graphics.Bitmap
import okhttp3.ResponseBody

data class ResponseTextToImage(
    val groupId: Int,
    val childId: Int,
    val response: ResponseBody
)

data class BodyTextToImage(
    val id: Int = 0,
    val groupId: Int = 0,
    val prompt: String = "a cute beautiful girl listening to relaxing music with her headphones that takes her to a surreal forest, young anime girl, long wavy blond hair, sky blue eyes, full round face, miniskirt, front view, mid - shot, highly detailed, digital art by wlop, trending on artstation",
    val negative_prompt: String = "(character out of frame)1.4, (worst quality)1.2, (low quality)1.6, (normal quality)1.6, lowres, (monochrome)1.1, (grayscale)1.3, acnes, skin blemishes, bad anatomy, DeepNegative,(fat)1.1, bad hands, text, error, missing fingers, extra limbs, missing limbs, extra digits, fewer digits, cropped, jpeg artifacts,signature, watermark, furry, elf ears",
    val guidance: String = "7.5",
    val upscale: String = "1",
    val sampler: String = "euler_a",
    val steps: String = "50",
    val model: String = "anything_4_0",
    val width: String = "320",
    val height: String = "320",
    val seed: String? = null
)

data class DezgoBodyTextToImage(
    val id: Int,
    val bodies: List<BodyTextToImage>
)

data class DezgoStatusTextToImage(
    val id: Int,
    val groupId: Int,
    var status: StatusBodyTextToImage
)

sealed class StatusBodyTextToImage(val isLoading: Boolean = false){
    object Idle: StatusBodyTextToImage()
    object Loading: StatusBodyTextToImage(isLoading = true)
    data class Success(val bitmap: Bitmap): StatusBodyTextToImage()
    data class Failure(val error: String? = null): StatusBodyTextToImage()
}