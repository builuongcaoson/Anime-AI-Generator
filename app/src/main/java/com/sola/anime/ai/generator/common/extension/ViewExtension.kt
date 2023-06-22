package com.sola.anime.ai.generator.common.extension

import android.view.ViewGroup
import eightbitlab.com.blurview.BlurView

fun BlurView.blur(rootView: ViewGroup, ratioBlur: Float = 20f){
    this.setupWith(rootView, context.getBlurAlgorithm())
        .setFrameClearDrawable(rootView.background)
        .setBlurRadius(ratioBlur)
}