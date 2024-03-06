package com.basic.common.extension

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.os.Build
import android.os.SystemClock
import android.util.TypedValue
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.basic.common.widget.scale.PushDownAnim
import com.google.android.material.animation.ArgbEvaluatorCompat
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.RelativeCornerSize
import com.google.android.material.shape.RoundedCornerTreatment

fun View.clicks(debounce: Long = 250, withAnim: Boolean = true, clicks: (View) -> Unit) {
    if (withAnim){
        var lastClickTime: Long = 0
        PushDownAnim.setPushDownAnimTo(this)
            .setOnClickListener {
                if (SystemClock.elapsedRealtime() - lastClickTime < debounce) return@setOnClickListener
                else clicks(this)
                lastClickTime = SystemClock.elapsedRealtime()
            }
    } else {
        var lastClickTime: Long = 0
        setOnClickListener {
            if (SystemClock.elapsedRealtime() - lastClickTime < debounce) return@setOnClickListener
            else clicks(this)
            lastClickTime = SystemClock.elapsedRealtime()
        }
    }
}

inline fun <reified T : RecyclerView.ViewHolder> RecyclerView.forEachVisibleHolder(
    action: (T) -> Unit
) {
    for (i in 0 until childCount) {
        action(getChildViewHolder(getChildAt(i)) as T)
    }
}

fun View.setBackgroundTint(color: Int) {
    // API 21 doesn't support this
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1) {
        background?.setColorFilter(color, PorterDuff.Mode.SRC_IN)
    }

    backgroundTintList = ColorStateList.valueOf(color)
}

fun View.setPaddingHorizontal(padding: Int) {
    setPadding(padding, paddingTop, padding, paddingBottom)
}

fun View.setPaddingVertical(padding: Int) {
    setPadding(paddingLeft, padding, paddingRight, padding)
}

fun EditText.showKeyboard() {
    requestFocus()
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}

fun EditText.hideKeyboard() {
    requestFocus()
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

fun ImageView.setTint(color: Int) {
    imageTintList = ColorStateList.valueOf(color)
}