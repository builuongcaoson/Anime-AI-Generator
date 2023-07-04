package com.sola.anime.ai.generator.feature.model.adapter

import androidx.core.view.isVisible
import com.basic.common.base.LsAdapter
import com.basic.common.extension.clicks
import com.bumptech.glide.Glide
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.databinding.ItemPreviewModelBinding
import com.sola.anime.ai.generator.domain.model.config.model.Model
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

class PreviewAdapter @Inject constructor(): LsAdapter<Model, ItemPreviewModelBinding>(ItemPreviewModelBinding::inflate) {

    val clicks: Subject<Model> = PublishSubject.create()
    var model: Model? = null
        set(value) {
            if (field == value){
                return
            }

            if (value == null){
                val oldIndex = data.indexOf(field)

                field = null

                notifyItemChanged(oldIndex)
                return
            }

            data.indexOf(field).takeIf { it != -1 }?.let { notifyItemChanged(it) }
            data.indexOf(value).takeIf { it != -1 }?.let { notifyItemChanged(it) }

            field = value
        }

    override fun bindItem(item: Model, binding: ItemPreviewModelBinding, position: Int) {
        val context = binding.root.context

        binding.display.text = item.display
        binding.viewSelected.isVisible = item.id == model?.id
        binding.viewDescription.isVisible = item.description.isNotEmpty()
        binding.description.text = item.description

        Glide
            .with(context)
            .load(item.preview)
            .error(R.drawable.place_holder_image)
            .into(binding.preview)

        binding.viewPreview.clicks { clicks.onNext(item) }
    }

}