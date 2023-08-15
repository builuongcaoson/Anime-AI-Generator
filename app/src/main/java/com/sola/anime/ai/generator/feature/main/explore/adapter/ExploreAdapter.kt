package com.sola.anime.ai.generator.feature.main.explore.adapter

import androidx.constraintlayout.widget.ConstraintSet
import com.basic.common.base.LsAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.databinding.ItemPreviewExploreBinding
import com.sola.anime.ai.generator.domain.model.config.explore.Explore
import javax.inject.Inject

class ExploreAdapter @Inject constructor(): LsAdapter<Explore, ItemPreviewExploreBinding>(ItemPreviewExploreBinding::inflate) {

    override fun bindItem(item: Explore, binding: ItemPreviewExploreBinding, position: Int) {
        ConstraintSet().apply {
            this.clone(binding.viewGroup)
            this.setDimensionRatio(binding.viewPreview.id, item.ratio)
            this.applyTo(binding.viewGroup)
        }

        Glide.with(binding.root)
            .load(item.previews.firstOrNull())
            .transition(DrawableTransitionOptions.withCrossFade())
            .error(R.drawable.place_holder_image)
            .into(binding.preview)

        binding.prompt.text = item.prompt
    }

}