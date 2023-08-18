package com.sola.anime.ai.generator.feature.detailModelOrLoRA.adapter

import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import coil.load
import com.basic.common.base.LsAdapter
import com.basic.common.extension.clicks
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.databinding.ItemPreviewExploreOrLoraBinding
import com.sola.anime.ai.generator.domain.model.ExploreOrLoRA
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

class ExploreOrLoRAAdapter @Inject constructor(
    private val prefs: Preferences
): LsAdapter<ExploreOrLoRA, ItemPreviewExploreOrLoraBinding>(ItemPreviewExploreOrLoraBinding::inflate) {

    val clicks: Subject<ExploreOrLoRA> = PublishSubject.create()

    override fun bindItem(item: ExploreOrLoRA, binding: ItemPreviewExploreOrLoraBinding, position: Int) {
        ConstraintSet().apply {
            this.clone(binding.viewGroup)
            this.setDimensionRatio(binding.viewClicks.id, item.ratio)
            this.applyTo(binding.viewGroup)
        }

        val preview = when {
            item.explore != null -> item.explore.previews.firstOrNull()
            item.loRA != null -> item.loRA.previews.firstOrNull()
            else -> null
        }
        binding.preview.load(preview) {
            crossfade(true)
            error(R.drawable.place_holder_image)
        }

        when {
            item.explore != null -> {
                binding.prompt.text = item.explore.prompt
            }
            item.loRA != null -> {
                binding.display.text = item.loRA.display
                binding.favouriteCount.text = "${if (prefs.getFavouriteCountLoRAId(item.loRA.id)) (item.loRA.favouriteCount + 1) else item.loRA.favouriteCount} Uses"
            }
        }
        binding.viewPromptExplore.isVisible = item.explore != null
        binding.viewDetailLoRA.isVisible = item.loRA != null

        binding.viewClicks.clicks(withAnim = false) { clicks.onNext(item) }
    }

}