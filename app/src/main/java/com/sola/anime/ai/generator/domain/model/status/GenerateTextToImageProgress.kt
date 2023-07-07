package com.sola.anime.ai.generator.domain.model.status

import android.net.Uri
import com.sola.anime.ai.generator.domain.model.textToImage.BodyImageToImage
import com.sola.anime.ai.generator.domain.model.textToImage.BodyTextToImage
import java.io.File

sealed class GenerateTextsToImagesProgress(val isLoading: Boolean = false) {
    object Idle: GenerateTextsToImagesProgress()
    object Loading: GenerateTextsToImagesProgress(isLoading = true)
    data class LoadingWithId(val groupId: Long, val childId: Long): GenerateTextsToImagesProgress(isLoading = true)
    data class SuccessWithId(val groupId: Long, val childId: Long, val file: File): GenerateTextsToImagesProgress(isLoading = true)
    data class FailureWithId(val groupId: Long, val childId: Long, val error: String? = null): GenerateTextsToImagesProgress(isLoading = true)
    object Done: GenerateTextsToImagesProgress()
}

sealed class GenerateImagesToImagesProgress(val isLoading: Boolean = false) {
    object Idle: GenerateImagesToImagesProgress()
    object Loading: GenerateImagesToImagesProgress(isLoading = true)
    data class LoadingWithId(val groupId: Long, val childId: Long): GenerateImagesToImagesProgress(isLoading = true)
    data class SuccessWithId(val groupId: Long, val childId: Long, val photoUri: Uri, val file: File): GenerateImagesToImagesProgress(isLoading = true)
    data class FailureWithId(val groupId: Long, val childId: Long, val error: String? = null): GenerateImagesToImagesProgress(isLoading = true)
    object Done: GenerateImagesToImagesProgress()
}

data class DezgoStatusTextToImage(
    val body: BodyTextToImage,
    var status: StatusBodyTextToImage
)

data class DezgoStatusImageToImage(
    val body: BodyImageToImage,
    var status: StatusBodyImageToImage
)

sealed class StatusBodyTextToImage {
    object Loading: StatusBodyTextToImage()
    data class Success(val file: File): StatusBodyTextToImage()
    data class Failure(val error: String? = null): StatusBodyTextToImage()
}

sealed class StatusBodyImageToImage {
    object Loading: StatusBodyImageToImage()
    data class Success(val file: File): StatusBodyImageToImage()
    data class Failure(val error: String? = null): StatusBodyImageToImage()
}