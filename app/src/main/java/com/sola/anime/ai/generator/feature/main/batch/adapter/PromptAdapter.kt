package com.sola.anime.ai.generator.feature.main.batch.adapter

import android.content.Context
import android.util.SparseBooleanArray
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import com.afollestad.materialdialogs.utils.MDUtil.textChanged
import com.basic.common.base.LsAdapter
import com.basic.common.extension.clicks
import com.basic.common.extension.resolveAttrColor
import com.sola.anime.ai.generator.databinding.ItemImageDimensionsBatchBinding
import com.sola.anime.ai.generator.databinding.ItemNumberOfImagesBatchBinding
import com.sola.anime.ai.generator.databinding.ItemPromptBatchBinding
import com.sola.anime.ai.generator.domain.model.NumberOfImages
import com.sola.anime.ai.generator.domain.model.PromptBatch
import com.sola.anime.ai.generator.domain.model.Ratio
import com.sola.anime.ai.generator.domain.model.Sampler
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

class PromptAdapter @Inject constructor(): LsAdapter<PromptBatch, ItemPromptBatchBinding>(ItemPromptBatchBinding::inflate) {

    init {
        data = listOf(PromptBatch())
    }

    val deleteClicks: Subject<Int> = PublishSubject.create()
    val modelClicks: Subject<Int> = PublishSubject.create()
    val styleClicks: Subject<Int> = PublishSubject.create()
    val numberOfImagesChanges: Subject<Pair<NumberOfImages, Int>> = PublishSubject.create()
    val fullHdChanges: Subject<Unit> = PublishSubject.create()

    private val sparseNumbers = SparseBooleanArray()
    private val sparseDimensions = SparseBooleanArray()
    private val sparseAdvanceds = SparseBooleanArray()

    override fun bindItem(item: PromptBatch, binding: ItemPromptBatchBinding, position: Int) {
        val context = binding.root.context

        initView(binding, context, item, position)

        showOrHideNumber(binding, sparseNumbers.get(0, true))
        showOrHideDimension(binding, sparseDimensions.get(0, true))
        showOrHideAdvanced(binding, sparseAdvanceds[position])

        binding.delete.clicks { deleteClicks.onNext(position) }
        binding.viewModel.clicks { modelClicks.onNext(position) }
        binding.viewStyle.clicks { styleClicks.onNext(position) }
        binding.viewDropNumbers.clicks { dropNumbersClicks(binding, position) }
        binding.viewDropDimensions.clicks { dropDimensionsClicks(binding, position) }
        binding.viewDropAdvanced.clicks { dropAdvancedClicks(binding, position) }

        binding.prompt.textChanged { edit -> item.prompt = edit.trim().takeIf { edit.isNotEmpty() }?.toString() ?: "" }
        binding.negative.textChanged { edit -> item.negativePrompt = edit.trim().takeIf { edit.isNotEmpty() }?.toString() ?: "" }
        binding.minusGuidance.clicks { minusOrPlusGuidance(binding, item, true) }
        binding.plusGuidance.clicks { minusOrPlusGuidance(binding, item, false) }
        binding.minusStep.clicks { minusOrPlusStep(binding, item, true) }
        binding.plusStep.clicks { minusOrPlusStep(binding, item, false) }
        binding.minusSampler.clicks { minusOrPlusSampler(binding, item, true) }
        binding.plusSampler.clicks { minusOrPlusSampler(binding, item, false) }
        binding.viewClicksFullHd.clicks(withAnim = false) {
            toggleFullHd(binding, item)

            fullHdChanges.onNext(Unit)
        }
    }

    private fun toggleFullHd(binding: ItemPromptBatchBinding, item: PromptBatch) {
        val newChecked = !item.isFullHd
        binding.switchFullHd.setNewChecked(newChecked)
        item.isFullHd = newChecked
    }

    private fun minusOrPlusSampler(
        binding: ItemPromptBatchBinding,
        item: PromptBatch,
        isMinus: Boolean
    ) {
        val samplers = Sampler.values()
        var index = samplers.indexOf(item.sampler)
        when {
            isMinus -> {
                index = if (index == 0) samplers.lastIndex else index - 1
            }
            else -> {
                index = if (index == samplers.lastIndex) 0 else index + 1
            }
        }
        item.sampler = samplers.getOrNull(index) ?: Sampler.Random
        binding.textSampler.text = samplers.getOrNull(index)?.display ?: Sampler.Random.display
    }

    private fun minusOrPlusStep(binding: ItemPromptBatchBinding, item: PromptBatch, isMinus: Boolean) {
        var step = item.step
        when {
            isMinus && step > 30 -> {
                step -= 5
            }
            !isMinus && step < 60 -> {
                step += 5
            }
        }
        item.step = step
        binding.textStep.text = step.toString()
    }

    private fun minusOrPlusGuidance(binding: ItemPromptBatchBinding, item: PromptBatch, isMinus: Boolean) {
        var guidance = item.guidance
        when {
            isMinus && guidance > 5f -> {
                guidance -= 0.5f
            }
            !isMinus && guidance < 10f -> {
                guidance += 0.5f
            }
        }
        item.guidance = guidance
        binding.textGuidance.text = guidance.toString()
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

        val model = item.model
        binding.viewNoModel.isVisible = model == null
        binding.viewHadModel.isVisible = model != null
        binding.displayModel.text = when (model) {
            null -> "Pick a Model"
            else -> model.display
        }

        val style = item.style
        binding.viewNoStyle.isVisible = style == null
        binding.viewHadStyle.isVisible = style != null
        binding.displayStyle.text = when (style) {
            null -> "Pick a Style"
            else -> style.display
        }

        binding.delete.isVisible = position != 0
        binding.textGuidance.text = item.guidance.toString()
        binding.textStep.text = item.step.toString()
        binding.textSampler.text = item.sampler.display
        binding.switchFullHd.setNewChecked(item.isFullHd)
    }

    private fun dropAdvancedClicks(binding: ItemPromptBatchBinding, position: Int) {
        val isShow = !sparseAdvanceds[position]
        showOrHideAdvanced(binding, isShow)
        sparseAdvanceds.put(position, isShow)
    }

//    private fun dropNegativeClicks(binding: ItemPromptBatchBinding, position: Int) {
//        val isShow = !sparseNegatives[position]
//        showOrHideNegative(binding, isShow)
//        sparseNegatives.put(position, isShow)
//    }

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

//    private fun showOrHideNegative(binding: ItemPromptBatchBinding, isShow: Boolean){
//        binding.viewNegative.isVisible = isShow
//        binding.viewDropNegative.rotation = if (isShow) 0f else 90f
//    }

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

//            binding.viewClicks.clicks(withAnim = false){ numberOfImagesChanges.onNext(item to promptPosition) }
            binding.viewClicks.clicks(withAnim = false){
                numberOfImages = item
                numberOfImagesChanges.onNext(item to promptPosition)
            }
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