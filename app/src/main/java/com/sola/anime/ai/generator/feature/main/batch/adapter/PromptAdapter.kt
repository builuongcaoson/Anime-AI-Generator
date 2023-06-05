package com.sola.anime.ai.generator.feature.main.batch.adapter

import android.animation.ValueAnimator
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.GridLayoutManager
import com.basic.common.base.LsAdapter
import com.basic.common.base.LsViewHolder
import com.basic.common.extension.clicks
import com.basic.common.extension.resolveAttrColor
import com.sola.anime.ai.generator.databinding.ItemImageDimensionsBatchBinding
import com.sola.anime.ai.generator.databinding.ItemNumberOfImagesBatchBinding
import com.sola.anime.ai.generator.databinding.ItemPromptBatchBinding
import com.sola.anime.ai.generator.domain.model.ImageDimensions
import com.sola.anime.ai.generator.domain.model.NumberOfImages
import com.sola.anime.ai.generator.domain.model.PromptBatch
import javax.inject.Inject

class PromptAdapter @Inject constructor(): LsAdapter<PromptBatch, ItemPromptBatchBinding>(ItemPromptBatchBinding::inflate) {

    init {
        data = listOf(
            PromptBatch(prompt = "Models")
        )
    }

    override fun bindItem(item: PromptBatch, binding: ItemPromptBatchBinding, position: Int) {
        val context = binding.root.context

        binding.recyclerNumberOfImages.apply {
            this.layoutManager = object: GridLayoutManager(context, 4, VERTICAL, false){
                override fun canScrollVertically(): Boolean {
                    return false
                }
            }
            this.adapter = NumberOfImagesAdapter()
        }
        binding.recyclerImageDimensions.apply {
            this.layoutManager = object: GridLayoutManager(context, 3, VERTICAL, false){
                override fun canScrollVertically(): Boolean {
                    return false
                }
            }
            this.adapter = ImageDimensionsAdapter()
        }

        binding.viewDropNegative.clicks { animShowOrHideNegative(binding = binding, isShow = true) }
        binding.viewDropNumbers.clicks { binding.recyclerNumberOfImages.isVisible = !binding.recyclerNumberOfImages.isVisible }
        binding.viewDropDimensions.clicks { binding.recyclerImageDimensions.isVisible = !binding.recyclerImageDimensions.isVisible }
        binding.viewDropAdvanced.clicks { binding.viewAdvanced.isVisible = !binding.viewAdvanced.isVisible }
    }

    private fun animShowOrHideNegative(binding: ItemPromptBatchBinding, isShow: Boolean){
        val startHeight = if (isShow) 0 else ViewGroup.LayoutParams.WRAP_CONTENT
        val endHeight = if (isShow) 200 else 0
        val valueAnimator = ValueAnimator.ofInt(startHeight, endHeight)
        valueAnimator.addUpdateListener { animation ->
            val height = animation.animatedValue as Int
            val layoutParams = binding.viewNegative.layoutParams
            layoutParams.height = height
            binding.viewNegative.layoutParams = layoutParams

//            when {
//                isShow -> binding.viewGroup.updateLayoutParams<ViewGroup.LayoutParams> {
//                    this.height += height
//                }
//                else -> binding.viewGroup.updateLayoutParams<ViewGroup.LayoutParams> {
//                    this.height -= height
//                }
//            }
        }
        valueAnimator.duration = 1000
        valueAnimator.start()
    }

    class NumberOfImagesAdapter: LsAdapter<NumberOfImages, ItemNumberOfImagesBatchBinding>(ItemNumberOfImagesBatchBinding::inflate) {

        init {
            data = NumberOfImages.values().toList()
        }

        override fun bindItem(
            item: NumberOfImages,
            binding: ItemNumberOfImagesBatchBinding,
            position: Int
        ) {
            val context = binding.root.context

            binding.display.text = item.display

            when (position) {
                0 -> {
                    binding.viewClicks.setCardBackgroundColor(context.resolveAttrColor(android.R.attr.colorAccent))
                    binding.display.setTextColor(context.resolveAttrColor(com.google.android.material.R.attr.colorOnPrimary))
                }
                else -> {
                    binding.viewClicks.setCardBackgroundColor(context.resolveAttrColor(android.R.attr.colorBackground))
                    binding.display.setTextColor(context.resolveAttrColor(android.R.attr.textColorPrimary))
                }
            }
        }

    }

    class ImageDimensionsAdapter: LsAdapter<ImageDimensions, ItemImageDimensionsBatchBinding>(ItemImageDimensionsBatchBinding::inflate) {

        init {
            data = ImageDimensions.values().toList()
        }

        override fun bindItem(
            item: ImageDimensions,
            binding: ItemImageDimensionsBatchBinding,
            position: Int
        ) {
            val context = binding.root.context

            binding.display.text = item.display

            when (position) {
                0 -> {
                    binding.viewClicks.setCardBackgroundColor(context.resolveAttrColor(android.R.attr.colorAccent))
                    binding.display.setTextColor(context.resolveAttrColor(com.google.android.material.R.attr.colorOnPrimary))
                }
                else -> {
                    binding.viewClicks.setCardBackgroundColor(context.resolveAttrColor(android.R.attr.colorBackground))
                    binding.display.setTextColor(context.resolveAttrColor(android.R.attr.textColorPrimary))
                }
            }
        }

    }

}