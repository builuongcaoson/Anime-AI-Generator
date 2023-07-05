package com.sola.anime.ai.generator.feature.model.adapter

import android.view.View
import androidx.core.view.isVisible
import com.basic.common.base.LsAdapter
import com.basic.common.extension.clicks
import com.bumptech.glide.Glide
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.databinding.ItemPreviewModelBinding
import com.sola.anime.ai.generator.domain.model.config.model.Model
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

class PreviewAdapter @Inject constructor(
    private val prefs: Preferences
): LsAdapter<Model, ItemPreviewModelBinding>(ItemPreviewModelBinding::inflate) {

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
        binding.viewDescription.visibility = if (item.description.isNotEmpty()) View.VISIBLE else View.INVISIBLE
        binding.description.text = item.description
        binding.viewPremium.isVisible = item.premium && !prefs.isUpgraded.get()

        Glide
            .with(context)
            .load(item.preview)
            .error(R.drawable.place_holder_image)
            .into(binding.preview)

        binding.viewPreview.clicks { clicks.onNext(item) }
    }

}