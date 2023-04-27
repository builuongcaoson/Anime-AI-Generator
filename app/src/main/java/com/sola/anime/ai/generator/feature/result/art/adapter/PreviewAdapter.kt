package com.sola.anime.ai.generator.feature.result.art.adapter

import android.view.ViewGroup
import androidx.core.view.isVisible
import com.basic.common.base.LsAdapter
import com.basic.common.base.LsViewHolder
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.databinding.ItemPreviewArtResultBinding
import com.sola.anime.ai.generator.domain.model.history.ChildHistory
import javax.inject.Inject

class PreviewAdapter @Inject constructor() : LsAdapter<ChildHistory, ItemPreviewArtResultBinding>() {

    var childHistory: ChildHistory? = null
        set(value) {
            if (field == value) return

            val oldIndex = data.indexOf(field)
            val newIndex = data.indexOf(value)

            notifyItemChanged(oldIndex)
            notifyItemChanged(newIndex)

            field = value
        }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LsViewHolder<ItemPreviewArtResultBinding> {
        return LsViewHolder(parent, ItemPreviewArtResultBinding::inflate)
    }

    override fun onBindViewHolder(
        holder: LsViewHolder<ItemPreviewArtResultBinding>,
        position: Int
    ) {
        val item = getItem(position)
        val binding = holder.binding
        val context = binding.root.context

        Glide
            .with(context)
            .asBitmap()
            .load(item.pathPreview)
            .transition(BitmapTransitionOptions.withCrossFade())
            .placeholder(R.drawable.place_holder_image)
            .error(R.drawable.place_holder_image)
            .into(binding.preview)

        binding.viewSelected.isVisible = childHistory == item
    }

}