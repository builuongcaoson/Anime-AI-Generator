package com.basic.common.util.theme

import android.graphics.Typeface
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.basic.common.extension.getColorCompat
import com.basic.common.extension.setBackgroundTint
import com.basic.common.extension.setTint
import com.basic.common.widget.LsCardView
import com.basic.common.widget.LsConstraintView
import com.basic.R
import com.basic.common.widget.LsEditText
import com.basic.common.widget.LsFrameView
import com.basic.common.widget.LsImageView
import com.basic.common.widget.LsLinearView
import com.basic.common.widget.LsRelativeView
import com.basic.common.widget.LsTextView
import com.basic.common.widget.LsView
import com.basic.data.LsPrefs
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ViewStyler @Inject constructor() {

    companion object {
        fun applyAttributesView(view: LsTextView, attrs: AttributeSet?, textSizeDefaultAttr: Int = -1) {
            view.context.obtainStyledAttributes(attrs, R.styleable.LsTextView).run {
                val textColorAttr = getInt(R.styleable.LsTextView_textColor, ColorManager.COLOR_NONE)
                val textSizeAttr = getInt(R.styleable.LsTextView_textSize, LsPrefs.TEXT_SIZE_NORMAL)
                val textFontAttr = getInt(R.styleable.LsTextView_textFont, FontManager.FONT_REGULAR)

                val textColor = ColorManager.colorById(view.context, textColorAttr)
                val textFont = FontManager.fontById(view.context, textFontAttr)
                val textSize = SizeManager.sizeById(view.context, LsPrefs.TEXT_SIZE_NORMAL, textSizeAttr)

                view.run {
                    textColor?.let { color -> setTextColor(color) }
                    textFont?.let { typeface -> setTypeface(typeface, view.typeface?.style ?: Typeface.NORMAL) }
                    if (textSizeDefaultAttr != -1){
                        textSize?.let { size -> setTextSize(TypedValue.COMPLEX_UNIT_PX, size) }
                    }
                }

                recycle()
            }
        }

        fun applyAttributesView(view: TextView, attrs: AttributeSet?, textSizeDefaultAttr: Int = -1) {
            view.context.obtainStyledAttributes(attrs, R.styleable.LsTextView).run {
                val textColorAttr = getInt(R.styleable.LsTextView_textColor, ColorManager.COLOR_NONE)
                val textSizeAttr = getInt(R.styleable.LsTextView_textSize, LsPrefs.TEXT_SIZE_NORMAL)
                val textFontAttr = getInt(R.styleable.LsTextView_textFont, FontManager.FONT_REGULAR)

                val textColor = ColorManager.colorById(view.context, textColorAttr)
                val textFont = FontManager.fontById(view.context, textFontAttr)
                val textSize = SizeManager.sizeById(view.context, LsPrefs.TEXT_SIZE_NORMAL, textSizeAttr)

                view.run {
                    textColor?.let { color -> setTextColor(color) }
                    textFont?.let { typeface -> setTypeface(typeface, view.typeface?.style ?: Typeface.NORMAL) }
                    if (textSizeDefaultAttr != -1){
                        textSize?.let { size -> setTextSize(TypedValue.COMPLEX_UNIT_PX, size) }
                    }
                }

                recycle()
            }
        }

        fun applyAttributesView(view: LsEditText, attrs: AttributeSet?, textSizeDefaultAttr: Int = -1) {
            view.context.obtainStyledAttributes(attrs, R.styleable.LsEditText).run {
                val textColorAttr = getInt(R.styleable.LsEditText_textColor, ColorManager.COLOR_NONE)
                val textHintColorAttr = getInt(R.styleable.LsEditText_textHintColor, ColorManager.COLOR_NONE)
                val textSizeAttr = getInt(R.styleable.LsEditText_textSize, LsPrefs.TEXT_SIZE_NORMAL)
                val textFontAttr = getInt(R.styleable.LsEditText_textFont, FontManager.FONT_REGULAR)

                val textColor = ColorManager.colorById(view.context, textColorAttr)
                val textHintColor = ColorManager.colorById(view.context, textHintColorAttr)
                val textFont = FontManager.fontById(view.context, textFontAttr)
                val textSize = SizeManager.sizeById(view.context, LsPrefs.TEXT_SIZE_NORMAL, textSizeAttr)

                view.run {
                    textColor?.let { color -> setTextColor(color) }
                    textHintColor?.let { color -> setHintTextColor(color) }
                    textFont?.let { typeface -> setTypeface(typeface, view.typeface?.style ?: Typeface.NORMAL) }
                    textSize?.takeIf { textSizeDefaultAttr != -1 }?.let { size -> setTextSize(TypedValue.COMPLEX_UNIT_PX, size) }
                    val drawable = ContextCompat.getDrawable(context, R.drawable.cursor)?.apply { setTint(context.getColorCompat(R.color.textPrimaryDark)) }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        textCursorDrawable = drawable
                    }
                }

                recycle()
            }
        }

        fun applyAttributesView(view: LsImageView, attrs: AttributeSet?) {
            view.context.obtainStyledAttributes(attrs, R.styleable.LsImageView).run {
                val colorTintAttr = getInt(R.styleable.LsImageView_colorTint, ColorManager.COLOR_NONE)
                val bgColorTintAttr = getInt(R.styleable.LsImageView_bgColorTint, ColorManager.COLOR_NONE)
                val colorTint = ColorManager.colorById(view.context, colorTintAttr)
                val bgColorTint = ColorManager.colorById(view.context, bgColorTintAttr)

                view.run {
                    colorTint?.let { color -> setTint(color) }
                    bgColorTint?.let { color -> setBackgroundTint(color) }
                }

                recycle()
            }
        }

        fun applyAttributesView(view: LsCardView, attrs: AttributeSet?) {
            view.context.obtainStyledAttributes(attrs, R.styleable.LsCardView).run {
                val bgColorAttr = getInt(R.styleable.LsCardView_bgColor, ColorManager.COLOR_NONE)
                val strokeColorAttr = getInt(R.styleable.LsCardView_strColor, ColorManager.COLOR_NONE)
                val bgColor = ColorManager.colorById(view.context, bgColorAttr)
                val strokeColor = ColorManager.colorById(view.context, strokeColorAttr)

                view.run {
                    bgColor?.let { color -> setCardBackgroundColor(color) }
                    strokeColor?.let { color -> setStrokeColor(color) }
                }

                recycle()
            }
        }

        fun applyAttributesView(view: LsConstraintView, attrs: AttributeSet?) {
            view.context.obtainStyledAttributes(attrs, R.styleable.LsConstraintView).run {
                val bgColorTintAttr = getInt(R.styleable.LsConstraintView_bgColorTint, ColorManager.COLOR_NONE)
                val bgColorTint = ColorManager.colorById(view.context, bgColorTintAttr)

                view.run {
                    bgColorTint?.let { color -> setBackgroundTint(color) }
                }

                recycle()
            }
        }

        fun applyAttributesView(view: ConstraintLayout, attrs: AttributeSet?) {
            view.context.obtainStyledAttributes(attrs, R.styleable.LsConstraintView).run {
                val bgColorTintAttr = getInt(R.styleable.LsConstraintView_bgColorTint, ColorManager.COLOR_NONE)
                val bgColorTint = ColorManager.colorById(view.context, bgColorTintAttr)

                view.run {
                    bgColorTint?.let { color -> setBackgroundTint(color) }
                }

                recycle()
            }
        }

        fun applyAttributesView(view: LsLinearView, attrs: AttributeSet?) {
            view.context.obtainStyledAttributes(attrs, R.styleable.LsLinearView).run {
                val bgColorTintAttr = getInt(R.styleable.LsLinearView_bgColorTint, ColorManager.COLOR_NONE)
                val bgColorTint = ColorManager.colorById(view.context, bgColorTintAttr)

                view.run {
                    bgColorTint?.let { color -> setBackgroundTint(color) }
                }

                recycle()
            }
        }

        fun applyAttributesView(view: LsRelativeView, attrs: AttributeSet?) {
            view.context.obtainStyledAttributes(attrs, R.styleable.LsRelativeView).run {
                val bgColorTintAttr = getInt(R.styleable.LsRelativeView_bgColorTint, ColorManager.COLOR_NONE)
                val bgColorTint = ColorManager.colorById(view.context, bgColorTintAttr)

                view.run {
                    bgColorTint?.let { color -> setBackgroundTint(color) }
                }

                recycle()
            }
        }

        fun applyAttributesView(view: LsFrameView, attrs: AttributeSet?) {
            view.context.obtainStyledAttributes(attrs, R.styleable.LsFrameView).run {
                val bgColorTintAttr = getInt(R.styleable.LsFrameView_bgColorTint, ColorManager.COLOR_NONE)
                val bgColorTint = ColorManager.colorById(view.context, bgColorTintAttr)

                view.run {
                    bgColorTint?.let { color -> setBackgroundTint(color) }
                }

                recycle()
            }
        }

        fun applyAttributesView(view: LsView, attrs: AttributeSet?) {
            view.context.obtainStyledAttributes(attrs, R.styleable.LsView).run {
                val bgColorTintAttr = getInt(R.styleable.LsView_bgColorTint, ColorManager.COLOR_NONE)
                val bgColorTint = ColorManager.colorById(view.context, bgColorTintAttr)

                view.run {
                    bgColorTint?.let { color -> setBackgroundTint(color) }
                }

                recycle()
            }
        }
    }

}