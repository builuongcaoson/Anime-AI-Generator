package com.sola.anime.ai.generator.domain.repo

import android.net.Uri
import com.sola.anime.ai.generator.domain.model.status.GenerateImagesToImagesProgress
import com.sola.anime.ai.generator.domain.model.status.GenerateTextsToImagesProgress
import com.sola.anime.ai.generator.domain.model.textToImage.DezgoBodyImageToImage
import com.sola.anime.ai.generator.domain.model.textToImage.DezgoBodyTextToImage
import io.reactivex.Observable
import java.io.File

interface DezgoApiRepository {

    suspend fun generateTextsToImages(
        datas: List<DezgoBodyTextToImage>,
        progress: (GenerateTextsToImagesProgress) -> Unit
    )

    suspend fun generateImagesToImages(
        datas: List<DezgoBodyImageToImage>,
        progress: (GenerateImagesToImagesProgress) -> Unit
    )

}