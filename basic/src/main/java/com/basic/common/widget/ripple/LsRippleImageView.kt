package com.basic.common.widget.ripple

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.widget.R
import com.basic.common.extension.getColorCompat

@SuppressLint("CustomViewStyleable")
class LsRippleImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs) {

    init {
        setBackgroundColor(Color.TRANSPARENT)

        context.obtainStyledAttributes(attrs, R.styleable.LsRippleView).run {
            background = Utils.getRippleDrawable(getContext(), background, 2f,
                getColor(R.styleable.LsRippleView_rippleColor, context.getColorCompat(R.color.tools_theme)))

            recycle()
        }
    }

}