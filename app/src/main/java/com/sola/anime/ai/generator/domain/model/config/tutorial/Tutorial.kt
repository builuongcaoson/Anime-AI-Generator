package com.sola.anime.ai.generator.domain.model.config.tutorial

import androidx.annotation.DrawableRes
import com.sola.anime.ai.generator.R

enum class TutorialStep(val display: String, @DrawableRes val preview: Int, val childs: List<TutorialStep2>){

}

data class TutorialStep2(val display: String, @DrawableRes val preview: Int, val childs: List<TutorialStep3>)

data class TutorialStep3(val display: String, @DrawableRes val preview: Int)