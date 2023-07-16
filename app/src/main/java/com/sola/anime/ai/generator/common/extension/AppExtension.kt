package com.sola.anime.ai.generator.common.extension

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.provider.Settings
import android.text.format.DateUtils
import com.sola.anime.ai.generator.domain.model.Ratio
import com.sola.anime.ai.generator.domain.model.Sampler
import com.sola.anime.ai.generator.domain.model.config.style.Style
import com.sola.anime.ai.generator.domain.model.history.ChildHistory
import com.sola.anime.ai.generator.domain.model.textToImage.BodyImageToImage
import com.sola.anime.ai.generator.domain.model.textToImage.BodyTextToImage
import com.sola.anime.ai.generator.domain.model.textToImage.DezgoBodyImageToImage
import com.sola.anime.ai.generator.domain.model.textToImage.DezgoBodyTextToImage
import java.util.*

fun getDeviceModel(): String {
    return android.os.Build.MODEL
}

@SuppressLint("HardwareIds")
fun Context.getDeviceId(): String {
    return Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID) ?: UUID.randomUUID().toString()
}

fun BodyTextToImage.toChildHistory(pathPreview: String): ChildHistory {
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
        strength = null,
        seed = this.seed
    ).apply {
        this.styleId = this@toChildHistory.styleId
        this.type = this@toChildHistory.type
    }
}

fun BodyImageToImage.toChildHistory(photoUriString: String, pathPreview: String): ChildHistory {
    return ChildHistory(
        photoUriString = photoUriString,
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
        strength = null,
        seed = this.seed
    ).apply {
        this.styleId = this@toChildHistory.styleId
        this.type = this@toChildHistory.type
    }
}

fun BodyImageToImage.toChildHistory(newPrompt: String, photoUriString: String, pathPreview: String): ChildHistory {
    return ChildHistory(
        photoUriString = photoUriString,
        pathPreview = pathPreview,
        prompt = newPrompt,
        negativePrompt = this.negativePrompt,
        guidance = this.guidance,
        upscale = this.upscale,
        sampler = this.sampler,
        steps = this.steps,
        model = this.model,
        width = this.width,
        height = this.height,
        strength = null,
        seed = this.seed
    ).apply {
        this.styleId = this@toChildHistory.styleId
        this.type = this@toChildHistory.type
    }
}

fun initDezgoBodyTextsToImages(
    groupId: Long = 0,
    maxChildId: Int = 0,
    prompt: String,
    negativePrompt: String,
    guidance: String,
    steps: String,
    model: String,
    sampler: String,
    upscale: String,
    styleId: Long,
    ratio: Ratio,
    seed: Long?,
    type: Int,
): List<DezgoBodyTextToImage>{
    val datas = arrayListOf<DezgoBodyTextToImage>()
    datas.add(
        DezgoBodyTextToImage(
            id = groupId,
            bodies = initBodyTextsToImages(
                groupId = groupId,
                maxChildId = maxChildId,
                prompt = prompt,
                negativePrompt = negativePrompt,
                guidance = guidance,
                steps = steps,
                model = model,
                sampler = sampler,
                upscale = upscale,
                styleId = styleId,
                ratio = ratio,
                seed = seed,
                type = type
            )
        )
    )
    return datas
}

fun initBodyTextsToImages(
    groupId: Long,
    maxChildId: Int,
    prompt: String,
    negativePrompt: String,
    guidance: String,
    steps: String,
    model: String,
    sampler: String,
    upscale: String,
    styleId: Long,
    ratio: Ratio,
    seed: Long?,
    type: Int
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
                steps = steps,
                model = model,
                sampler = sampler,
                upscale = upscale,
                width = ratio.width,
                height = ratio.height,
                seed = seed?.toString()
            ).apply {
                this.styleId = styleId
                this.type = type
            }
        )
    }
    return bodies
}

fun initDezgoBodyImagesToImages(
    groupId: Long = 0,
    maxChildId: Int = 0,
    initImage: Uri,
    prompt: String,
    negativePrompt: String,
    guidance: String,
    steps: String,
    model: String,
    sampler: String,
    upscale: String,
    styleId: Long,
    ratio: Ratio,
    strength: String,
    seed: Long?,
    type: Int,
): List<DezgoBodyImageToImage>{
    val datas = arrayListOf<DezgoBodyImageToImage>()
    datas.add(
        DezgoBodyImageToImage(
            id = groupId,
            bodies = initBodyImagesToImages(
                groupId = groupId,
                maxChildId = maxChildId,
                initImage = initImage,
                prompt = prompt,
                negativePrompt = negativePrompt,
                guidance = guidance,
                steps = steps,
                model = model,
                sampler = sampler,
                upscale = upscale,
                styleId = styleId,
                ratio = ratio,
                strength = strength,
                seed = seed,
                type = type
            )
        )
    )
    return datas
}

fun initBodyImagesToImages(
    groupId: Long,
    maxChildId: Int,
    initImage: Uri,
    prompt: String,
    negativePrompt: String,
    guidance: String,
    steps: String,
    model: String,
    sampler: String,
    upscale: String,
    styleId: Long,
    ratio: Ratio,
    strength: String,
    seed: Long?,
    type: Int
): List<BodyImageToImage>{
    val bodies = arrayListOf<BodyImageToImage>()
    (0..maxChildId).forEach { id ->
        bodies.add(
            BodyImageToImage(
                id = id.toLong(),
                groupId = groupId,
                initImage = initImage,
                prompt = prompt,
                negativePrompt = negativePrompt,
                guidance = guidance,
                steps = steps,
                model = model,
                sampler = sampler,
                upscale = upscale,
                width = ratio.width,
                height = ratio.height,
                seed = seed?.toString(),
                strength = strength
            ).apply {
                this.styleId = styleId
                this.type = type
            }
        )
    }
    return bodies
}

fun Long.isToday(): Boolean {
    return DateUtils.isToday(this)
}

fun Context.getDrawableUri(drawableResId: Int): Uri {
    return Uri.parse("android.resource://${packageName}/$drawableResId")
}