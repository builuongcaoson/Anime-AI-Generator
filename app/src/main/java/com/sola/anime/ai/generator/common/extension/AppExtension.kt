package com.sola.anime.ai.generator.common.extension

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.provider.Settings
import android.text.format.DateUtils
import com.sola.anime.ai.generator.BuildConfig
import com.sola.anime.ai.generator.common.ConfigApp
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.domain.model.Ratio
import com.sola.anime.ai.generator.domain.model.history.ChildHistory
import com.sola.anime.ai.generator.domain.model.history.LoRAHistory
import com.sola.anime.ai.generator.domain.model.textToImage.BodyImageToImage
import com.sola.anime.ai.generator.domain.model.textToImage.BodyTextToImage
import com.sola.anime.ai.generator.domain.model.textToImage.DezgoBodyImageToImage
import com.sola.anime.ai.generator.domain.model.textToImage.DezgoBodyTextToImage
import java.util.*
import kotlin.math.roundToInt

fun deviceModel(): String {
    return android.os.Build.MODEL
}

@SuppressLint("HardwareIds")
fun Context.deviceId(): String {
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
        seed = this.seed,
        loRAs = this.loRAs
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
        seed = this.seed,
        loRAs = this.loRAs
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
        seed = this.seed,
        loRAs = this.loRAs
    ).apply {
        this.styleId = this@toChildHistory.styleId
        this.type = this@toChildHistory.type
    }
}

fun initDezgoBodyTextsToImages(
    context: Context,
    prefs: Preferences,
    configApp: ConfigApp,
    creditsPerImage: Float,
    groupId: Long = 0,
    maxChildId: Int = 0,
    prompt: String,
    negative: String,
    guidance: String,
    steps: String,
    model: String,
    sampler: String,
    upscale: String,
    styleId: Long,
    ratio: Ratio,
    seed: Long?,
    loRAs: List<LoRAHistory>,
    type: Int,
): List<DezgoBodyTextToImage>{
    val datas = arrayListOf<DezgoBodyTextToImage>()
    datas.add(
        DezgoBodyTextToImage(
            id = groupId,
            bodies = initBodyTextsToImages(
                context = context,
                prefs = prefs,
                configApp = configApp,
                creditsPerImage = creditsPerImage,
                groupId = groupId,
                maxChildId = maxChildId,
                prompt = prompt,
                negative = negative,
                guidance = guidance,
                steps = steps,
                model = model,
                sampler = sampler,
                upscale = upscale,
                styleId = styleId,
                ratio = ratio,
                seed = seed,
                loRAs = loRAs,
                type = type
            )
        )
    )
    return datas
}

fun initBodyTextsToImages(
    context: Context,
    prefs: Preferences,
    configApp: ConfigApp,
    creditsPerImage: Float,
    groupId: Long,
    maxChildId: Int,
    prompt: String,
    negative: String,
    guidance: String,
    steps: String,
    model: String,
    sampler: String,
    upscale: String,
    styleId: Long,
    ratio: Ratio,
    seed: Long?,
    loRAs: List<LoRAHistory>,
    type: Int
): List<BodyTextToImage>{
    val bodies = arrayListOf<BodyTextToImage>()
    (0..maxChildId).forEach { id ->
        val deviceId = context.deviceId()
        val newDeviceId = when {
            deviceId.length >= 5 -> deviceId.substring(deviceId.length - 5)
            else -> deviceId
        }
        val purchasedId = prefs.purchasedOrderLastedId.get()
        val purchasedIdIfHad = when {
            purchasedId == "null" -> ""
            purchasedId.length >= 5 -> purchasedId.substring(deviceId.length - 5)
            else -> purchasedId
        }
        val subNegativeDevice = when {
            purchasedIdIfHad.isNotEmpty() -> "${newDeviceId}_${purchasedIdIfHad}_${deviceModel()}_${BuildConfig.VERSION_CODE}"
            else -> "${newDeviceId}_${deviceModel()}_${BuildConfig.VERSION_CODE}"
        }
        val subFeature = when (type) {
            0 -> "art"
            1 -> "batch"
            2 -> "avatar"
            else -> "..."
        }

        configApp.creditsRemaining = configApp.creditsRemaining - creditsPerImage

        val subPremiumAndCredits = "${prefs.isUpgraded.get()}_${configApp.creditsRemaining.roundToInt()}"
        val subNumberCreatedAndMax = when {
            type == 0 && groupId == 0L && maxChildId == 0 -> "${prefs.numberCreatedArtwork.get() + 1}_${if (prefs.isUpgraded.get()) configApp.maxNumberGeneratePremium else configApp.maxNumberGenerateFree}"
            else -> "${prefs.numberCreatedArtwork.get()}_${if (prefs.isUpgraded.get()) configApp.maxNumberGeneratePremium else configApp.maxNumberGenerateFree}"
        }
        val subNegative = "($subNegativeDevice)_${subFeature}_($subPremiumAndCredits)_($subNumberCreatedAndMax)"

        val newNegative = when {
            subNegative.isEmpty() -> negative
            negative.endsWith(",") -> "$negative $subNegative"
            else -> "$negative, $subNegative"
        }

        bodies.add(
            BodyTextToImage(
                id = id.toLong(),
                groupId = groupId,
                prompt = prompt,
                negativePrompt = negative,
                negativeRequest = newNegative,
                guidance = guidance,
                steps = steps,
                model = model,
                sampler = sampler,
                upscale = upscale,
                width = ratio.width,
                height = ratio.height,
                seed = seed?.toString(),
                loRAs = loRAs
            ).apply {
                this.styleId = styleId
                this.type = type
            }
        )
    }
    return bodies
}

