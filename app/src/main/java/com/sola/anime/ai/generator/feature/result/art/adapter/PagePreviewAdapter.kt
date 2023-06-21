package com.sola.anime.ai.generator.feature.result.art.adapter

import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import com.basic.common.base.LsAdapter
import com.basic.common.extension.clicks
import com.basic.common.extension.tryOrNull
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.databinding.ItemPreview2ArtResultBinding
import com.sola.anime.ai.generator.domain.model.history.ChildHistory
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

class PagePreviewAdapter @Inject constructor() : LsAdapter<ChildHistory, ItemPreview2ArtResultBinding>(ItemPreview2ArtResultBinding::inflate) {

    val clicks: Subject<ChildHistory> = PublishSubject.create()
    val upscaleClicks: Subject<ChildHistory> = PublishSubject.create()

    override fun bindItem(
        item: ChildHistory,
        binding: ItemPreview2ArtResultBinding,
        position: Int
    ) {
        val context = binding.root.context

        binding.viewEnhance.isVisible = item.upscalePathPreview == null

        tryOrNull {
            val ratio = "${item.width}:${item.height}"
            val set = ConstraintSet()
            set.clone(binding.viewGroup)
            set.setDimensionRatio(binding.cardPreview.id, ratio)
            set.applyTo(binding.viewGroup)
        }

        tryOrNull {
            Glide
                .with(context)
                .load(item.upscalePathPreview ?: item.pathPreview)
                .centerCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .error(R.drawable.place_holder_image)
                .into(binding.preview)
        }

        binding.cardPreview.clicks(withAnim = false){ clicks.onNext(item) }
        binding.viewEnhance.clicks(withAnim = false){ upscaleClicks.onNext(item) }
    }

}