package com.sola.anime.ai.generator.feature.processing.batch.adapter

import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import com.basic.common.base.LsAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.databinding.ItemPreviewBatchProcessingBinding
import com.sola.anime.ai.generator.domain.model.status.DezgoStatusTextToImage
import com.sola.anime.ai.generator.domain.model.status.StatusBodyTextToImage
import javax.inject.Inject

class PreviewAdapter @Inject constructor() : LsAdapter<DezgoStatusTextToImage, ItemPreviewBatchProcessingBinding>(ItemPreviewBatchProcessingBinding::inflate) {

    override fun bindItem(
        item: DezgoStatusTextToImage,
        binding: ItemPreviewBatchProcessingBinding,
        position: Int
    ) {
        ConstraintSet().apply {
            clone(binding.viewRoot)
            setDimensionRatio(binding.viewPreviewRatio.id, "${item.body.width}:${item.body.height}")
            applyTo(binding.viewRoot)
        }

        val status = item.status

        when {
            status == StatusBodyTextToImage.Loading -> {
                binding.viewLoading.isVisible = true
            }
            status is StatusBodyTextToImage.Failure -> {
                binding.viewLoading.isVisible = false

                binding.preview.setImageResource(R.drawable.place_holder_image)
            }
            status is StatusBodyTextToImage.Success -> {
                binding.viewLoading.isVisible = false

                Glide
                    .with(binding.root)
                    .load(status.file)
                    .centerCrop()
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .error(R.drawable.place_holder_image)
                    .into(binding.preview)
            }
        }
    }


}