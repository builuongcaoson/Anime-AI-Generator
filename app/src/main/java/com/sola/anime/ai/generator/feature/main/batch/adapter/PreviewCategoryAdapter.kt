package com.sola.anime.ai.generator.feature.main.batch.adapter

import android.view.View
import androidx.core.view.isVisible
import com.basic.common.base.LsAdapter
import com.basic.common.extension.clicks
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.sola.anime.ai.generator.data.db.query.ModelDao
import com.sola.anime.ai.generator.databinding.ItemPreviewCategoryBatchBinding
import com.sola.anime.ai.generator.domain.model.PreviewCategoryBatch
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

class PreviewCategoryAdapter @Inject constructor(
    private val modelDao: ModelDao
): LsAdapter<PreviewCategoryBatch, ItemPreviewCategoryBatchBinding>(ItemPreviewCategoryBatchBinding::inflate) {

    init {
        data = ArrayList(modelDao.getAll().map {
            PreviewCategoryBatch(preview = it.preview, display = it.display, model = it.model, description = it.description, isPremium = it.premium)
        })
        data.firstOrNull{ !it.isPremium }.also { this.category = it }
    }

    val clicks: Subject<PreviewCategoryBatch> = PublishSubject.create()
    var category: PreviewCategoryBatch? = null
        set(value) {
            if (field == value){
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
        binding.viewDescription.visibility = if (item.description.isNotEmpty()) View.VISIBLE else View.INVISIBLE
        binding.description.text = item.description
        binding.viewPremium.isVisible = item.isPremium

        Glide
            .with(context)
            .load(item.preview)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(binding.preview)

        binding.viewClicks.clicks(withAnim = false){ clicks.onNext(item) }
    }

}