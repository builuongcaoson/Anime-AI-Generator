package com.sola.anime.ai.generator.common.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

class NonSwipeViewPager(context: Context, attrs: AttributeSet) : ViewPager(context, attrs) {

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // Disable touch events
        return false
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        // Disable touch events
        return false
    }
}