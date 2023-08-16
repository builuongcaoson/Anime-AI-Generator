package com.sola.anime.ai.generator.feature.preview.adapter

import android.graphics.drawable.Drawable
import androidx.constraintlayout.widget.ConstraintSet
import coil.load
import coil.transition.CrossfadeTransition
import com.basic.common.base.LsAdapter
import com.basic.common.extension.getDimens
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.databinding.ItemPreviewInPreviewBinding
import com.sola.anime.ai.generator.domain.model.history.ChildHistory
import javax.inject.Inject

class PagePreviewAdapter @Inject constructor() : LsAdapter<ChildHistory, ItemPreviewInPreviewBinding>(ItemPreviewInPreviewBinding::inflate) {

    override fun bindItem(item: ChildHistory, binding: ItemPreviewInPreviewBinding, position: Int) {
        val context = binding.root.context

        val ratio = "${item.width}:${item.height}"

        val set = ConstraintSet()
        set.clone(binding.viewGroup)
        set.setDimensionRatio(binding.cardPreview.id, ratio)
        set.applyTo(binding.viewGroup)

        binding.preview.load(item.upscalePathPreview ?: item.pathPreview) {
            crossfade(true)
            placeholder(R.drawable.place_holder_image)
            error(R.drawable.place_holder_image)
        }
    }

}