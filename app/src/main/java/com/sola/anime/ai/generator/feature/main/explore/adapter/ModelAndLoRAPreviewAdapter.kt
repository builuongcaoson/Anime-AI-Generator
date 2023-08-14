package com.sola.anime.ai.generator.feature.main.explore.adapter

import com.basic.common.base.LsAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.databinding.ItemModelLoraInBatchBinding
import com.sola.anime.ai.generator.domain.model.ModelOrLoRA
import javax.inject.Inject

class ModelAndLoRAPreviewAdapter @Inject constructor(): LsAdapter<ModelOrLoRA, ItemModelLoraInBatchBinding>(ItemModelLoraInBatchBinding::inflate) {

    override fun bindItem(item: ModelOrLoRA, binding: ItemModelLoraInBatchBinding, position: Int) {
        Glide
            .with(binding.root)
            .load(item.preview)
            .transition(DrawableTransitionOptions.withCrossFade())
            .error(R.drawable.preview_processing_batch)
            .into(binding.preview)

        binding.display.text = item.display
        binding.description.text = item.description
    }

}