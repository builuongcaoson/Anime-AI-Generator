package com.sola.anime.ai.generator.feature.iap.adapter

import android.view.ViewGroup
import com.basic.common.base.LsAdapter
import com.basic.common.base.LsViewHolder
import com.bumptech.glide.Glide
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.common.ConfigApp
import com.sola.anime.ai.generator.databinding.ItemPreviewIapBinding
import com.sola.anime.ai.generator.domain.model.PreviewIap
import javax.inject.Inject

class PreviewAdapter @Inject constructor(): LsAdapter<PreviewIap, ItemPreviewIapBinding>() {

    var totalCount = 0

    fun insert(){
        totalCount += 1
        notifyItemInserted(totalCount - 1)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LsViewHolder<ItemPreviewIapBinding> {
        return LsViewHolder(parent, ItemPreviewIapBinding::inflate)
    }

    override fun onBindViewHolder(holder: LsViewHolder<ItemPreviewIapBinding>, position: Int) {
        val item = getItem(position % data.size)
        val binding = holder.binding
        val context = binding.root.context

        Glide.with(context)
            .load(item.preview)
            .thumbnail(0.5f)
            .error(R.drawable.place_holder_image)
            .placeholder(R.drawable.place_holder_image)
            .into(binding.image)

    }

    override fun getItemCount(): Int {
        return totalCount
    }

}