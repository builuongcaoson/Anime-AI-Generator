package com.sola.anime.ai.generator.feature.main.batch.adapter

import android.view.View
import androidx.core.view.isVisible
import coil.load
import coil.transition.CrossfadeTransition
import com.basic.common.base.LsAdapter
import com.basic.common.extension.clicks
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.databinding.ItemPreviewCategoryBatchBinding
import com.sola.anime.ai.generator.domain.model.PreviewCategoryBatch
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

class PreviewCategoryAdapter @Inject constructor(
    private val prefs: Preferences
): LsAdapter<PreviewCategoryBatch, ItemPreviewCategoryBatchBinding>(ItemPreviewCategoryBatchBinding::inflate) {

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
        binding.display.text = item.display
        binding.viewSelected.isVisible = item.display == category?.display
        binding.viewDescription.visibility = if (item.description.isNotEmpty()) View.VISIBLE else View.INVISIBLE
        binding.description.text = item.description

        binding.preview.load(item.preview) {
            crossfade(true)
        }

        binding.viewClicks.clicks(withAnim = false){ clicks.onNext(item) }
    }

}