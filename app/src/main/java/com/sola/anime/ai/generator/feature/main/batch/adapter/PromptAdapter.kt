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

//        binding.viewNegative.apply {
//            measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
//            updateLayoutParams<ViewGroup.LayoutParams> {
//                this.height = if (sparseNegatives[position]) measuredHeight else 0
//            }
//        }
//
//        binding.recyclerNumberOfImages.apply {
//            measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
//            updateLayoutParams<ViewGroup.LayoutParams> {
//                this.height = if (sparseNumbers[position]) measuredHeight else 0
//            }
//        }

        binding.viewNegative.isVisible = sparseNegatives[position]
        binding.recyclerNumberOfImages.isVisible = sparseNumbers[position]
        binding.recyclerImageDimensions.isVisible = sparseDimensions[position]
        binding.viewAdvanced.isVisible = sparseAdvanceds[position]

        binding.viewDropNegative.clicks { showOrHideNegative(binding, position, !sparseNegatives[position]) }
        binding.viewDropNumbers.clicks { showOrHideNumber(binding, position, !sparseNumbers[position]) }
        binding.viewDropDimensions.clicks { showOrHideDimension(binding, position, !sparseDimensions[position] )}
        binding.viewDropAdvanced.clicks { showOrHideAdvanced(binding, position, !sparseAdvanceds[position]) }
    }

    private fun showOrHideNegative(binding: ItemPromptBatchBinding, position: Int, isShow: Boolean){
        binding.viewNegative.isVisible = isShow
        binding.viewDropNegative.rotation = if (isShow) 0f else 90f

        sparseNegatives.put(position, isShow)
    }

    private fun showOrHideNumber(binding: ItemPromptBatchBinding, position: Int, isShow: Boolean){
        binding.recyclerNumberOfImages.isVisible = isShow
        binding.viewDropNumbers.rotation = if (isShow) 0f else 90f

        sparseNumbers.put(position, isShow)
    }

    private fun showOrHideDimension(binding: ItemPromptBatchBinding, position: Int, isShow: Boolean){
        binding.recyclerImageDimensions.isVisible = isShow
        binding.viewDropDimensions.rotation = if (isShow) 0f else 90f

        sparseDimensions.put(position, isShow)
    }

    private fun showOrHideAdvanced(binding: ItemPromptBatchBinding, position: Int, isShow: Boolean){
        binding.viewAdvanced.isVisible = isShow
        binding.viewDropAdvanced.rotation = if (isShow) 0f else 90f

        sparseAdvanceds.put(position, isShow)
    }

//    private fun animShowOrHideNegative(binding: ItemPromptBatchBinding, position: Int, isShow: Boolean){
//        binding.viewNegative.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
//
//        val startHeight = if (isShow) 0 else binding.viewNegative.measuredHeight
//        val endHeight = if (isShow) binding.viewNegative.measuredHeight else 0
//        ValueAnimator.ofInt(startHeight, endHeight).apply {
//            addUpdateListener { animation ->
//                val height = animation.animatedValue as Int
//
//                binding.viewNegative.updateLayoutParams<ViewGroup.LayoutParams> {
//                    this.height = height
//                }
//            }
//            doOnStart {
//                binding.viewDropNegative.isClickable = false
//            }
//            doOnEnd {
//                binding.viewDropNegative.isClickable = true
//
//                sparseNegatives.put(position, isShow)
//            }
//            duration = 250
//            start()
//        }
//
//        val rotate = if (isShow) 0f else 90f
//        binding.viewDropNegative.animate().rotation(rotate).setDuration(250).start()
//    }

//    private fun animShowOrHideNumber(binding: ItemPromptBatchBinding, position: Int, isShow: Boolean){
//        binding.recyclerNumberOfImages.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
//
//        val startHeight = if (isShow) 0 else binding.recyclerNumberOfImages.measuredHeight
//        val endHeight = if (isShow) binding.recyclerNumberOfImages.measuredHeight else 0
//        ValueAnimator.ofInt(startHeight, endHeight).apply {
//            addUpdateListener { animation ->
//                val height = animation.animatedValue as Int
//
//                binding.recyclerNumberOfImages.updateLayoutParams<ViewGroup.LayoutParams> {
//                    this.height = height
//                }
//            }
//            doOnStart {
//                binding.viewDropNumbers.isClickable = false
//            }
//            doOnEnd {
//                binding.viewDropNumbers.isClickable = true
//
//                sparseNumbers.put(position, isShow)
//            }
//            duration = 250
//            start()
//        }
//
//        val rotate = if (isShow) 0f else 90f
//        binding.viewDropNumbers.animate().rotation(rotate).setDuration(250).start()
//    }

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