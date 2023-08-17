package com.sola.anime.ai.generator.feature.explore.adapter

import androidx.constraintlayout.widget.ConstraintSet
import coil.load
import com.basic.common.base.LsAdapter
import com.basic.common.extension.clicks
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.databinding.ItemPreviewExploreBinding
import com.sola.anime.ai.generator.domain.model.config.explore.Explore
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

class ExploreAdapter @Inject constructor(): LsAdapter<Explore, ItemPreviewExploreBinding>(ItemPreviewExploreBinding::inflate) {

    val clicks: Subject<Explore> = PublishSubject.create()

    override fun bindItem(item: Explore, binding: ItemPreviewExploreBinding, position: Int) {
        val set = ConstraintSet()
        set.clone(binding.viewGroup)
        set.setDimensionRatio(binding.viewClicks.id, item.ratio)
        set.applyTo(binding.viewGroup)

        binding.preview.load(item.previews.firstOrNull()) {
            crossfade(true)
            error(R.drawable.place_holder_image)
        }

        binding.prompt.text = item.prompt

        binding.viewClicks.clicks(withAnim = false) { clicks.onNext(item) }
    }

}