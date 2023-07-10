package com.sola.anime.ai.generator.common.widget.compareSlider

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.databinding.CompareSliderViewBinding
import timber.log.Timber

@SuppressLint("CustomViewStyleable")
class CompareSliderView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    val binding = CompareSliderViewBinding.inflate(LayoutInflater.from(context))
    private var isThumbPressed = false

    init {
        layoutParams = ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        binding.root.layoutParams = ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        addView(binding.root)

        context.obtainStyledAttributes(attrs, R.styleable.CompareSlider).run {
            getResourceId(R.styleable.CompareSlider_left_image, -1).takeIf { it != -1 }?.let { id ->
                binding.leftPreview.setImageResource(id)
            }

            getResourceId(R.styleable.CompareSlider_right_image, -1).takeIf { it != -1 }?.let { id ->
                binding.rightPreview.setImageResource(id)
            }

            getResourceId(R.styleable.CompareSlider_slider_icon, -1).takeIf { it != -1 }?.let { id ->
                binding.slideIcon.setImageResource(id)
            }

            recycle()
        }

        binding.rightPreview.post {
            binding.leftPreview.layoutParams.height = binding.rightPreview.height
            binding.leftPreview.layoutParams.width = binding.rightPreview.width

            binding.sliderBar.layoutParams.height = binding.rightPreview.height

            binding.sbImageSeek.post {
                binding.sbImageSeek.max = binding.rightPreview.width
                binding.sbImageSeek.progress = binding.rightPreview.width / 2
            }
        }

        listenerView()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun listenerView() {
//        binding.slideIcon.setOnTouchListener { v, event ->
//            when (event.action) {
//                MotionEvent.ACTION_DOWN -> {
//                    isThumbPressed = true
//                }
//                MotionEvent.ACTION_MOVE -> {
                    // Lấy vị trí X hiện tại của sự kiện chạm
//                    val x = event.rawX

                    // Tính toán width mới dựa trên vị trí chạm
//                    val newWidth = (x - viewA.left).toInt()

                    // Cập nhật width của view A
//                    updateViewAWidth(newWidth)
//                    setImageWidth(x.toInt())
//                }
//                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
//                    isThumbPressed = false
//                }
//            }
//            false
//        }
        binding.sbImageSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
//                if (isThumbPressed){
                    setImageWidth(i)
//                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {
                Timber.e("StartTrackingTouch")
            }
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                Timber.e("StopTrackingTouch")
            }
        })
    }

    private fun setImageWidth(progress: Int) {
        if (progress <= 0) return
        binding.target.updateLayoutParams<ViewGroup.LayoutParams> {
            this.width = progress
        }
    }


}