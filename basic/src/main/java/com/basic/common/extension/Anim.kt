package com.basic.common.extension

import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import com.google.android.material.card.MaterialCardView

fun View.animBackgroundColorChange(endColor: Int, duration: Long = 250L): ValueAnimator? {
    val valueAnimator = ValueAnimator.ofArgb(backgroundTintList!!.defaultColor, endColor)
    valueAnimator.duration = duration
    valueAnimator.interpolator = LinearOutSlowInInterpolator()
    valueAnimator.addUpdateListener { animation -> backgroundTintList = ColorStateList.valueOf(animation.animatedValue as Int) }
    valueAnimator.start()
    return valueAnimator
}

fun MaterialCardView.animCardBackgroundColorChange(endColor: Int, duration: Long = 250L): ValueAnimator? {
    val valueAnimator = ValueAnimator.ofArgb(cardBackgroundColor.defaultColor, endColor)
    valueAnimator.duration = duration
    valueAnimator.interpolator = LinearOutSlowInInterpolator()
    valueAnimator.addUpdateListener { animation -> setCardBackgroundColor(ColorStateList.valueOf(animation.animatedValue as Int)) }
    valueAnimator.start()
    return valueAnimator
}

fun MaterialCardView.animCardStrokeColorChange(endColor: Int, duration: Long = 250L): ValueAnimator? {
    val valueAnimator = ValueAnimator.ofArgb(strokeColorStateList!!.defaultColor, endColor)
    valueAnimator.duration = duration
    valueAnimator.interpolator = LinearOutSlowInInterpolator()
    valueAnimator.addUpdateListener { animation -> setStrokeColor(ColorStateList.valueOf(animation.animatedValue as Int)) }
    valueAnimator.start()
    return valueAnimator
}

fun TextView.animateTextColorChange(endColor: Int, duration: Long = 250L): ValueAnimator? {
    val valueAnimator = ValueAnimator.ofArgb(textColors.defaultColor, endColor)
    valueAnimator.duration = duration
    valueAnimator.interpolator = LinearOutSlowInInterpolator()
    valueAnimator.addUpdateListener { animation -> setTextColor(ColorStateList.valueOf(animation.animatedValue as Int)) }
    valueAnimator.start()
    return valueAnimator
}

fun TextView.animateTextLinkColorChange(endColor: Int, duration: Long = 250L): ValueAnimator? {
    val valueAnimator = ValueAnimator.ofArgb(linkTextColors.defaultColor, endColor)
    valueAnimator.duration = duration
    valueAnimator.interpolator = LinearOutSlowInInterpolator()
    valueAnimator.addUpdateListener { animation -> setLinkTextColor(ColorStateList.valueOf(animation.animatedValue as Int)) }
    valueAnimator.start()
    return valueAnimator
}

fun TextView.animateTextHintColorChange(endColor: Int, duration: Long = 250L): ValueAnimator? {
    val valueAnimator = ValueAnimator.ofArgb(hintTextColors.defaultColor, endColor)
    valueAnimator.duration = duration
    valueAnimator.interpolator = LinearOutSlowInInterpolator()
    valueAnimator.addUpdateListener { animation -> setHintTextColor(ColorStateList.valueOf(animation.animatedValue as Int)) }
    valueAnimator.start()
    return valueAnimator
}

fun ImageView.animateTintColorChange(endColor: Int, duration: Long = 250L): ValueAnimator? {
    val valueAnimator = ValueAnimator.ofArgb(imageTintList!!.defaultColor, endColor)
    valueAnimator.duration = duration
    valueAnimator.interpolator = LinearOutSlowInInterpolator()
    valueAnimator.addUpdateListener { animation -> imageTintList = ColorStateList.valueOf(animation.animatedValue as Int) }
    valueAnimator.start()
    return valueAnimator
}

fun ImageView.animateBackgroundTintColorChange(endColor: Int, duration: Long = 250L): ValueAnimator? {
    val valueAnimator = ValueAnimator.ofArgb(backgroundTintList!!.defaultColor, endColor)
    valueAnimator.duration = duration
    valueAnimator.interpolator = LinearOutSlowInInterpolator()
    valueAnimator.addUpdateListener { animation -> backgroundTintList = ColorStateList.valueOf(animation.animatedValue as Int) }
    valueAnimator.start()
    return valueAnimator
}