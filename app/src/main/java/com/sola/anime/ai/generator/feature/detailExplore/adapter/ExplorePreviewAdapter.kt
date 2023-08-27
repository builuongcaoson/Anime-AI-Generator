package com.sola.anime.ai.generator.feature.detailExplore.adapter

import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import com.basic.common.base.LsAdapter
import com.basic.common.extension.clicks
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.common.extension.load
import com.sola.anime.ai.generator.databinding.ItemPreviewExploreBinding
import com.sola.anime.ai.generator.domain.model.ExplorePreview
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

class ExplorePreviewAdapter @Inject constructor(): LsAdapter<ExplorePreview, ItemPreviewExploreBinding>(ItemPreviewExploreBinding::inflate) {

    val clicks: Subject<ExplorePreview> = PublishSubject.create()

    override fun bindItem(item: ExplorePreview, binding: ItemPreviewExploreBinding, position: Int) {
        ConstraintSet().apply {
            this.clone(binding.viewGroup)
            this.setDimensionRatio(binding.viewClicks.id, item.ratio)
            this.applyTo(binding.viewGroup)
        }

        binding.preview.load(item.preview, R.drawable.place_holder_image)
        binding.favourite.isVisible = false
        binding.blurView.isVisible = false

        binding.viewClicks.clicks(withAnim = false) { clicks.onNext(item) }
    }

}