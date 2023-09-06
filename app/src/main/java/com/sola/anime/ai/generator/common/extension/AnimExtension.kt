package com.sola.anime.ai.generator.common.extension

import android.animation.ValueAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.Interpolator

import android.view.animation.TranslateAnimation
import androidx.core.animation.doOnEnd

fun animInt(from: Int, to: Int, duration: Long, update: (Int) -> Unit, endAction: () -> Unit = {}){
    val colorAnimation = ValueAnimator.ofInt(from, to)
    colorAnimation.duration = duration // milliseconds
    colorAnimation.addUpdateListener {
        update(it.animatedValue as Int)
    }
    colorAnimation.doOnEnd { endAction() }
    colorAnimation.start()
}

fun View.animateHorizontalShake(
    offset: Float,
    repeatCount: Int = 3,
    dampingRatio: Float? = null,
    duration: Long = 1000L,
    interpolator: Interpolator = AccelerateDecelerateInterpolator()
) {
    val defaultDampingRatio = dampingRatio ?: (1f / (repeatCount + 1))
    val animValues = mutableListOf<Float>()
    repeat(repeatCount) { index ->
        animValues.add(0f)
        animValues.add(-offset * (1 - defaultDampingRatio * index))
        animValues.add(0f)
        animValues.add(offset * (1 - defaultDampingRatio * index))
    }
    animValues.add(0f)

    val anim: ValueAnimator = ValueAnimator.ofFloat(*animValues.toFloatArray())
    anim.addUpdateListener {
        translationX = it.animatedValue as Float
    }
    anim.interpolator = interpolator
    anim.duration = duration
    anim.start()
}