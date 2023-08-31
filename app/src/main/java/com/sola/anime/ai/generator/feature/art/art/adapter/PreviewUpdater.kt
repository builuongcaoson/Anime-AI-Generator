package com.sola.anime.ai.generator.feature.art.art.adapter

import android.view.View
import androidx.annotation.Keep
import androidx.core.view.ViewCompat
import com.sola.anime.ai.generator.common.widget.cardSlider.DefaultViewUpdater
import com.sola.anime.ai.generator.databinding.ItemPreviewArt2Binding

@Keep
class PreviewUpdater: DefaultViewUpdater() {

    override fun updateView(view: View, position: Float) {
        super.updateView(view, position)

        val binding = ItemPreviewArt2Binding.bind(view)

        when {
            position < 0 -> {
                val alpha = ViewCompat.getAlpha(view)
                ViewCompat.setAlpha(view, 1f)
                ViewCompat.setAlpha(binding.viewAlpha, 0.9f - alpha)
                ViewCompat.setAlpha(binding.preview, 0.3f + alpha)
            }
            else -> {
                ViewCompat.setAlpha(binding.viewAlpha, 0f)
                ViewCompat.setAlpha(binding.preview, 1f)
            }
        }
    }

}