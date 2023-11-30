package com.sola.anime.ai.generator.common.widget

import android.os.SystemClock

class FrameRateCounter {
    private var lastTime: Long = 0

    fun timeStep(): Float {
        val time = SystemClock.uptimeMillis()
        val timeDelta = time - lastTime
        val timeDeltaSeconds = if (lastTime > 0.0f) timeDelta / 1000.0f else 0.0f
        lastTime = time
        return Math.min(0.015f, timeDeltaSeconds)
    }
}