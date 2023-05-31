package com.sola.anime.ai.generator.common.util

import android.content.Context
import android.util.DisplayMetrics
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.basic.common.extension.tryOrNull

class AutoScrollLayoutManager(val context: Context, val scrollSpeed: Float = 5000F) :
    LinearLayoutManager(context, RecyclerView.HORIZONTAL, false) {

    override fun canScrollHorizontally(): Boolean {
        return false
    }

    override fun canScrollVertically(): Boolean {
        return false
    }

    override fun smoothScrollToPosition(
        recyclerView: RecyclerView,
        state: RecyclerView.State,
        position: Int
    ) {
        val smoothScroller: LinearSmoothScroller = object : LinearSmoothScroller(context) {
            override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
                return scrollSpeed / displayMetrics.densityDpi
            }
        }
        tryOrNull { smoothScroller.targetPosition = position }
        tryOrNull { startSmoothScroll(smoothScroller) }
    }
}
