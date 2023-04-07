package com.basic.common.widget.switchview

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.setPadding
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel

open class SwitchFrameLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr) {

    init {
        layoutParams = LayoutParams(context.resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._40sdp), ViewGroup.LayoutParams.WRAP_CONTENT)
        setPadding(context.resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._5sdp))
        minimumWidth = resources.getDimensionPixelOffset(com.intuit.sdp.R.dimen._40sdp)

        backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)

        val shapeAppearanceModel = ShapeAppearanceModel()
            .toBuilder()
            .setAllCorners(CornerFamily.ROUNDED, 100F)
            .build()

        background = MaterialShapeDrawable(shapeAppearanceModel)
    }
}