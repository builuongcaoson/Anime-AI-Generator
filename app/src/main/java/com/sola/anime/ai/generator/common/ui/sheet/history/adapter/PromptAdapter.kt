package com.sola.anime.ai.generator.common.ui.sheet.history.adapter

import android.view.ViewGroup
import com.basic.common.base.LsAdapter
import com.basic.common.base.LsViewHolder
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.sola.anime.ai.generator.databinding.ItemPreviewArtBinding
import com.sola.anime.ai.generator.databinding.ItemPromptHistoryBinding
import com.sola.anime.ai.generator.domain.model.config.explore.Explore
import javax.inject.Inject

class PromptAdapter @Inject constructor(): LsAdapter<Unit, ItemPromptHistoryBinding>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LsViewHolder<ItemPromptHistoryBinding> {
        return LsViewHolder(parent, ItemPromptHistoryBinding::inflate)
    }

    override fun onBindViewHolder(holder: LsViewHolder<ItemPromptHistoryBinding>, position: Int) {
//        val item = getItem(position % data.size)
//        val binding = holder.binding
//        val context = binding.root.context
    }

    override fun getItemCount(): Int {
        return 5
    }

}