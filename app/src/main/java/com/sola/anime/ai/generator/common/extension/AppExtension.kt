package com.sola.anime.ai.generator.common.extension

import com.sola.anime.ai.generator.domain.model.history.ChildHistory
import com.sola.anime.ai.generator.domain.model.textToImage.BodyTextToImage
import com.sola.anime.ai.generator.domain.model.textToImage.DezgoBodyTextToImage

fun DezgoBodyTextToImage.toChildHistory(): ChildHistory{
    return ChildHistory()
}

fun initDezgoBodyTextsToImages(maxGroupId: Int = 0, maxChildId: Int = 0): List<DezgoBodyTextToImage>{
    val datas = arrayListOf<DezgoBodyTextToImage>()
    (0..maxGroupId).forEach { id ->
        datas.add(
            DezgoBodyTextToImage(
                id = id.toLong(),
                bodies = initBodyTextsToImages(maxChildId = maxChildId)
            )
        )
    }
    return datas
}

fun initBodyTextsToImages(maxChildId: Int = 0): List<BodyTextToImage>{
    val bodies = arrayListOf<BodyTextToImage>()
    (0..maxChildId).forEach { id ->
        bodies.add(BodyTextToImage(id = id.toLong()))
    }
    return bodies
}