package com.sola.anime.ai.generator.common.widget.compareSlider

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.databinding.CompareSliderViewBinding

@SuppressLint("CustomViewStyleable")
class CompareSliderView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    val binding = CompareSliderViewBinding.inflate(LayoutInflater.from(context))

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
            binding.sbImageSeek.max = binding.rightPreview.width
            binding.sliderBar.layoutParams.height = binding.rightPreview.height
        }

        listenerView()
    }

    private fun listenerView() {
        binding.sbImageSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                setImageWidth(i)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    private fun setImageWidth(progress: Int) {
        if (progress <= 0) return
        binding.target.updateLayoutParams<ViewGroup.LayoutParams> {
            this.width = progress
        }
    }


}