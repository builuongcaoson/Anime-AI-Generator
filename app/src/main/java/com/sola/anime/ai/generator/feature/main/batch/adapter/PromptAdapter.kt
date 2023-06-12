package com.sola.anime.ai.generator.feature.main.batch.adapter

import android.util.SparseBooleanArray
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import com.basic.common.base.LsAdapter
import com.basic.common.extension.clicks
import com.basic.common.extension.resolveAttrColor
import com.sola.anime.ai.generator.databinding.ItemImageDimensionsBatchBinding
import com.sola.anime.ai.generator.databinding.ItemNumberOfImagesBatchBinding
import com.sola.anime.ai.generator.databinding.ItemPromptBatchBinding
import com.sola.anime.ai.generator.domain.model.NumberOfImages
import com.sola.anime.ai.generator.domain.model.PromptBatch
import com.sola.anime.ai.generator.domain.model.Ratio
import javax.inject.Inject

class PromptAdapter @Inject constructor(): LsAdapter<PromptBatch, ItemPromptBatchBinding>(ItemPromptBatchBinding::inflate) {

    init {
        data = listOf(PromptBatch())
    }

    private val sparseNegatives = SparseBooleanArray()
    private val sparseNumbers = SparseBooleanArray()
    private val sparseDimensions = SparseBooleanArray()
    private val sparseAdvanceds = SparseBooleanArray()

    override fun bindItem(item: PromptBatch, binding: ItemPromptBatchBinding, position: Int) {
        val context = binding.root.context

        binding.recyclerNumberOfImages.apply {
            this.layoutManager = object: GridLayoutManager(context, 4, VERTICAL, false){
                override fun canScrollVertically(): Boolean {
                    return false
                }
            }
            this.adapter = NumberOfImagesAdapter(item)
        }
        binding.recyclerImageDimensions.apply {
            this.layoutManager = object: GridLayoutManager(context, 4, VERTICAL, false){
                override fun canScrollVertically(): Boolean {
                    return false
                }
            }
            this.adapter = ImageDimensionsAdapter(item)
        }

        showOrHideNegative(binding, sparseNegatives[position])
        showOrHideNumber(binding, sparseNumbers.get(0, true))
        showOrHideDimension(binding, sparseDimensions.get(0, true))
        showOrHideAdvanced(binding, sparseAdvanceds[position])

        binding.viewDropNegative.clicks {
            val isShow = !sparseNegatives[position]
            showOrHideNegative(binding, isShow)
            sparseNegatives.put(position, isShow)
        }
        binding.viewDropNumbers.clicks {
            val isShow = !sparseNumbers.get(position, true)
            showOrHideNumber(binding, isShow)
            sparseNumbers.put(position, isShow)
        }
        binding.viewDropDimensions.clicks {
            val isShow = !sparseDimensions.get(position, true)
            showOrHideDimension(binding, isShow)
            sparseDimensions.put(position, isShow)
        }
        binding.viewDropAdvanced.clicks {
            val isShow = !sparseAdvanceds[position]
            showOrHideAdvanced(binding, isShow)
            sparseAdvanceds.put(position, isShow)
        }
    }

    private fun showOrHideNegative(binding: ItemPromptBatchBinding, isShow: Boolean){
        binding.viewNegative.isVisible = isShow
        binding.viewDropNegative.rotation = if (isShow) 0f else 90f
    }

    private fun showOrHideNumber(binding: ItemPromptBatchBinding, isShow: Boolean){
        binding.recyclerNumberOfImages.isVisible = isShow
        binding.viewDropNumbers.rotation = if (isShow) 0f else 90f
    }

    private fun showOrHideDimension(binding: ItemPromptBatchBinding, isShow: Boolean){
        binding.recyclerImageDimensions.isVisible = isShow
        binding.viewDropDimensions.rotation = if (isShow) 0f else 90f
    }

    private fun showOrHideAdvanced(binding: ItemPromptBatchBinding, isShow: Boolean){
        binding.viewAdvanced.isVisible = isShow
        binding.viewDropAdvanced.rotation = if (isShow) 0f else 90f
    }

    class NumberOfImagesAdapter(private val promptBatch: PromptBatch): LsAdapter<NumberOfImages, ItemNumberOfImagesBatchBinding>(ItemNumberOfImagesBatchBinding::inflate) {

        init {
            data = NumberOfImages.values().toList()
        }

        var numberOfImages = promptBatch.numberOfImages
            set(value) {
                promptBatch.numberOfImages = value

                if (field == value){
                    return
                }

                val oldIndex = data.indexOf(field)
                val newIndex = data.indexOf(value)

                notifyItemChanged(oldIndex)
                notifyItemChanged(newIndex)

                field = value
            }

        override fun bindItem(
            item: NumberOfImages,
            binding: ItemNumberOfImagesBatchBinding,
            position: Int
        ) {
            val context = binding.root.context

            binding.display.text = item.display

            when {
                numberOfImages == item -> {
                    binding.viewClicks.setCardBackgroundColor(context.resolveAttrColor(android.R.attr.colorAccent))
                    binding.display.setTextColor(context.resolveAttrColor(com.google.android.material.R.attr.colorOnPrimary))
                }
                else -> {
                    binding.viewClicks.setCardBackgroundColor(context.resolveAttrColor(android.R.attr.colorBackground))
                    binding.display.setTextColor(context.resolveAttrColor(android.R.attr.textColorPrimary))
                }
            }

            binding.viewClicks.clicks(withAnim = false){ numberOfImages = item }
        }

    }

    class ImageDimensionsAdapter(private val promptBatch: PromptBatch): LsAdapter<Ratio, ItemImageDimensionsBatchBinding>(ItemImageDimensionsBatchBinding::inflate) {

        init {
            data = Ratio.values().toList()
        }

        var ratio = promptBatch.ratio
            set(value) {
                promptBatch.ratio = value

                if (field == value){
                    return
                }

                val oldIndex = data.indexOf(field)
                val newIndex = data.indexOf(value)

                notifyItemChanged(oldIndex)
                notifyItemChanged(newIndex)

                field = value
            }

        override fun bindItem(
            item: Ratio,
            binding: ItemImageDimensionsBatchBinding,
            position: Int
        ) {
            val context = binding.root.context

            binding.display.text = item.display

            when {
                ratio == item -> {
                    binding.viewClicks.setCardBackgroundColor(context.resolveAttrColor(android.R.attr.colorAccent))
                    binding.display.setTextColor(context.resolveAttrColor(com.google.android.material.R.attr.colorOnPrimary))
                }
                else -> {
                    binding.viewClicks.setCardBackgroundColor(context.resolveAttrColor(android.R.attr.colorBackground))
                    binding.display.setTextColor(context.resolveAttrColor(android.R.attr.textColorPrimary))
                }
            }

            binding.viewClicks.clicks(withAnim = false){ ratio = item }
        }

    }

}