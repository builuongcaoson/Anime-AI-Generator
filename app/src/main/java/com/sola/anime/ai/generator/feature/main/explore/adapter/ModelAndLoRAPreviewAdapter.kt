package com.sola.anime.ai.generator.feature.main.explore.adapter

import coil.load
import com.basic.common.base.LsAdapter
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.databinding.ItemModelLoraInBatchBinding
import com.sola.anime.ai.generator.domain.model.ModelOrLoRA
import javax.inject.Inject

class ModelAndLoRAPreviewAdapter @Inject constructor(): LsAdapter<ModelOrLoRA, ItemModelLoraInBatchBinding>(ItemModelLoraInBatchBinding::inflate) {

    override fun bindItem(item: ModelOrLoRA, binding: ItemModelLoraInBatchBinding, position: Int) {
        binding.preview.load(item.preview) {
            crossfade(true)
            error(R.drawable.place_holder_image)
        }

        binding.description.text = if (item.isModel) "Model" else "LoRA"
        binding.display.text = item.display
        binding.favouriteCount.text = "${if (item.isFavourite) (item.favouriteCount + 1) else item.favouriteCount} Uses"
    }

}