package com.sola.anime.ai.generator.feature.art.art.adapter

import androidx.constraintlayout.widget.ConstraintSet
import com.basic.common.base.LsAdapter
import com.basic.common.extension.clicks
import com.basic.common.extension.resolveAttrColor
import com.sola.anime.ai.generator.databinding.ItemAspectRatioBinding
import com.sola.anime.ai.generator.domain.model.Ratio
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

class AspectRatioAdapter @Inject constructor(): LsAdapter<Ratio, ItemAspectRatioBinding>(ItemAspectRatioBinding::inflate) {

    init {
        data = Ratio.values().toList()
    }

    val clicks: Subject<Ratio> = PublishSubject.create()
    var ratio: Ratio = Ratio.Ratio1x1
        set(value) {
            if (field == value){
                return
            }

            val oldIndex = data.indexOf(field)
            val newIndex = data.indexOf(value)

            notifyItemChanged(oldIndex)
            notifyItemChanged(newIndex)

            field = value
        }

    override fun bindItem(item: Ratio, binding: ItemAspectRatioBinding, position: Int) {
        val context = binding.root.context

        binding.display.text = item.display

        ConstraintSet().apply {
            clone(binding.viewPreviewRatio)
            setDimensionRatio(binding.previewRatio.id, item.ratio)
            applyTo(binding.viewPreviewRatio)
        }

        when (item) {
            ratio -> {
                binding.viewClicks.setCardBackgroundColor(context.resolveAttrColor(android.R.attr.colorAccent))
                binding.previewRatio.setCardBackgroundColor(context.resolveAttrColor(com.google.android.material.R.attr.colorOnPrimary))
                binding.display.setTextColor(context.resolveAttrColor(com.google.android.material.R.attr.colorOnPrimary))
            }
            else -> {
                binding.viewClicks.setCardBackgroundColor(context.resolveAttrColor(android.R.attr.colorBackground))
                binding.previewRatio.setCardBackgroundColor(context.resolveAttrColor(android.R.attr.textColorPrimary))
                binding.display.setTextColor(context.resolveAttrColor(android.R.attr.textColorPrimary))
            }
        }

        binding.viewClicks.clicks { clicks.onNext(item) }
    }

}