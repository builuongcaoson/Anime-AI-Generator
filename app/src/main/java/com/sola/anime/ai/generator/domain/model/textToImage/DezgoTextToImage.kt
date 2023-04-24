package com.sola.anime.ai.generator.domain.model.textToImage

import android.graphics.Bitmap
import okhttp3.ResponseBody

data class ResponseTextToImage(
    val groupId: Int,
    val childId: Int,
    val response: ResponseBody
)

data class DezgoTextToImage(
    val id: Int,
    val dezgoBodyTextToImage: DezgoBodyTextToImage
)

data class DezgoBodyTextToImage(
    val id: Int,
    val bodyTextToImage: BodyTextToImage
)

data class DezgoStatusTextToImage(
    val id: Int,
    val bodyId: Int,
    val status: StatusBodyTextToImage
)

sealed class StatusBodyTextToImage(val isLoading: Boolean = false){
    object Idle: StatusBodyTextToImage()
    object Loading: StatusBodyTextToImage(isLoading = true)
    data class Success(val bitmap: Bitmap): StatusBodyTextToImage()
    data class Failure(val error: String? = null)
}