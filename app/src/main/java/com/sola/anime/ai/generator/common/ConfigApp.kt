package com.sola.anime.ai.generator.common

import android.content.Context
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.domain.model.config.ProcessPreview
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConfigApp @Inject constructor(
    private val context: Context
) {

    // App
    var previewsInRes = listOf(
        ProcessPreview().apply {
            this.previewRes = R.drawable.preview_1
            this.title = "Noble Women"
            this.artist = "by: Serena"
        },
        ProcessPreview().apply {
            this.previewRes = R.drawable.preview_2
            this.title = "Shy Girl"
            this.artist = "by: Jennifer"
        },
        ProcessPreview().apply {
            this.previewRes = R.drawable.preview_3
            this.title = "Beautiful Angel"
            this.artist = "by: Christopher"
        }
    )

    // Art process
    var artProcessPreviews = listOf<ProcessPreview>()

    var subjectStyleClicks: Subject<Int> = BehaviorSubject.createDefault(-1) // Default No Style
    var subjectExploreClicks: Subject<Int> = BehaviorSubject.createDefault(-1) // Default No Explore

}