package com.basic.common.widget

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import com.basic.R
import com.basic.common.extension.animateTextHintColorChange
import com.basic.common.extension.animateTextColorChange
import com.basic.common.extension.animateTextLinkColorChange
import com.basic.common.util.theme.ColorManager
import com.basic.common.util.theme.FontManager
import com.basic.common.util.theme.SizeManager
import com.basic.common.util.theme.UIChangedListener
import com.basic.common.util.theme.UIManager
import com.basic.common.util.theme.ViewStyler
import com.basic.data.LsPrefs
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LsEditText @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AppCompatEditText(context, attrs), UIChangedListener {

    @Inject lateinit var fontManager: FontManager
    @Inject lateinit var colorManager: ColorManager

    private var valueAnimator: ValueAnimator? = null
    private var textColorAttr = ColorManager.COLOR_NONE
    private var textHintColorAttr = ColorManager.COLOR_NONE
    private var textLinkColorAttr = ColorManager.COLOR_NONE
    private var textFontAttr = FontManager.FONT_REGULAR
    private var textSizeAttr = -1

    init {
        context.obtainStyledAttributes(attrs, R.styleable.LsEditText).run {
            textColorAttr = getInt(R.styleable.LsEditText_textColor, ColorManager.COLOR_NONE)
            textHintColorAttr = getInt(R.styleable.LsEditText_textHintColor, ColorManager.COLOR_NONE)
            textLinkColorAttr = getInt(R.styleable.LsEditText_textLinkColor, ColorManager.COLOR_NONE)
            textFontAttr = getInt(R.styleable.LsEditText_textFont, FontManager.FONT_REGULAR)
            textSizeAttr = getInt(R.styleable.LsEditText_textSize, -1)
            recycle()
        }

        if (!isInEditMode) {
            fontManager.get(textFontAttr) { typeface -> setTypeface(typeface, this.typeface?.style ?: Typeface.NORMAL) }

            if (textSizeAttr != -1){
                SizeManager.sizeById(context, LsPrefs.TEXT_SIZE_NORMAL, textSizeAttr)?.let { textSize ->
                    setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val drawable = ContextCompat.getDrawable(context, R.drawable.cursor)?.apply { setTint(colorManager.theme().textPrimary) }
                textCursorDrawable = drawable
            }

            updateUi(false)
        } else {
            ViewStyler.applyAttributesView(this, attrs, textSizeAttr)
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
        colorManager.theme().colorById(textColorAttr)?.let { color ->
            if (animate) {
                valueAnimator = animateTextColorChange(endColor = color)
            } else {
                setTextColor(ColorStateList.valueOf(color))
            }
        }
        colorManager.theme().colorById(textHintColorAttr)?.let { color ->
            if (animate) {
                valueAnimator = animateTextHintColorChange(endColor = color)
            } else {
                setHintTextColor(ColorStateList.valueOf(color))
            }
        }
        colorManager.theme().colorById(textLinkColorAttr)?.let { color ->
            if (animate) {
                valueAnimator = animateTextLinkColorChange(endColor = color)
            } else {
                setHintTextColor(ColorStateList.valueOf(color))
            }
        }
    }

    override fun onThemeChanged(withAnim: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val drawable = ContextCompat.getDrawable(context, R.drawable.cursor)?.apply { setTint(colorManager.theme().textPrimary) }
            textCursorDrawable = drawable
        }

        updateUi(withAnim)
    }

    override fun onLanguageChanged(withAnim: Boolean?) {

    }

}