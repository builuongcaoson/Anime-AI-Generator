package com.sola.anime.ai.generator.common

import android.content.Context
import android.content.Intent
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.domain.model.PreviewIap
import com.sola.anime.ai.generator.feature.iap.IapActivity
import com.sola.anime.ai.generator.feature.main.MainActivity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConfigApp @Inject constructor(
    private val context: Context
){

    var previewsIap1 = listOf<PreviewIap>()
    var previewsIap2 = listOf<PreviewIap>()
    var previewsIap3 = listOf<PreviewIap>()

}