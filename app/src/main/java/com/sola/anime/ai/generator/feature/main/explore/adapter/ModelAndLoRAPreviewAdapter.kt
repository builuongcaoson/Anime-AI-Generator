package com.sola.anime.ai.generator.feature.main.explore.adapter

import com.basic.common.base.LsAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.databinding.ItemModelLoraInBatchBinding
import javax.inject.Inject

class ModelAndLoRAPreviewAdapter @Inject constructor(): LsAdapter<Int, ItemModelLoraInBatchBinding>(ItemModelLoraInBatchBinding::inflate) {

    init {
        data = listOf(
            R.drawable.preview_top_batch
        )
    }

    override fun bindItem(item: Int, binding: ItemModelLoraInBatchBinding, position: Int) {
        Glide
            .with(binding.root)
            .load(item)
            .transition(DrawableTransitionOptions.withCrossFade())
            .error(R.drawable.preview_processing_batch)
            .into(binding.preview)
    }

}