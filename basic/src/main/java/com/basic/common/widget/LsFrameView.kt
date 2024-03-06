package com.basic.common.widget

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.widget.FrameLayout
import com.basic.R
import com.basic.common.extension.animBackgroundColorChange
import com.basic.common.util.theme.ColorManager
import com.basic.common.util.theme.UIChangedListener
import com.basic.common.util.theme.UIManager
import com.basic.common.util.theme.ViewStyler
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LsFrameView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs), UIChangedListener {

    @Inject lateinit var colorManager: ColorManager

    private var valueAnimator: ValueAnimator? = null
    private var backgroundTintAttr = ColorManager.COLOR_NONE
    private var hadBackgroundAttr = false

    init {
        context.obtainStyledAttributes(attrs, intArrayOf(android.R.attr.background)).run {
            hadBackgroundAttr = getResourceId(0, 0) != 0
            recycle()
        }

        context.obtainStyledAttributes(attrs, R.styleable.LsFrameView).run {
            backgroundTintAttr = getInt(R.styleable.LsFrameView_bgColorTint, ColorManager.COLOR_NONE)
            recycle()
        }

        if (!hadBackgroundAttr && backgroundTintAttr != ColorManager.COLOR_NONE){
            setBackgroundColor(Color.WHITE)
        }

        if (!isInEditMode) {
            setBackground(false)
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

    private fun setBackground(animate: Boolean) {
        colorManager.theme().colorById(backgroundTintAttr)?.let { color ->
            if (animate) {
                valueAnimator = animBackgroundColorChange(endColor = color)
            } else {
                backgroundTintList = ColorStateList.valueOf(color)
            }
        }
    }

    override fun onThemeChanged(withAnim: Boolean) {
        setBackground(withAnim)
    }

}