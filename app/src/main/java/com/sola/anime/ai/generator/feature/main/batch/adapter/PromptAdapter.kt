package com.sola.anime.ai.generator.feature.main.batch.adapter

import android.content.Context
import android.util.SparseBooleanArray
import androidx.core.text.isDigitsOnly
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.GridLayoutManager
import com.basic.common.base.LsAdapter
import com.basic.common.extension.clicks
import com.basic.common.extension.resolveAttrColor
import com.basic.common.extension.tryOrNull
import com.sola.anime.ai.generator.databinding.ItemImageDimensionsBatchBinding
import com.sola.anime.ai.generator.databinding.ItemNumberOfImagesBatchBinding
import com.sola.anime.ai.generator.databinding.ItemPromptBatchBinding
import com.sola.anime.ai.generator.domain.model.NumberOfImages
import com.sola.anime.ai.generator.domain.model.PromptBatch
import com.sola.anime.ai.generator.domain.model.Ratio
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

class PromptAdapter @Inject constructor(): LsAdapter<PromptBatch, ItemPromptBatchBinding>(ItemPromptBatchBinding::inflate) {

    init {
        data = listOf(PromptBatch())
    }

    val deleteClicks: Subject<Int> = PublishSubject.create()
    val numberOfImagesChanges: Subject<Pair<NumberOfImages, Int>> = PublishSubject.create()

    private val sparseNegatives = SparseBooleanArray()
    private val sparseNumbers = SparseBooleanArray()
    private val sparseDimensions = SparseBooleanArray()
    private val sparseAdvanceds = SparseBooleanArray()

    override fun bindItem(item: PromptBatch, binding: ItemPromptBatchBinding, position: Int) {
        val context = binding.root.context

        initView(binding, context, item, position)

        showOrHideNegative(binding, sparseNegatives[position])
        showOrHideNumber(binding, sparseNumbers.get(0, true))
        showOrHideDimension(binding, sparseDimensions.get(0, true))
        showOrHideAdvanced(binding, sparseAdvanceds[position])

        binding.delete.clicks { deleteClicks.onNext(position) }
        binding.viewDropNegative.clicks { dropNegativeClicks(binding, position) }
        binding.viewDropNumbers.clicks { dropNumbersClicks(binding, position) }
        binding.viewDropDimensions.clicks { dropDimensionsClicks(binding, position) }
        binding.viewDropAdvanced.clicks { dropAdvancedClicks(binding, position) }
    }

    private fun initView(binding: ItemPromptBatchBinding, context: Context, item: PromptBatch, position: Int) {
        binding.recyclerNumberOfImages.apply {
            this.layoutManager = object: GridLayoutManager(context, 4, VERTICAL, false){
                override fun canScrollVertically(): Boolean {
                    return false
                }
            }
            this.itemAnimator = null
            this.adapter = NumberOfImagesAdapter(item, numberOfImagesChanges, position)
        }

        binding.recyclerImageDimensions.apply {
            this.layoutManager = object: GridLayoutManager(context, 4, VERTICAL, false){
                override fun canScrollVertically(): Boolean {
                    return false
                }
            }
            this.itemAnimator = null
            this.adapter = ImageDimensionsAdapter(item)
        }
        binding.delete.isVisible = position != 0
    }

    private fun dropAdvancedClicks(binding: ItemPromptBatchBinding, position: Int) {
        val isShow = !sparseAdvanceds[position]
        showOrHideAdvanced(binding, isShow)
        sparseAdvanceds.put(position, isShow)
    }

    private fun dropNegativeClicks(binding: ItemPromptBatchBinding, position: Int) {
        val isShow = !sparseNegatives[position]
        showOrHideNegative(binding, isShow)
        sparseNegatives.put(position, isShow)
    }

    private fun dropNumbersClicks(binding: ItemPromptBatchBinding, position: Int) {
        val isShow = !sparseNumbers.get(position, true)
        showOrHideNumber(binding, isShow)
        sparseNumbers.put(position, isShow)
    }

    private fun dropDimensionsClicks(binding: ItemPromptBatchBinding, position: Int) {
        val isShow = !sparseDimensions.get(position, true)
        showOrHideDimension(binding, isShow)
        sparseDimensions.put(position, isShow)
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

    class NumberOfImagesAdapter(
        private val promptBatch: PromptBatch,
        private val numberOfImagesChanges: Subject<Pair<NumberOfImages, Int>>,
        private val promptPosition: Int
    ): LsAdapter<NumberOfImages, ItemNumberOfImagesBatchBinding>(ItemNumberOfImagesBatchBinding::inflate) {

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
//            var isUserInput = true

            binding.display.text = item.display
//            binding.display.isVisible = position != data.lastIndex
//            when {
//                numberOfImages == item -> binding.edit.setText(item.display)
//                else -> binding.edit.hint = item.display
//            }
//            binding.edit.isVisible = position == data.lastIndex
//            binding.edit.doAfterTextChanged {
//                val value = it?.trim()?.takeIf { value -> value.isNotEmpty() && value.isDigitsOnly() }?.toString()?.let { value -> tryOrNull { value.toInt() } } ?: 0
//                when {
//                    isUserInput && (value <= 0 || value > 50) -> {
//                        isUserInput = false
//                        binding.edit.setText(binding.edit.text?.firstOrNull()?.toString()?.takeIf { firstChar -> firstChar != "0" }?: "")
//                        binding.edit.setSelection(binding.edit.text?.firstOrNull()?.toString()?.takeIf { firstChar -> firstChar != "0" }?.let { 1 } ?: 0)
//                        isUserInput = true
//                    }
//                    else -> {
//                        item.display = value.toString()
//                    }
//                }
//            }
//            binding.edit.setOnFocusChangeListener { v, hasFocus ->
//                when {
//                    v == binding.edit && hasFocus -> numberOfImages = item
//                    else -> {}
//                }
//            }

            when {
                numberOfImages == item -> {
                    binding.viewClicks.setCardBackgroundColor(context.resolveAttrColor(android.R.attr.colorAccent))
                    binding.display.setTextColor(context.resolveAttrColor(com.google.android.material.R.attr.colorOnPrimary))
//                    binding.edit.setTextColor(context.resolveAttrColor(com.google.android.material.R.attr.colorOnPrimary))
//                    binding.edit.setHintTextColor(context.resolveAttrColor(com.google.android.material.R.attr.colorOnTertiary))
                }
                else -> {
                    binding.viewClicks.setCardBackgroundColor(context.resolveAttrColor(android.R.attr.colorBackground))
                    binding.display.setTextColor(context.resolveAttrColor(android.R.attr.textColorPrimary))
//                    binding.edit.setTextColor(context.resolveAttrColor(android.R.attr.textColorPrimary))
//                    binding.edit.setHintTextColor(context.resolveAttrColor(android.R.attr.textColorTertiary))
                }
            }

            binding.viewClicks.clicks(withAnim = false){ numberOfImagesChanges.onNext(item to promptPosition) }
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