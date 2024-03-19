package com.sola.anime.ai.generator.feature.main.mine.adapter

import androidx.constraintlayout.widget.ConstraintSet
import com.basic.common.base.LsAdapter
import com.basic.common.extension.clicks
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.common.extension.load
import com.sola.anime.ai.generator.databinding.ItemHistoryMineBinding
import com.sola.anime.ai.generator.domain.model.history.History
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

class HistoryAdapter @Inject constructor(): LsAdapter<History, ItemHistoryMineBinding>(ItemHistoryMineBinding::inflate) {

    val clicks: Subject<History> = PublishSubject.create()
    val longClicks: Subject<History> = PublishSubject.create()

    override fun bindItem(item: History, binding: ItemHistoryMineBinding, position: Int) {
        val ratio = "${item.childs.lastOrNull()?.width ?: "1"}:${item.childs.lastOrNull()?.height ?: "1"}"

        val set = ConstraintSet()
        set.clone(binding.viewGroup)
        set.setDimensionRatio(binding.cardPreview.id, ratio)
        set.applyTo(binding.viewGroup)

        val lastChild = item.childs.lastOrNull()
        binding.preview.load(lastChild?.upscalePathPreview ?: lastChild?.pathPreview, errorRes = R.drawable.place_holder_image)

        binding.title.text = item.title
        binding.prompt.text = item.childs.lastOrNull()?.prompt ?: ""

        binding.viewGroup.clicks(withAnim = false) { clicks.onNext(item) }
        binding.viewGroup.setOnLongClickListener {
            longClicks.onNext(item)
            false
        }
    }

}