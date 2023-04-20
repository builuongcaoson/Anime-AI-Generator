package com.sola.anime.ai.generator.common

import android.content.Context
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.domain.model.PreviewIap
import com.sola.anime.ai.generator.domain.model.config.ProcessPreview
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

}