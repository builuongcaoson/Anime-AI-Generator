package com.sola.anime.ai.generator.common.extension

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.TypedValue
import android.view.WindowInsets

@SuppressLint("InternalInsetResource", "DiscouragedApi")
fun Activity.getStatusBarHeight(): Int {
    var statusBarHeight = 0
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val windowInsets = window?.decorView?.rootWindowInsets
        windowInsets?.let {
            val insets = it.getInsets(WindowInsets.Type.statusBars())
            statusBarHeight = insets.top
        }
    } else {
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            statusBarHeight = resources.getDimensionPixelSize(resourceId)
        }
    }
    return statusBarHeight
}

fun Context.convertIntToDimension(value: Int): Float {
    val displayMetrics = resources.displayMetrics
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value.toFloat(), displayMetrics)
}
