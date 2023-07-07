package com.sola.anime.ai.generator.feature.crop.adapter

import com.basic.common.base.LsAdapter
import com.basic.common.extension.clicks
import com.basic.common.extension.resolveAttrColor
import com.sola.anime.ai.generator.databinding.ItemAspectRatioCropBinding
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

class AspectRatioAdapter @Inject constructor(): LsAdapter<AspectRatioAdapter.AspectRatio, ItemAspectRatioCropBinding>(ItemAspectRatioCropBinding::inflate) {

    init {
        data = AspectRatio.values().toList()
    }

    val clicks: Subject<AspectRatio> = PublishSubject.create()

    var aspectRatioSelect = AspectRatio.OneToOne
        set(value) {
            if (field == value) return

            notifyItemChanged(field.ordinal)

            field = value

            notifyItemChanged(value.ordinal)
        }

    override fun bindItem(item: AspectRatio, binding: ItemAspectRatioCropBinding, position: Int) {
        binding.display.setTextColor(
            when (aspectRatioSelect) {
                item -> binding.root.context.resolveAttrColor(android.R.attr.textColorPrimary)
                else -> binding.root.context.resolveAttrColor(android.R.attr.textColorSecondary)
            }
        )
        binding.display.text = item.display

        binding.display.clicks { clicks.onNext(item) }
    }

    enum class AspectRatio(val display: String, val aspectRatio: Float) {
        OneToOne("1:1", 1f / 1f),
        NineToSixteen("9:16", 9f / 16f),
        SixteenToNine("16:9",16f / 9f),
        TwoToThree("2:3",2f / 3f),
        ThreeToTwo("3:2", 3f / 2f),
        ThreeToFour("3:4",3f / 4f),
        FourToThree("4:3",4f /3f)
    }

}