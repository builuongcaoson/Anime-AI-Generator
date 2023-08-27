package com.sola.anime.ai.generator.feature.detailExplore.adapter

import android.content.Context
import androidx.constraintlayout.widget.ConstraintSet
import com.basic.common.base.LsAdapter
import com.basic.common.extension.clicks
import com.basic.common.extension.getColorCompat
import com.basic.common.extension.setTint
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.common.extension.load
import com.sola.anime.ai.generator.databinding.ItemPreviewExploreBinding
import com.sola.anime.ai.generator.domain.model.config.explore.Explore
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

class ExploreAdapter @Inject constructor(
    private val context: Context
): LsAdapter<Explore, ItemPreviewExploreBinding>(ItemPreviewExploreBinding::inflate) {

    val clicks: Subject<Explore> = PublishSubject.create()
    val favouriteClicks: Subject<Explore> = PublishSubject.create()

    override fun bindItem(item: Explore, binding: ItemPreviewExploreBinding, position: Int) {
        ConstraintSet().apply {
            this.clone(binding.viewGroup)
            this.setDimensionRatio(binding.viewClicks.id, item.ratio)
            this.applyTo(binding.viewGroup)
        }

        binding.preview.load(item.previews.firstOrNull(), errorRes = R.drawable.place_holder_image)
        binding.favourite.setTint(context.getColorCompat(if (item.isFavourite) R.color.red else R.color.white))
        binding.prompt.text = item.prompt

        binding.favourite.clicks {
            item.isFavourite = !item.isFavourite
            binding.favourite.setTint(context.getColorCompat(if (item.isFavourite) R.color.red else R.color.white))

            favouriteClicks.onNext(item)
        }
        binding.viewClicks.clicks(withAnim = false) { clicks.onNext(item) }
    }

}