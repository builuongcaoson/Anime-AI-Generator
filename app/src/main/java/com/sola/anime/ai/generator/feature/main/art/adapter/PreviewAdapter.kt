package com.sola.anime.ai.generator.feature.main.art.adapter

import android.view.ViewGroup
import com.basic.common.base.LsAdapter
import com.basic.common.base.LsViewHolder
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.sola.anime.ai.generator.databinding.ItemPreviewArtBinding
import com.sola.anime.ai.generator.domain.model.config.explore.Explore
import javax.inject.Inject

class PreviewAdapter @Inject constructor(): LsAdapter<Explore, ItemPreviewArtBinding>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LsViewHolder<ItemPreviewArtBinding> {
        return LsViewHolder(parent, ItemPreviewArtBinding::inflate)
    }

    override fun onBindViewHolder(holder: LsViewHolder<ItemPreviewArtBinding>, position: Int) {
        val item = getItem(position % data.size)
        val binding = holder.binding
        val context = binding.root.context

        Glide
            .with(context)
            .asBitmap()
            .load(item.preview)
            .transition(BitmapTransitionOptions.withCrossFade())
            .into(binding.preview)
    }

}