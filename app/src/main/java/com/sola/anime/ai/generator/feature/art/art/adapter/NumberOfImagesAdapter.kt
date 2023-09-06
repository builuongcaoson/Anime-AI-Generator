package com.sola.anime.ai.generator.feature.art.art.adapter

import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import com.basic.common.base.LsAdapter
import com.sola.anime.ai.generator.databinding.ItemNumberOfImagesInArtBinding
import com.sola.anime.ai.generator.domain.model.NumberOfImages
import javax.inject.Inject

class NumberOfImagesAdapter @Inject constructor(): LsAdapter<NumberOfImages, ItemNumberOfImagesInArtBinding>(ItemNumberOfImagesInArtBinding::inflate) {

    init {
        data = NumberOfImages.values().toList()
    }

    var numberOfImages = NumberOfImages.NumberOfImages1
    var screenWidth = 0

    override fun bindItem(
        item: NumberOfImages,
        binding: ItemNumberOfImagesInArtBinding,
        position: Int
    ) {
        binding.root.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            this.marginStart = when (position) {
                0 -> screenWidth / 2 - binding.root.width / 2
                else -> 0
            }
            this.marginEnd = when (position) {
                data.lastIndex -> screenWidth / 2 - binding.root.width / 2
                else -> 0
            }
        }
        binding.display.text = item.display
    }

}