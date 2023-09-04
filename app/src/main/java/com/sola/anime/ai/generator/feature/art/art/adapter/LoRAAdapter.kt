package com.sola.anime.ai.generator.feature.art.art.adapter

import androidx.core.view.isVisible
import com.basic.common.base.LsAdapter
import com.basic.common.extension.clicks
import com.sola.anime.ai.generator.databinding.ItemLoraInArtBinding
import com.sola.anime.ai.generator.domain.model.config.lora.LoRA
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

class LoRAAdapter @Inject constructor(): LsAdapter<LoRA?, ItemLoraInArtBinding>(ItemLoraInArtBinding::inflate) {

    init {
        data = listOf(null)
    }

    val clicks: Subject<Int> = PublishSubject.create()

    override fun bindItem(item: LoRA?, binding: ItemLoraInArtBinding, position: Int) {
        binding.viewNoLoRA.isVisible = item == null
        binding.viewHadLoRA.isVisible = item != null

        binding.displayLoRA.text = when (item) {
            null -> "Pick a LoRA"
            else -> item.display
        }

        binding.viewLoRA.clicks(withAnim = false) { clicks.onNext(position) }
    }

}