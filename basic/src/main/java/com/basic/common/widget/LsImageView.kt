package com.basic.common.widget

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.basic.R
import com.basic.common.extension.animateBackgroundTintColorChange
import com.basic.common.extension.animateTintColorChange
import com.basic.common.util.theme.ColorManager
import com.basic.common.util.theme.UIChangedListener
import com.basic.common.util.theme.UIManager
import com.basic.common.util.theme.ViewStyler
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LsImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs),
    UIChangedListener {

    @Inject lateinit var colorManager: ColorManager

    private var valueAnimator: ValueAnimator? = null
    private var colorTintAttr = ColorManager.COLOR_NONE
    private var backgroundColorTintAttr = ColorManager.COLOR_NONE

    init {
        context.obtainStyledAttributes(attrs, R.styleable.LsImageView).run {
            colorTintAttr = getInt(R.styleable.LsImageView_colorTint, ColorManager.COLOR_NONE)
            backgroundColorTintAttr = getInt(R.styleable.LsImageView_bgColorTint, ColorManager.COLOR_NONE)
            recycle()
        }

        if (!isInEditMode) {
            setTint(false)
            updateUi(false)
        } else {
            ViewStyler.applyAttributesView(this, attrs)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        UIManager.addListener(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        UIManager.removeListener(this)
        if (valueAnimator != null) {
            valueAnimator!!.cancel()
        }
    }

    private fun setTint(animate: Boolean) {
        colorManager.theme().colorById(colorTintAttr)?.let { color ->
            if (animate) {
                valueAnimator = animateTintColorChange(endColor = color)
            } else {
                imageTintList = ColorStateList.valueOf(color)
            }
        }
    }

    fun updateUi(animate: Boolean) {
        colorManager.theme().colorById(backgroundColorTintAttr)?.let { color ->
            if (animate) {
                valueAnimator = animateBackgroundTintColorChange(endColor = color)
            } else {
                backgroundTintList = ColorStateList.valueOf(color)
            }
        }
    }

    override fun onThemeChanged(withAnim: Boolean) {
        setTint(withAnim)
        updateUi(withAnim)
    }

}