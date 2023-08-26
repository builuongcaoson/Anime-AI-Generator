package com.sola.anime.ai.generator.feature.detailModelOrLoRA.adapter

import android.content.Context
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import coil.load
import com.basic.common.base.LsAdapter
import com.basic.common.extension.clicks
import com.basic.common.extension.getColorCompat
import com.basic.common.extension.setTint
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.databinding.ItemPreviewExploreOrLoraBinding
import com.sola.anime.ai.generator.domain.model.ExploreOrLoRA
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

class ExploreOrLoRAAdapter @Inject constructor(
    private val context: Context
): LsAdapter<ExploreOrLoRA, ItemPreviewExploreOrLoraBinding>(ItemPreviewExploreOrLoraBinding::inflate) {

    val clicks: Subject<ExploreOrLoRA> = PublishSubject.create()
    val favouriteClicks: Subject<ExploreOrLoRA> = PublishSubject.create()

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
                binding.favouriteCount.text = "${if (item.isFavourite) item.favouriteCount + 1 else item.favouriteCount} Uses"
            }
        }
        binding.viewPromptExplore.isVisible = item.explore != null
        binding.viewDetailLoRA.isVisible = item.loRA != null
        binding.favourite.setTint(context.getColorCompat(if (item.isFavourite) R.color.red else R.color.white))

        binding.favourite.clicks {
            item.explore?.isFavourite = !(item.explore?.isFavourite ?: false)
            item.loRA?.isFavourite = !(item.loRA?.isFavourite ?: false)
            item.isFavourite = !item.isFavourite
            binding.favourite.setTint(context.getColorCompat(if (item.isFavourite) R.color.red else R.color.white))

            favouriteClicks.onNext(item)
        }
        binding.viewClicks.clicks(withAnim = false) { clicks.onNext(item) }
    }

}