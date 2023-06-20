package com.sola.anime.ai.generator.domain.model.status

import com.sola.anime.ai.generator.domain.model.textToImage.BodyTextToImage
import java.io.File

sealed class GenerateTextsToImagesProgress(val isLoading: Boolean = false) {
    object Idle: GenerateTextsToImagesProgress()
    object Loading: GenerateTextsToImagesProgress(isLoading = true)
    data class LoadingWithId(val body: BodyTextToImage): GenerateTextsToImagesProgress(isLoading = true)
    data class SuccessWithId(val body: BodyTextToImage, val file: File): GenerateTextsToImagesProgress(isLoading = true)
    data class FailureWithId(val body: BodyTextToImage, val error: String? = null): GenerateTextsToImagesProgress(isLoading = true)
    object Done: GenerateTextsToImagesProgress()
}

data class DezgoStatusTextToImage(
    val body: BodyTextToImage,
    var status: StatusBodyTextToImage
)

sealed class StatusBodyTextToImage {
    object Loading: StatusBodyTextToImage()
    data class Success(val file: File): StatusBodyTextToImage()
    data class Failure(val error: String? = null): StatusBodyTextToImage()
}