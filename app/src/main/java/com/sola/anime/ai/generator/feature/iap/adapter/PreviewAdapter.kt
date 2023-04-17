package com.sola.anime.ai.generator.feature.iap.adapter

import android.view.ViewGroup
import com.basic.common.base.LsAdapter
import com.basic.common.base.LsViewHolder
import com.sola.anime.ai.generator.databinding.ItemPreviewIapBinding
import javax.inject.Inject

class PreviewAdapter @Inject constructor(): LsAdapter<String, ItemPreviewIapBinding>() {

    init {
        data = listOf(
            "https://i.imgur.com/bbTke58.png",
            "https://i.imgur.com/bbTke58.png",
            "https://i.imgur.com/bbTke58.png",
            "https://i.imgur.com/bbTke58.png",
            "https://i.imgur.com/bbTke58.png",
            "https://i.imgur.com/bbTke58.png",
            "https://i.imgur.com/bbTke58.png",
            "https://i.imgur.com/bbTke58.png",
            "https://i.imgur.com/bbTke58.png",
            "https://i.imgur.com/bbTke58.png",
            "https://i.imgur.com/bbTke58.png",
            "https://i.imgur.com/bbTke58.png",
            "https://i.imgur.com/bbTke58.png",
            "https://i.imgur.com/bbTke58.png"
        )
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LsViewHolder<ItemPreviewIapBinding> {
        return LsViewHolder(parent, ItemPreviewIapBinding::inflate)
    }

    override fun onBindViewHolder(holder: LsViewHolder<ItemPreviewIapBinding>, position: Int) {
        val item = getItem(position)
        val binding = holder.binding
        val context = binding.root.context


    }

}