package com.sola.anime.ai.generator.feature.main.explore.adapter

import coil.load
import com.basic.common.base.LsAdapter
import com.basic.common.extension.clicks
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.databinding.ItemModelLoraInBatchBinding
import com.sola.anime.ai.generator.domain.model.ModelOrLoRA
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

class ModelAndLoRAPreviewAdapter @Inject constructor(
    private val prefs: Preferences
): LsAdapter<ModelOrLoRA, ItemModelLoraInBatchBinding>(ItemModelLoraInBatchBinding::inflate) {

    val clicks: Subject<ModelOrLoRA> = PublishSubject.create()

    override fun bindItem(item: ModelOrLoRA, binding: ItemModelLoraInBatchBinding, position: Int) {
        val preview = when {
            item.model != null -> item.model.preview
            item.loRA != null -> item.loRA.previews.firstOrNull()
            else -> null
        }
        binding.preview.load(preview) {
            crossfade(true)
            error(R.drawable.place_holder_image)
        }

        binding.description.text = if (item.model != null) "Model" else if (item.loRA != null) "LoRA" else ""
        binding.display.text = item.display
        val favouriteCount = when {
            item.model != null -> if (prefs.getFavouriteCountModelId(modelId = item.model.id)) item.favouriteCount + 1 else item.favouriteCount
            item.loRA != null -> if (prefs.getFavouriteCountModelId(modelId = item.loRA.id)) item.favouriteCount + 1 else item.favouriteCount
            else -> 0
        }
        binding.favouriteCount.text = "$favouriteCount Uses"

        binding.viewClicks.clicks(withAnim = false) { clicks.onNext(item) }
    }

}