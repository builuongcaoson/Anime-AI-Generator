package com.sola.anime.ai.generator.domain.model.status

import android.graphics.Bitmap

sealed class GenerateTextsToImagesProgress(val isLoading: Boolean = false) {
    object Idle: GenerateTextsToImagesProgress()
    object Loading: GenerateTextsToImagesProgress(isLoading = true)
    data class LoadingWithId(val groupId: Int, val childId: Int): GenerateTextsToImagesProgress(isLoading = true)
    data class SuccessWithId(val groupId: Int, val childId: Int, val bitmap: Bitmap): GenerateTextsToImagesProgress(isLoading = true)
    data class FailureWithId(val groupId: Int, val childId: Int, val error: String? = null): GenerateTextsToImagesProgress(isLoading = true)
    object Done: GenerateTextsToImagesProgress()
}