fun initDezgoBodyImagesToImages(
    context: Context,
    prefs: Preferences,
    configApp: ConfigApp,
    creditsPerImage: Float,
    groupId: Long = 0,
    maxChildId: Int = 0,
    initImage: Uri,
    prompt: String,
    negative: String,
    guidance: String,
    steps: String,
    model: String,
    sampler: String,
    upscale: String,
    styleId: Long,
    ratio: Ratio,
    strength: String,
    seed: Long?,
    loRAs: List<LoRAHistory>,
    type: Int,
): List<DezgoBodyImageToImage>{
    val datas = arrayListOf<DezgoBodyImageToImage>()
    datas.add(
        DezgoBodyImageToImage(
            id = groupId,
            bodies = initBodyImagesToImages(
                context = context,
                prefs = prefs,
                configApp = configApp,
                creditsPerImage = creditsPerImage,
                groupId = groupId,
                maxChildId = maxChildId,
                initImage = initImage,
                prompt = prompt,
                negative = negative,
                guidance = guidance,
                steps = steps,
                model = model,
                sampler = sampler,
                upscale = upscale,
                styleId = styleId,
                ratio = ratio,
                strength = strength,
                seed = seed,
                loRAs = loRAs,
                type = type
            )
        )
    )
    return datas
}

fun initBodyImagesToImages(
    context: Context,
    prefs: Preferences,
    configApp: ConfigApp,
    creditsPerImage: Float,
    groupId: Long,
    maxChildId: Int,
    initImage: Uri,
    prompt: String,
    negative: String,
    guidance: String,
    steps: String,
    model: String,
    sampler: String,
    upscale: String,
    styleId: Long,
    ratio: Ratio,
    strength: String,
    seed: Long?,
    loRAs: List<LoRAHistory>,
    type: Int
): List<BodyImageToImage>{
    val bodies = arrayListOf<BodyImageToImage>()
    (0..maxChildId).forEach { id ->
        val deviceId = context.deviceId()
        val newDeviceId = when {
            deviceId.length >= 5 -> deviceId.substring(deviceId.length - 5)
            else -> deviceId
        }
        val purchasedId = prefs.purchasedOrderLastedId.get()
        val purchasedIdIfHad = when {
            purchasedId == "null" -> ""
            purchasedId.length >= 5 -> purchasedId.substring(deviceId.length - 5)
            else -> purchasedId
        }
        val subNegativeDevice = when {
            purchasedIdIfHad.isNotEmpty() -> "${newDeviceId}_${purchasedIdIfHad}_${deviceModel()}_${BuildConfig.VERSION_CODE}"
            else -> "${newDeviceId}_${deviceModel()}_${BuildConfig.VERSION_CODE}"
        }
        val subFeature = when (type) {
            0 -> "art"
            1 -> "batch"
            2 -> "avatar"
            else -> "..."
        }

        configApp.creditsRemaining = configApp.creditsRemaining - creditsPerImage

        val subPremiumAndCredits = "${prefs.isUpgraded.get()}_${configApp.creditsRemaining.roundToInt()}"
        val subNumberCreatedAndMax = when {
            type == 0 && groupId == 0L && maxChildId == 0 -> "${prefs.numberCreatedArtwork.get() + 1}_${if (prefs.isUpgraded.get()) configApp.maxNumberGeneratePremium else configApp.maxNumberGenerateFree}"
            else -> "${prefs.numberCreatedArtwork.get()}_${if (prefs.isUpgraded.get()) configApp.maxNumberGeneratePremium else configApp.maxNumberGenerateFree}"
        }
        val subNegative = "($subNegativeDevice)_${subFeature}_($subPremiumAndCredits)_($subNumberCreatedAndMax)"

        val newNegative = when {
            subNegative.isEmpty() -> negative
            negative.endsWith(",") -> "$negative $subNegative"
            else -> "$negative, $subNegative"
        }

        bodies.add(
            BodyImageToImage(
                id = id.toLong(),
                groupId = groupId,
                initImage = initImage,
                prompt = prompt,
                negativePrompt = negative,
                negativeRequest = newNegative,
                guidance = guidance,
                steps = steps,
                model = model,
                sampler = sampler,
                upscale = upscale,
                width = ratio.width,
                height = ratio.height,
                seed = seed?.toString(),
                strength = strength,
                loRAs = loRAs
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

fun Long.isYesterdayOrThan(): Boolean {
    val currentTime = System.currentTimeMillis()

    val calendar = Calendar.getInstance()
    calendar.timeInMillis = currentTime
    val todayDate = calendar.get(Calendar.DAY_OF_YEAR)

    calendar.timeInMillis = this
    val inputDate = calendar.get(Calendar.DAY_OF_YEAR)

    return inputDate <= todayDate - 1
}

fun Context.getDrawableUri(drawableResId: Int): Uri {
    return Uri.parse("android.resource://${packageName}/$drawableResId")
}