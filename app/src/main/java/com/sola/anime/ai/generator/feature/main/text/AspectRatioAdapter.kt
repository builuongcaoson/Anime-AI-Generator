package com.sola.anime.ai.generator.feature.main.text

import android.view.LayoutInflater
import android.view.ViewGroup
import com.basic.common.base.LsAdapter
import com.basic.common.base.LsViewHolder
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.databinding.ItemAspectRatioBinding
import com.sola.anime.ai.generator.databinding.ItemPreviewBinding
import javax.inject.Inject

class AspectRatioAdapter @Inject constructor(): LsAdapter<Unit, ItemAspectRatioBinding>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LsViewHolder<ItemAspectRatioBinding> {
        return LsViewHolder(parent, ItemAspectRatioBinding::inflate)
    }

    override fun onBindViewHolder(holder: LsViewHolder<ItemAspectRatioBinding>, position: Int) {
        val binding = holder.binding
    }

    override fun getItemCount(): Int {
        return 5
    }

}