package com.sola.anime.ai.generator.feature.art.art.adapter

import com.basic.common.base.LsAdapter
import com.basic.common.extension.clicks
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.common.extension.load
import com.sola.anime.ai.generator.databinding.ItemLoraInArtBinding
import com.sola.anime.ai.generator.domain.model.config.lora.LoRA
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

class LoRAAdapter @Inject constructor(): LsAdapter<LoRA, ItemLoraInArtBinding>(ItemLoraInArtBinding::inflate) {

    val deleteClicks: Subject<Int> = PublishSubject.create()

    override fun bindItem(item: LoRA, binding: ItemLoraInArtBinding, position: Int) {
        binding.previewStyle.load(item.previews.firstOrNull(), errorRes = R.drawable.place_holder_image)
        binding.display.text = item.display

        binding.delete.clicks(withAnim = false) { deleteClicks.onNext(position) }
    }

}