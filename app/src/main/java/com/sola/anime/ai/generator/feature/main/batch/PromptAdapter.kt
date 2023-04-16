package com.sola.anime.ai.generator.feature.main.batch

import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.basic.common.base.LsAdapter
import com.basic.common.base.LsViewHolder
import com.basic.common.extension.resolveAttrColor
import com.sola.anime.ai.generator.databinding.ItemImageDimensionsBatchBinding
import com.sola.anime.ai.generator.databinding.ItemNumberOfImagesBatchBinding
import com.sola.anime.ai.generator.databinding.ItemPromptBatchBinding
import com.sola.anime.ai.generator.domain.model.ImageDimensions
import com.sola.anime.ai.generator.domain.model.NumberOfImages
import com.sola.anime.ai.generator.domain.model.PromptBatch
import javax.inject.Inject

class PromptAdapter @Inject constructor(): LsAdapter<PromptBatch, ItemPromptBatchBinding>() {

    init {
        data = listOf(
            PromptBatch(prompt = "Models")
        )
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LsViewHolder<ItemPromptBatchBinding> {
        return LsViewHolder(parent, ItemPromptBatchBinding::inflate)
    }

    override fun onBindViewHolder(holder: LsViewHolder<ItemPromptBatchBinding>, position: Int) {
        val item = getItem(position)
        val binding = holder.binding
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
    }

    class NumberOfImagesAdapter: LsAdapter<NumberOfImages, ItemNumberOfImagesBatchBinding>() {

        init {
            data = NumberOfImages.values().toList()
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): LsViewHolder<ItemNumberOfImagesBatchBinding> {
            return LsViewHolder(parent, ItemNumberOfImagesBatchBinding::inflate)
        }

        override fun onBindViewHolder(holder: LsViewHolder<ItemNumberOfImagesBatchBinding>, position: Int) {
            val item = getItem(position)
            val binding = holder.binding
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

    class ImageDimensionsAdapter: LsAdapter<ImageDimensions, ItemImageDimensionsBatchBinding>() {

        init {
            data = ImageDimensions.values().toList()
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): LsViewHolder<ItemImageDimensionsBatchBinding> {
            return LsViewHolder(parent, ItemImageDimensionsBatchBinding::inflate)
        }

        override fun onBindViewHolder(holder: LsViewHolder<ItemImageDimensionsBatchBinding>, position: Int) {
            val item = getItem(position)
            val binding = holder.binding
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