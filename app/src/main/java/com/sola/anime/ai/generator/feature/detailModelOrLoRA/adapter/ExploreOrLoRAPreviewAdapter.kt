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
import com.sola.anime.ai.generator.databinding.ItemPreviewExploreOrLoraBinding
import com.sola.anime.ai.generator.domain.model.ExploreOrLoRAPreview
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

class ExploreOrLoRAPreviewAdapter @Inject constructor(
    private val context: Context
): LsAdapter<ExploreOrLoRAPreview, ItemPreviewExploreOrLoraBinding>(ItemPreviewExploreOrLoraBinding::inflate) {

    val clicks: Subject<ExploreOrLoRAPreview> = PublishSubject.create()
    val favouriteClicks: Subject<ExploreOrLoRAPreview> = PublishSubject.create()

    override fun bindItem(item: ExploreOrLoRAPreview, binding: ItemPreviewExploreOrLoraBinding, position: Int) {
        ConstraintSet().apply {
            this.clone(binding.viewGroup)
            this.setDimensionRatio(binding.viewClicks.id, item.ratio)
            this.applyTo(binding.viewGroup)
        }

        val preview = when {
            item.explore != null -> item.explore.previews.firstOrNull()
            item.loRAPreview != null -> item.loRAPreview
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
        }
        binding.viewPromptExplore.isVisible = item.explore != null
        binding.favourite.setTint(context.getColorCompat(if (item.isFavourite) R.color.red else R.color.white))
        binding.favourite.isVisible = item.explore != null

        binding.favourite.clicks {
            item.explore?.isFavourite = !(item.explore?.isFavourite ?: false)
            item.isFavourite = !item.isFavourite
            binding.favourite.setTint(context.getColorCompat(if (item.isFavourite) R.color.red else R.color.white))

            favouriteClicks.onNext(item)
        }
        binding.viewClicks.clicks(withAnim = false) { clicks.onNext(item) }
    }

}