package com.yalantis.ucrop.view

import kotlin.jvm.JvmOverloads
import android.widget.FrameLayout
import android.view.LayoutInflater
import android.content.Context
import android.util.AttributeSet
import com.yalantis.ucrop.R
import com.yalantis.ucrop.callback.CropBoundsChangeListener
import com.yalantis.ucrop.callback.OverlayViewChangeListener

class UCropView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

//    private var isShowOverlay = false
    var cropImageView: GestureCropImageView
    var overlayView: OverlayView

    init {
        LayoutInflater.from(context).inflate(R.layout.ucrop_view, this, true)
        cropImageView = findViewById(R.id.cropImageView)
        overlayView = findViewById(R.id.overlayView)
        val a = context.obtainStyledAttributes(attrs, R.styleable.ucrop_UCropView)
        overlayView.processStyledAttributes(a)
        cropImageView.processStyledAttributes(a)
        a.recycle()

//        if (isShowOverlay) {
//            overlayView.alpha = 1f
//        } else {
//            overlayView.alpha = 0f
//        }

        setListenersToViews()
    }

//    fun setShowOverlay(showOverlay: Boolean) {
//        isShowOverlay = showOverlay
//        if (isShowOverlay) {
//            overlayView.alpha = 1f
//        } else {
//            overlayView.alpha = 0f
//        }
//    }

//    fun animShowOverlay(showOverlay: Boolean) {
//        isShowOverlay = showOverlay

//        val from = when {
//            isShowOverlay -> 0
//            else -> resources.getDimension(com.intuit.sdp.R.dimen._15sdp).toInt()
//        }
//
//        val to = when {
//            isShowOverlay -> resources.getDimension(com.intuit.sdp.R.dimen._15sdp).toInt()
//            else -> 0
//        }

//        if (isShowOverlay) {
//            overlayView.animate().alpha(1f).setDuration(250).start()
//        } else {
//            overlayView.animate().alpha(0f).setDuration(250).start()
//        }

//        context.animInt(
//            from = from,
//            to = to,
//            duration = 250,
//            update = {
//                cropImageView.updateLayoutParams<LayoutParams> {
//                    setPadding(it, it, it, it)
//                }
//                overlayView.updateLayoutParams<LayoutParams> {
//                    setPadding(it, it, it, it)
//                }
//                cropImageView.updatePadding(it, it, it, it)
//                cropImageView.requestLayout()
//            }
//        )
//    }

    private fun setListenersToViews() {
        cropImageView.cropBoundsChangeListener =
            CropBoundsChangeListener { cropRatio -> overlayView.setTargetAspectRatio(cropRatio) }
        overlayView.overlayViewChangeListener =
            OverlayViewChangeListener { cropRect -> cropImageView.setCropRect(cropRect) }
    }

    override fun shouldDelayChildPressedState(): Boolean {
        return false
    }

    /**
     * Method for reset state for UCropImageView such as rotation, scale, translation.
     * Be careful: this method recreate UCropImageView instance and reattach it to layout.
     */
    fun resetCropImageView() {
        removeView(cropImageView)
        cropImageView = GestureCropImageView(context)
        setListenersToViews()
        cropImageView.setCropRect(overlayView.cropViewRect)
        addView(cropImageView, 0)
    }
}