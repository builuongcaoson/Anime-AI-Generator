package com.sola.anime.ai.generator.feature.iap.adapter

import androidx.constraintlayout.widget.ConstraintSet
import com.basic.common.base.LsAdapter
import com.basic.common.base.LsViewHolder
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.common.extension.load
import com.sola.anime.ai.generator.databinding.ItemPreviewIapBinding
import com.sola.anime.ai.generator.domain.model.config.iap.IAP
import javax.inject.Inject

class PreviewAdapter @Inject constructor(): LsAdapter<IAP, ItemPreviewIapBinding>(ItemPreviewIapBinding::inflate) {

    var totalCount = 0

    fun insert(){
        totalCount += 1
        notifyItemInserted(totalCount - 1)
    }

    override fun onBindViewHolder(holder: LsViewHolder<ItemPreviewIapBinding>, position: Int) {
        val binding = holder.binding
        val item = getItem(position % data.size)

        val set = ConstraintSet()
        set.clone(binding.viewGroup)
        set.setDimensionRatio(binding.viewPreview.id, item.ratio)
        set.applyTo(binding.viewGroup)

        binding.preview.load(item.preview, errorRes = R.drawable.place_holder_image)
    }

    override fun bindItem(item: IAP, binding: ItemPreviewIapBinding, position: Int) {

    }

    override fun getItemCount(): Int {
        return totalCount
    }

}