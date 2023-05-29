package com.sola.anime.ai.generator.feature.result.art.adapter

import android.view.ViewGroup
import androidx.core.view.isVisible
import com.basic.common.base.LsAdapter
import com.basic.common.base.LsViewHolder
import com.basic.common.extension.clicks
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.sola.anime.ai.generator.R
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
        val context = binding.root.context

        Glide
            .with(context)
            .load(item.pathPreview)
            .transition(DrawableTransitionOptions.withCrossFade())
            .placeholder(R.drawable.place_holder_image)
            .error(R.drawable.place_holder_image)
            .into(binding.preview)

        binding.viewSelected.isVisible = childHistory == item
        binding.viewGroup.clicks(withAnim = false){ clicks.onNext(item) }
    }

}