package com.sola.anime.ai.generator.feature.main.batch.adapter

import androidx.core.view.isVisible
import com.basic.common.base.LsAdapter
import com.basic.common.extension.clicks
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.databinding.ItemPreviewCategoryBatchBinding
import com.sola.anime.ai.generator.domain.model.PreviewCategoryBatch
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

class PreviewCategoryAdapter @Inject constructor(): LsAdapter<PreviewCategoryBatch, ItemPreviewCategoryBatchBinding>(ItemPreviewCategoryBatchBinding::inflate) {

    init {
        data = listOf(
            PreviewCategoryBatch(display = "Anime V5", preview = R.drawable.preview_model_anime_v5),
            PreviewCategoryBatch(display = "Anime V4", preview = R.drawable.preview_model_anime_v4),
            PreviewCategoryBatch(display = "Anime V3", preview = R.drawable.preview_model_anime_v3),
            PreviewCategoryBatch(display = "Waifu V4", preview = R.drawable.preview_model_waifu_v4),
            PreviewCategoryBatch(display = "Waifu V3", preview = R.drawable.preview_model_waifu_v3),
        )
    }

    val clicks: Subject<PreviewCategoryBatch> = PublishSubject.create()
    var category: PreviewCategoryBatch? = data.firstOrNull()
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

    override fun bindItem(
        item: PreviewCategoryBatch,
        binding: ItemPreviewCategoryBatchBinding,
        position: Int
    ) {
        val context = binding.root.context

        binding.display.text = item.display
        binding.viewSelected.isVisible = item.display == category?.display

        Glide
            .with(context)
            .load(item.preview)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(binding.preview)

        binding.viewClicks.clicks(withAnim = false){ clicks.onNext(item) }
    }

}