package com.sola.anime.ai.generator.common.extension

import com.sola.anime.ai.generator.domain.model.Ratio
import com.sola.anime.ai.generator.domain.model.config.style.Style
import com.sola.anime.ai.generator.domain.model.history.ChildHistory
import com.sola.anime.ai.generator.domain.model.textToImage.BodyTextToImage
import com.sola.anime.ai.generator.domain.model.textToImage.DezgoBodyTextToImage

fun BodyTextToImage.toChildHistory(pathPreview: String): ChildHistory{
    return ChildHistory(
        pathPreview = pathPreview,
        prompt = this.prompt,
        negativePrompt = this.negativePrompt,
        guidance = this.guidance,
        upscale = this.upscale,
        sampler = this.sampler,
        steps = this.steps,
        model = this.model,
        width = this.width,
        height = this.height,
        seed = this.seed
    ).apply {
        this.styleId = this@toChildHistory.styleId
    }
}

fun initDezgoBodyTextsToImages(
    maxGroupId: Int = 0,
    maxChildId: Int = 0,
    prompt: String,
    negativePrompt: String,
    guidance: String,
    styleId: Long,
    ratio: Ratio,
    seed: Long
): List<DezgoBodyTextToImage>{
    val datas = arrayListOf<DezgoBodyTextToImage>()
    (0..maxGroupId).forEach { id ->
        datas.add(
            DezgoBodyTextToImage(
                id = id.toLong(),
                bodies = initBodyTextsToImages(
                    groupId = id.toLong(),
                    maxChildId = maxChildId,
                    prompt = prompt,
                    negativePrompt = negativePrompt,
                    guidance = guidance,
                    styleId = styleId,
                    ratio = ratio,
                    seed = seed
                )
            )
        )
    }
    return datas
}

fun initBodyTextsToImages(
    groupId: Long,
    maxChildId: Int,
    prompt: String,
    negativePrompt: String,
    guidance: String,
    styleId: Long,
    ratio: Ratio,
    seed: Long
): List<BodyTextToImage>{
    val bodies = arrayListOf<BodyTextToImage>()
    (0..maxChildId).forEach { id ->
        bodies.add(
            BodyTextToImage(
                id = id.toLong(),
                groupId = groupId,
                prompt = prompt,
                negativePrompt = negativePrompt,
                guidance = guidance,
                width = ratio.width,
                height = ratio.height,
                seed = seed.toString()
            ).apply {
                this.styleId = styleId
            }
        )
    }
    return bodies
}