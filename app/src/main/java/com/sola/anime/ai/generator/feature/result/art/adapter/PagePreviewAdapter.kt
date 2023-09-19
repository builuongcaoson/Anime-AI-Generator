package com.sola.anime.ai.generator.feature.result.art.adapter

import android.annotation.SuppressLint
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import com.basic.common.base.LsAdapter
import com.basic.common.extension.clicks
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.common.extension.load
import com.sola.anime.ai.generator.databinding.ItemPreview2ArtResultBinding
import com.sola.anime.ai.generator.domain.model.history.ChildHistory
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

class PagePreviewAdapter @Inject constructor() : LsAdapter<ChildHistory, ItemPreview2ArtResultBinding>(ItemPreview2ArtResultBinding::inflate) {

    val clicks: Subject<ChildHistory> = PublishSubject.create()
    val upscaleClicks: Subject<ChildHistory> = PublishSubject.create()
    val longClicks: Subject<ChildHistory> = PublishSubject.create()

    override fun bindItem(
        item: ChildHistory,
        binding: ItemPreview2ArtResultBinding,
        position: Int
    ) {
        val set = ConstraintSet()
        set.clone(binding.viewGroup)
        set.setDimensionRatio(binding.cardPreview.id, "${item.width}:${item.height}")
        set.applyTo(binding.viewGroup)

        binding.preview.load(item.upscalePathPreview ?: item.pathPreview, errorRes = R.drawable.place_holder_image)
        binding.viewUpscale.isVisible = item.upscalePathPreview == null

        binding.cardPreview.clicks(withAnim = false){ clicks.onNext(item) }
        binding.cardPreview.setOnLongClickListener {
            longClicks.onNext(item)
            return@setOnLongClickListener false
        }
        binding.viewUpscale.clicks(withAnim = false){ upscaleClicks.onNext(item) }

    }

}