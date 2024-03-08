package com.basic.common.widget

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.TypedValue
import androidx.appcompat.widget.AppCompatTextView
import com.basic.R
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
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class LsTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatTextView(context, attrs), UIChangedListener {

    @Inject lateinit var fontManager: FontManager
    @Inject lateinit var colorManager: ColorManager

    // For language
    private var textResourceId: Int = -1
    // For theme
    private var valueAnimator: ValueAnimator? = null
    private var textColorAttr = ColorManager.COLOR_NONE
    private var textLinkColorAttr = ColorManager.COLOR_NONE
    private var textFontAttr = FontManager.FONT_REGULAR
    private var textSizeAttr = -1

    init {
        context.obtainStyledAttributes(attrs, R.styleable.LsTextView).run {
            textColorAttr = getInt(R.styleable.LsTextView_textColor, ColorManager.COLOR_NONE)
            textLinkColorAttr = getInt(R.styleable.LsTextView_textLinkColor, ColorManager.COLOR_NONE)
            textFontAttr = getInt(R.styleable.LsTextView_textFont, FontManager.FONT_REGULAR)
            textSizeAttr = getInt(R.styleable.LsTextView_textSize, -1)
            recycle()
        }

        context.obtainStyledAttributes(attrs, intArrayOf(android.R.attr.text)).run {
            textResourceId = getResourceId(0, -1)
            recycle()
        }

        if (!isInEditMode) {
            fontManager.get(textFontAttr) { typeface -> setTypeface(typeface, this.typeface?.style ?: Typeface.NORMAL) }

            if (textSizeAttr != -1){
                SizeManager.sizeById(context, LsPrefs.TEXT_SIZE_NORMAL, textSizeAttr)?.let { textSize ->
                    setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
                }
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

    fun setTextFont(textFontAttr: Int) {
        when (textFontAttr) {
            FontManager.FONT_REGULAR -> {
                setTypeface(null, Typeface.NORMAL)
            }
            else -> {
                fontManager.get(textFontAttr) { typeFace ->
                    setTypeface(typeFace, typeface?.style ?: Typeface.NORMAL)
                }
            }
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
        colorManager.theme().colorById(textLinkColorAttr)?.let { color ->
            if (animate) {
                valueAnimator = animateTextLinkColorChange(endColor = color)
            } else {
                setLinkTextColor(ColorStateList.valueOf(color))
            }
        }
    }

    override fun onThemeChanged(withAnim: Boolean) {
        updateUi(withAnim)
    }

    override fun onLanguageChanged(withAnim: Boolean?) {
        textResourceId.takeIf { it != -1 }?.let { textResourceId ->
            text = context.getString(textResourceId)
        }
        Timber.e("TextResourceId: $textResourceId --- ${textResourceId.takeIf { it != -1 }?.let { textResourceId -> context.getString(textResourceId) }}")
    }

}