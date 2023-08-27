package com.sola.anime.ai.generator.feature.preview.adapter

import androidx.constraintlayout.widget.ConstraintSet
import com.basic.common.base.LsAdapter
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.common.extension.load
import com.sola.anime.ai.generator.databinding.ItemPreviewInPreviewBinding
import com.sola.anime.ai.generator.domain.model.history.ChildHistory
import javax.inject.Inject

class PagePreviewAdapter @Inject constructor() : LsAdapter<ChildHistory, ItemPreviewInPreviewBinding>(ItemPreviewInPreviewBinding::inflate) {

    override fun bindItem(item: ChildHistory, binding: ItemPreviewInPreviewBinding, position: Int) {
        val ratio = "${item.width}:${item.height}"

        val set = ConstraintSet()
        set.clone(binding.viewGroup)
        set.setDimensionRatio(binding.cardPreview.id, ratio)
        set.applyTo(binding.viewGroup)

        binding.preview.load(item.upscalePathPreview ?: item.pathPreview, placeholderRes = R.drawable.place_holder_image, errorRes = R.drawable.place_holder_image)
    }

}