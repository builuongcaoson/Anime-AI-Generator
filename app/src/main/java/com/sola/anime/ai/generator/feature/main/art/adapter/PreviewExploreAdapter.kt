package com.sola.anime.ai.generator.feature.main.art.adapter

import android.graphics.drawable.Drawable
import androidx.constraintlayout.widget.ConstraintSet
import coil.load
import coil.transition.CrossfadeTransition
import com.basic.common.base.LsAdapter
import com.basic.common.extension.clicks
import com.basic.common.extension.getDimens
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.databinding.ItemPreviewExploreBinding
import com.sola.anime.ai.generator.domain.model.config.explore.Explore
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

class PreviewExploreAdapter @Inject constructor(): LsAdapter<Explore, ItemPreviewExploreBinding>(ItemPreviewExploreBinding::inflate) {

    val clicks: Subject<Explore> = PublishSubject.create()

    override fun bindItem(item: Explore, binding: ItemPreviewExploreBinding, position: Int) {
        val context = binding.root.context

        val set = ConstraintSet()
        set.clone(binding.viewGroup)
        set.setDimensionRatio(binding.viewClicks.id, item.ratio)
        set.applyTo(binding.viewGroup)

        val preview = item.previews.firstOrNull()
        binding.preview.load(preview) {
            crossfade(true)
            error(R.drawable.place_holder_image)
        }

        binding.prompt.text = item.prompt

        binding.viewClicks.clicks { clicks.onNext(item) }
    }

}