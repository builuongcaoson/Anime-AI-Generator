package com.basic.common.widget

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import com.basic.R
import com.basic.common.extension.animCardBackgroundColorChange
import com.basic.common.extension.animCardStrokeColorChange
import com.basic.common.util.theme.ColorManager
import com.basic.common.util.theme.UIChangedListener
import com.basic.common.util.theme.UIManager
import com.basic.common.util.theme.ViewStyler
import com.google.android.material.card.MaterialCardView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LsCardView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : MaterialCardView(context, attrs),
    UIChangedListener {

    @Inject lateinit var colorManager: ColorManager

    private var valueAnimator: ValueAnimator? = null
    private var backgroundColorAttr = ColorManager.COLOR_NONE
    private var strokeColorAttr = ColorManager.COLOR_NONE

    init {
        context.obtainStyledAttributes(attrs, R.styleable.LsCardView).run {
            backgroundColorAttr = getInt(R.styleable.LsCardView_bgColor, ColorManager.COLOR_NONE)
            strokeColorAttr = getInt(R.styleable.LsCardView_strColor, ColorManager.COLOR_NONE)
            recycle()
        }

        if (!isInEditMode) {
            if (backgroundColorAttr != ColorManager.COLOR_NONE){
                setCardBackgroundColor(Color.WHITE)
            }

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

    fun updateUi(animate: Boolean) {
        colorManager.theme().colorById(backgroundColorAttr)?.let { color ->
            if (animate) {
                valueAnimator = animCardBackgroundColorChange(endColor = color)
            } else {
                setCardBackgroundColor(ColorStateList.valueOf(color))
            }
        }
        colorManager.theme().colorById(strokeColorAttr)?.let { color ->
            if (animate) {
                valueAnimator = animCardStrokeColorChange(endColor = color)
            } else {
                setStrokeColor(ColorStateList.valueOf(color))
            }
        }
    }

    override fun onThemeChanged(withAnim: Boolean) {
        updateUi(withAnim)
    }

}