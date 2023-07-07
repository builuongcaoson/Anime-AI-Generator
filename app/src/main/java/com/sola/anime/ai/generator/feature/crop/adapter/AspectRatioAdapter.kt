package com.sola.anime.ai.generator.feature.crop.adapter

import com.basic.common.base.LsAdapter
import com.basic.common.extension.clicks
import com.basic.common.extension.resolveAttrColor
import com.sola.anime.ai.generator.databinding.ItemAspectRatioCropBinding
import com.sola.anime.ai.generator.domain.model.Ratio
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

class AspectRatioAdapter @Inject constructor(): LsAdapter<Ratio, ItemAspectRatioCropBinding>(ItemAspectRatioCropBinding::inflate) {

    init {
        data = Ratio.values().toList()
    }

    val clicks: Subject<Ratio> = PublishSubject.create()
    var ratio = Ratio.Ratio1x1
        set(value) {
            if (field == value) return

            notifyItemChanged(field.ordinal)

            field = value

            notifyItemChanged(value.ordinal)
        }

    override fun bindItem(item: Ratio, binding: ItemAspectRatioCropBinding, position: Int) {
        binding.display.setTextColor(
            when (ratio) {
                item -> binding.root.context.resolveAttrColor(android.R.attr.textColorPrimary)
                else -> binding.root.context.resolveAttrColor(android.R.attr.textColorSecondary)
            }
        )
        binding.display.text = item.display

        binding.display.clicks { clicks.onNext(item) }
    }

}