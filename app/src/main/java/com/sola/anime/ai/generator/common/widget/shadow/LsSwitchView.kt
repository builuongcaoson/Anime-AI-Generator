package com.sola.anime.ai.generator.common.widget.shadow

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import com.basic.common.extension.*
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.common.extension.addShadow
import com.sola.anime.ai.generator.common.extension.animateBackground

@SuppressLint("ClickableViewAccessibility")
class LsSwitchView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0,
) : LsSwitchFrameLayout(context, attrs, defStyleAttr) {

    private var thumb: ImageView
    private var track: LsSwitchFrameLayout
    private var switchCallbacks: LsSwitchCallbacks? = null

    private var isChecked: Boolean = false

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.switch_view, this, true)

        thumb = view.findViewById(R.id.switch_thumb)
        track = view.findViewById(R.id.switch_track)

        track.addShadow(context.resolveAttrColor(android.R.attr.colorAccent))

        view.setOnClickListener {
            isChecked = if (isChecked) {
                animateUnchecked()
                switchCallbacks?.onCheckedChanged(false)
                false
            } else {
                animateChecked()
                switchCallbacks?.onCheckedChanged(true)
                true
            }
        }

        animateUnchecked()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                thumb.animate()
                        .scaleY(1.5F)
                        .scaleX(1.5F)
                        .setInterpolator(DecelerateInterpolator(1.5F))
                        .setDuration(500L)
                        .start()
            }
            MotionEvent.ACTION_MOVE,
            MotionEvent.ACTION_UP,
            -> {
                thumb.animate()
                        .scaleY(1.0F)
                        .scaleX(1.0F)
                        .setInterpolator(DecelerateInterpolator(1.5F))
                        .setDuration(500L)
                        .start()
            }
        }

        return super.onTouchEvent(event)
    }

    fun setChecked(boolean: Boolean) {
        isChecked = if (boolean) {
            animateChecked()
            boolean
        } else {
            animateUnchecked()
            boolean
        }
    }

    private fun animateUnchecked() {
        thumb.animate()
                .translationX(0F)
                .setInterpolator(OvershootInterpolator(3F))
                .setDuration(500)
                .start()

        track.animateBackground(context.getColorCompat(R.color.switchTrackOff))
        animateElevation(0F)
    }

    private fun animateChecked() {
        val w =  context.getDimens(com.intuit.sdp.R.dimen._40sdp)
        val p = context.getDimens(com.intuit.sdp.R.dimen._3sdp)
        val thumbWidth = context.getDimens(com.intuit.sdp.R.dimen._20sdp)

        thumb.animate()
                .translationX((w - p * 2 - thumbWidth))
                .setInterpolator(OvershootInterpolator(3F))
                .setDuration(500)
                .start()

        track.animateBackground(context.resolveAttrColor(android.R.attr.colorAccent))
        animateElevation(25F)
    }

    private fun animateElevation(elevation: Float) {
        val valueAnimator = ValueAnimator.ofFloat(track.elevation, elevation)
        valueAnimator.duration = 500L
        valueAnimator.interpolator = DecelerateInterpolator(1.5F)
        valueAnimator.addUpdateListener {
            track.elevation = it.animatedValue as Float
        }
        valueAnimator.start()
    }

    fun setOnSwitchCheckedChangeListener(switchCallbacks: LsSwitchCallbacks) {
        this.switchCallbacks = switchCallbacks
    }
}