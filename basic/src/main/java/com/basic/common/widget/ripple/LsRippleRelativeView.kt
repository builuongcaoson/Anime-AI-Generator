package com.basic.common.widget.ripple

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.RelativeLayout
import com.widget.R
import com.basic.common.extension.addShadow
import com.basic.common.extension.getColorCompat

@SuppressLint("CustomViewStyleable")
class LsRippleRelativeView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : RelativeLayout(context, attrs) {

    init {
        setBackgroundColor(Color.TRANSPARENT)

        context.obtainStyledAttributes(attrs, R.styleable.LsRippleView).run {
            val radius = getFloat(R.styleable.LsRippleView_rippleRadius, 2f)

            background = Utils.getRippleDrawable(getContext(), background, radius, getColor(R.styleable.LsRippleView_rippleColor, context.getColorCompat(R.color.tools_theme)))

            getColor(R.styleable.LsRippleView_rippleShadow, -1).takeIf { it != -1 }?.let {
                addShadow(it)
            }

            recycle()
        }
    }

}