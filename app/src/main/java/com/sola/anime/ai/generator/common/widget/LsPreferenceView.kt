package com.sola.anime.ai.generator.common.widget

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.databinding.PreferenceViewBinding

class LsPreferenceView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    val binding = PreferenceViewBinding.inflate(LayoutInflater.from(context))
    var viewWidgetFrame: View? = null

    var title: String? = null
        set(value) {
            field = value

            binding.titleView.text = value
        }

    var summary: String? = null
        set(value) {
            field = value

            binding.summaryView.text = value
            binding.summaryView.isVisible = value?.isNotEmpty() == true
        }

    init {
        layoutParams = ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        binding.root.layoutParams = ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        addView(binding.root)

        context.obtainStyledAttributes(attrs, R.styleable.LsPreferenceView).run {
            title = getString(R.styleable.LsPreferenceView_title)
            summary = getString(R.styleable.LsPreferenceView_summary)

            getResourceId(R.styleable.LsPreferenceView_widget, -1).takeIf { it != -1 }?.let { id ->
                viewWidgetFrame = View.inflate(context, id, binding.widgetFrame)
            }

            getResourceId(R.styleable.LsPreferenceView_icon, -1).takeIf { it != -1 }?.let { id ->
                binding.icon.isVisible = true
                binding.viewLottie.isVisible = false
                binding.icon.setImageResource(id)
            }

            getResourceId(R.styleable.LsPreferenceView_lottieRes, -1).takeIf { it != -1 }?.let { id ->
                binding.icon.isVisible = false
                binding.viewLottie.isVisible = true
                binding.viewLottie.setAnimation(id)
            }

            recycle()
        }
    }

}