package com.sola.anime.ai.generator.domain.repo

import com.sola.anime.ai.generator.domain.model.status.GenerateTextsToImagesProgress
import com.sola.anime.ai.generator.domain.model.textToImage.DezgoBodyTextToImage
import io.reactivex.Observable

interface DezgoApiRepository {

    fun progress(): Observable<GenerateTextsToImagesProgress>

    suspend fun generateTextsToImages(datas: List<DezgoBodyTextToImage>)

}