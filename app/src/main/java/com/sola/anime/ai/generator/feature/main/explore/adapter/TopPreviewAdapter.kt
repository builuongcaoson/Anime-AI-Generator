package com.sola.anime.ai.generator.feature.main.explore.adapter

import com.basic.common.base.LsAdapter
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.common.extension.load
import com.sola.anime.ai.generator.databinding.ItemTopPreviewInExploreBinding
import javax.inject.Inject

class TopPreviewAdapter @Inject constructor(): LsAdapter<Int, ItemTopPreviewInExploreBinding>(ItemTopPreviewInExploreBinding::inflate) {

    init {
        data = listOf(
            R.drawable.preview_top_batch
        )
    }

    override fun bindItem(item: Int, binding: ItemTopPreviewInExploreBinding, position: Int) {
        binding.preview.load(item, errorRes = R.drawable.preview_processing_batch)
    }

}