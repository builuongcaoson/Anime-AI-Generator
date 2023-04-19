package com.sola.anime.ai.generator.feature.result.art.adapter

import android.view.ViewGroup
import com.basic.common.base.LsAdapter
import com.basic.common.base.LsViewHolder
import com.sola.anime.ai.generator.databinding.ItemPreviewArtResultBinding
import javax.inject.Inject

class PreviewAdapter @Inject constructor() : LsAdapter<Unit, ItemPreviewArtResultBinding>() {

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
//        val item = getItem(position % data.size)
//        val binding = holder.binding
//        val context = binding.root.context

    }

    override fun getItemCount(): Int {
        return 3
    }

}