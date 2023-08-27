package com.sola.anime.ai.generator.feature.result.art.adapter

import androidx.core.view.isVisible
import com.basic.common.base.LsAdapter
import com.basic.common.extension.clicks
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.common.extension.load
import com.sola.anime.ai.generator.databinding.ItemPreviewArtResultBinding
import com.sola.anime.ai.generator.domain.model.history.ChildHistory
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

class PreviewAdapter @Inject constructor() : LsAdapter<ChildHistory, ItemPreviewArtResultBinding>(ItemPreviewArtResultBinding::inflate) {

    val clicks: Subject<ChildHistory> = PublishSubject.create()
    var childHistory: ChildHistory? = null
        set(value) {
            if (field == value) return

            val oldIndex = data.indexOf(field)
            val newIndex = data.indexOf(value)

            notifyItemChanged(oldIndex)
            notifyItemChanged(newIndex)

            field = value
        }

    override fun bindItem(item: ChildHistory, binding: ItemPreviewArtResultBinding, position: Int) {
        binding.preview.load(item.upscalePathPreview ?: item.pathPreview, errorRes = R.drawable.place_holder_image)
        binding.viewSelected.isVisible = childHistory == item

        binding.viewGroup.clicks(withAnim = false){ clicks.onNext(item) }
    }

}