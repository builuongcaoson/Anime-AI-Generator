package com.sola.anime.ai.generator.feature.art.art.adapter

import com.basic.common.base.LsAdapter
import com.basic.common.extension.clicks
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.common.extension.load
import com.sola.anime.ai.generator.databinding.ItemLoraInArtBinding
import com.sola.anime.ai.generator.domain.model.LoRAPreview
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

class LoRAAdapter @Inject constructor(): LsAdapter<LoRAPreview, ItemLoraInArtBinding>(ItemLoraInArtBinding::inflate) {

    val clicks: Subject<LoRAPreview> = PublishSubject.create()
    val deleteClicks: Subject<Int> = PublishSubject.create()

    override fun bindItem(item: LoRAPreview, binding: ItemLoraInArtBinding, position: Int) {
        binding.previewStyle.load(item.loRA.previews.firstOrNull(), errorRes = R.drawable.place_holder_image)
        binding.display.text = item.loRA.display
        binding.slider.currentValue = item.strength

        binding.slider.setListener { _, currentValue -> item.strength = currentValue }
        binding.cardLoRA.clicks(withAnim = false) { clicks.onNext(item) }
        binding.delete.clicks(withAnim = false) { deleteClicks.onNext(position) }
    }

}