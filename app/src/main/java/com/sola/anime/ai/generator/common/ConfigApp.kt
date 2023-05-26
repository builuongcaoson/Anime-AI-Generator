package com.sola.anime.ai.generator.common

import android.content.Context
import com.afollestad.materialdialogs.utils.MDUtil.getStringArray
import com.sola.anime.ai.generator.R
import com.basic.common.extension.tryOrNull
import com.sola.anime.ai.generator.domain.model.Ratio
import com.sola.anime.ai.generator.domain.model.config.first.FirstPreview
import com.sola.anime.ai.generator.domain.model.textToImage.DezgoBodyTextToImage
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConfigApp @Inject constructor(
    private val context: Context
) {

    var scriptIap = "2" // 0: (Nothing), 1: (LifeTime - 3 Day Trial Week - Year), 2: (Lifetime - Month - Year)

    val sensitiveKeywords = context.getStringArray(R.array.sensitives)

    // Art & Batch
    var dezgoBodiesTextsToImages: List<DezgoBodyTextToImage> = listOf()

    // RxJava
    var subjectRatioClicks: Subject<Ratio> = BehaviorSubject.createDefault(Ratio.Ratio1x1)
    var subjectStyleClicks: Subject<Long> = BehaviorSubject.createDefault(-1)
    var subjectExploreClicks: Subject<Long> = BehaviorSubject.createDefault(-1)

}