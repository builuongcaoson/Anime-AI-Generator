package com.sola.anime.ai.generator.common.widget

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.databinding.ActionViewBinding

class LsActionView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : LinearLayoutCompat(context, attrs) {

    val binding = ActionViewBinding.inflate(LayoutInflater.from(context))
    var display: String? = null
        set(value) {
            field = value

            binding.textDisplay.text = value
        }

    @DrawableRes
    var icon: Int? = null
        set(value) {
            field = value

            value?.let {
                binding.imageIcon.setImageResource(value)
            } ?: binding.imageIcon.setBackgroundDrawable(null)
        }

    init {
        layoutParams = ViewGroup.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)
        binding.root.layoutParams = ViewGroup.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)
        addView(binding.root)

        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL

        context.obtainStyledAttributes(attrs, R.styleable.LsActionView).run {
            display = getString(R.styleable.LsActionView_title)

            // If an icon is being used, set up the icon view
            getResourceId(R.styleable.LsActionView_icon, -1).takeIf { it != -1 }?.let { id ->
                icon = id
            }

            recycle()
        }
    }

}