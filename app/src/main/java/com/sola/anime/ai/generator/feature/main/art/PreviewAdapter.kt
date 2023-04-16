package com.sola.anime.ai.generator.feature.main.art

import android.view.ViewGroup
import com.basic.common.base.LsAdapter
import com.basic.common.base.LsViewHolder
import com.sola.anime.ai.generator.databinding.ItemPreviewBinding
import javax.inject.Inject

class PreviewAdapter @Inject constructor(): LsAdapter<Unit, ItemPreviewBinding>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LsViewHolder<ItemPreviewBinding> {
        return LsViewHolder(parent, ItemPreviewBinding::inflate)
    }

    override fun onBindViewHolder(holder: LsViewHolder<ItemPreviewBinding>, position: Int) {
        val binding = holder.binding


    }

    override fun getItemCount(): Int {
        return 10
    }

}