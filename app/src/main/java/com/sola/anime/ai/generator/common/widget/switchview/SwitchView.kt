package com.sola.anime.ai.generator.common.widget.switchview

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import com.basic.common.extension.getColorCompat
import com.basic.common.extension.resolveAttrColor
import com.basic.common.util.ColorUtils.animateColorChange
import com.basic.common.util.LocaleHelper.isRTL
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.common.widget.switchview.SwitchFrameLayout
import com.sola.anime.ai.generator.common.widget.switchview.util.ViewUtils

@SuppressLint("ClickableViewAccessibility")
class SwitchView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : SwitchFrameLayout(context, attrs, defStyleAttr) {

    private var thumb: ImageView
    private var switchCallbacks: SwitchCallbacks? = null

    private val tension = 3.5F
    val w = context.resources.getDimensionPixelOffset(com.intuit.sdp.R.dimen._40sdp)
    val p = context.resources.getDimensionPixelOffset(com.intuit.sdp.R.dimen._5sdp)
    private val thumbWidth = context.resources.getDimensionPixelOffset(com.intuit.sdp.R.dimen._15sdp)

    private var isChecked: Boolean = false

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.switch_view, this, true)

        thumb = view.findViewById(R.id.switch_thumb)

        clipChildren = false
        clipToPadding = false
        clipToOutline = false

        ViewUtils.addShadow(this)

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

        // Disable click
        view.isClickable = false

        unchecked()
        requestLayout()
    }

    // Disable touch
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        return true
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

    fun updateUi(){
        this.backgroundTintList = ColorStateList.valueOf(context.resolveAttrColor(android.R.attr.colorAccent))
    }

    /**
     * Change checked status of the switch.
     *
     * This method will animate the checked status, to
     * change without animation use [animateChecked] method.
     */
    fun setChecked(boolean: Boolean) {
        isChecked = if (boolean) {
            animateChecked()
            boolean
        } else {
            animateUnchecked()
            boolean
        }
    }

    /**
     * Change checked status of the switch without animation.
     *
     * This method will animate the checked status, to
     * change with animation use [setChecked] method.
     */
    fun staticChecked(boolean: Boolean) {
        isChecked = if (boolean) {
            checked()
            boolean
        } else {
            unchecked()
            boolean
        }
    }

    private fun animateUnchecked() {
        thumb.animate()
            .translationX(if (resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL) (w - p * 2 - thumbWidth).toFloat() else 0F)
            .setInterpolator(OvershootInterpolator(tension))
            .setDuration(500)
            .start()

        animateColorChange(context.getColorCompat(R.color.white1))
        animateElevation(0F)
    }

    private fun unchecked() {
        thumb.translationX = if (resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL) (w - p * 2 - thumbWidth).toFloat() else 0F
        this.backgroundTintList = ColorStateList.valueOf(context.getColorCompat(R.color.white1))
        this.elevation = 0F
    }

    private fun animateChecked() {
        thumb.animate()
            .translationX(if (resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL) 0F else (w - p * 2 - thumbWidth).toFloat())
            .setInterpolator(OvershootInterpolator(tension))
            .setDuration(500)
            .start()

        animateColorChange(context.resolveAttrColor(android.R.attr.colorAccent))
        animateElevation(25F)
    }

    private fun checked() {
        thumb.translationX = if (resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL) 0F else (w - p * 2 - thumbWidth).toFloat()
        this.backgroundTintList = ColorStateList.valueOf(context.resolveAttrColor(android.R.attr.colorAccent))
        this.elevation = 25F
    }

    private fun animateElevation(elevation: Float) {
        val valueAnimator = ValueAnimator.ofFloat(this.elevation, elevation)
        valueAnimator.duration = 500L
        valueAnimator.interpolator = LinearOutSlowInInterpolator()
        valueAnimator.addUpdateListener {
            this.elevation = it.animatedValue as Float
        }
        valueAnimator.start()
    }

    fun setOnSwitchCheckedChangeListener(switchCallbacks: SwitchCallbacks) {
        this.switchCallbacks = switchCallbacks
    }

    override fun onViewRemoved(child: View?) {
        super.onViewRemoved(child)
        thumb.clearAnimation()
    }

    /**
     * Inverts the switch's checked status. If the switch is checked then
     * it will be unchecked and vice-versa
     */
    fun invertCheckedStatus() {
        isChecked = !isChecked
    }

}