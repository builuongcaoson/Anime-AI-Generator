package com.sola.anime.ai.generator.feature.iap.adapter

import androidx.constraintlayout.widget.ConstraintSet
import coil.load
import coil.transition.CrossfadeTransition
import com.basic.common.base.LsAdapter
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.databinding.ItemPreviewIapBinding
import com.sola.anime.ai.generator.domain.model.config.iap.IAP
import javax.inject.Inject

class PreviewAdapter @Inject constructor(): LsAdapter<IAP, ItemPreviewIapBinding>(ItemPreviewIapBinding::inflate) {

    var totalCount = 0

    fun insert(){
        totalCount += 1
        notifyItemInserted(totalCount - 1)
    }

    override fun bindItem(item: IAP, binding: ItemPreviewIapBinding, position: Int) {
        val item = getItem(position % data.size) ?: return

        val set = ConstraintSet()
        set.clone(binding.viewGroup)
        set.setDimensionRatio(binding.viewPreview.id, item.ratio)
        set.applyTo(binding.viewGroup)

        binding.preview.load(item.preview) {
            crossfade(true)
            error(R.drawable.place_holder_image)
        }
    }

    override fun getItemCount(): Int {
        return totalCount
    }

}