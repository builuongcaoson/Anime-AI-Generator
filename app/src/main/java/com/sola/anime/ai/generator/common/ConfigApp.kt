package com.sola.anime.ai.generator.common

import android.content.Context
import com.basic.common.extension.tryOrNull
import com.sola.anime.ai.generator.R
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

    val firstPreviews by lazy {
        listOf(
            R.drawable.first_preview_zzz1xxx1zzz_1,
            R.drawable.first_preview_zzz9xxx16zzz_2,
            R.drawable.first_preview_zzz16xxx9zzz_3,
            R.drawable.first_preview_zzz3xxx4zzz_4,
            R.drawable.first_preview_zzz4xxx3zzz_5,
            R.drawable.first_preview_zzz2xxx3zzz_6,
            R.drawable.first_preview_zzz3xxx2zzz_7,
            R.drawable.first_preview_zzz1xxx1zzz_8,
            R.drawable.first_preview_zzz9xxx16zzz_9,
            R.drawable.first_preview_zzz16xxx9zzz_10,
            R.drawable.first_preview_zzz3xxx4zzz_11,
            R.drawable.first_preview_zzz4xxx3zzz_12,
            R.drawable.first_preview_zzz2xxx3zzz_13,
            R.drawable.first_preview_zzz3xxx2zzz_14,
            R.drawable.first_preview_zzz1xxx1zzz_15,
            R.drawable.first_preview_zzz9xxx16zzz_16,
            R.drawable.first_preview_zzz16xxx9zzz_17,
            R.drawable.first_preview_zzz3xxx4zzz_18,
            R.drawable.first_preview_zzz4xxx3zzz_19,
            R.drawable.first_preview_zzz2xxx3zzz_20,
            R.drawable.first_preview_zzz3xxx2zzz_21
        ).map { res ->
            val ratio = tryOrNull { context.resources.getResourceName(res).split("zzz").getOrNull(1)?.replace("xxx",":") } ?: "1:1"

            FirstPreview(res, ratio)
        }
    }

    // Art & Batch
    var dezgoBodiesTextsToImages: List<DezgoBodyTextToImage> = listOf()

    // RxJava
    var subjectRatioClicks: Subject<Ratio> = BehaviorSubject.createDefault(Ratio.Ratio1x1)
    var subjectStyleClicks: Subject<Long> = BehaviorSubject.createDefault(-1)
    var subjectExploreClicks: Subject<Long> = BehaviorSubject.createDefault(-1)

}