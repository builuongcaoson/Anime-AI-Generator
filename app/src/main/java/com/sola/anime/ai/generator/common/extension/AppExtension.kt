package com.sola.anime.ai.generator.common.extension

import com.sola.anime.ai.generator.domain.model.textToImage.BodyTextToImage
import com.sola.anime.ai.generator.domain.model.textToImage.DezgoBodyTextToImage

fun initDezgoBodyTextsToImages(maxGroupId: Int = 0, maxChildId: Int = 0): List<DezgoBodyTextToImage>{
    val datas = arrayListOf<DezgoBodyTextToImage>()
    (0..maxGroupId).forEach { id ->
        datas.add(
            DezgoBodyTextToImage(
                id = id,
                bodies = initBodyTextsToImages(maxChildId = maxChildId)
            )
        )
    }
    return datas
}

fun initBodyTextsToImages(maxChildId: Int = 0): List<BodyTextToImage>{
    val bodies = arrayListOf<BodyTextToImage>()
    (0..maxChildId).forEach { id ->
        bodies.add(BodyTextToImage(id = id))
    }
    return bodies
}