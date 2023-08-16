package com.sola.anime.ai.generator.feature.main.art.adapter

import coil.load
import coil.transition.CrossfadeTransition
import com.basic.common.base.LsAdapter
import com.basic.common.extension.clicks
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.databinding.ItemPreviewArt2Binding
import com.sola.anime.ai.generator.domain.model.config.explore.Explore
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

class PreviewAdapter @Inject constructor(): LsAdapter<Explore, ItemPreviewArt2Binding>(ItemPreviewArt2Binding::inflate) {

    val clicks: Subject<Explore> = PublishSubject.create()

    override fun bindItem(item: Explore, binding: ItemPreviewArt2Binding, position: Int) {
        val preview = item.previews.firstOrNull()
        binding.preview.load(preview) {
            crossfade(true)
            error(R.drawable.place_holder_image)
        }

        binding.cardPreview.clicks(withAnim = false) { clicks.onNext(item) }
    }

}