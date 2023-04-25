package com.sola.anime.ai.generator.domain.model.status

import android.graphics.Bitmap
import java.io.File

sealed class GenerateTextsToImagesProgress(val isLoading: Boolean = false) {
    object Idle: GenerateTextsToImagesProgress()
    object Loading: GenerateTextsToImagesProgress(isLoading = true)
    data class LoadingWithId(val groupId: Long, val childId: Long): GenerateTextsToImagesProgress(isLoading = true)
    data class SuccessWithId(val groupId: Long, val childId: Long, val bitmap: Bitmap, val file: File): GenerateTextsToImagesProgress(isLoading = true)
    data class FailureWithId(val groupId: Long, val childId: Long, val error: String? = null): GenerateTextsToImagesProgress(isLoading = true)
    object Done: GenerateTextsToImagesProgress()
}

data class DezgoStatusTextToImage(
    val id: Long,
    val groupId: Long,
    var status: StatusBodyTextToImage
)

sealed class StatusBodyTextToImage{
    object Idle: StatusBodyTextToImage()
    object Loading: StatusBodyTextToImage()
    data class Success(val bitmap: Bitmap): StatusBodyTextToImage()
    data class Failure(val error: String? = null): StatusBodyTextToImage()
}