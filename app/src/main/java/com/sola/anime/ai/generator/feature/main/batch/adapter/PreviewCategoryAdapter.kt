package com.sola.anime.ai.generator.feature.main.batch.adapter

import android.view.ViewGroup
import com.basic.common.base.LsAdapter
import com.basic.common.base.LsViewHolder
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.databinding.ItemCategoryBatchBinding
import com.sola.anime.ai.generator.databinding.ItemPreviewCategoryBatchBinding
import com.sola.anime.ai.generator.domain.model.CategoryBatch
import com.sola.anime.ai.generator.domain.model.PreviewCategoryBatch
import javax.inject.Inject

class PreviewCategoryAdapter @Inject constructor(): LsAdapter<PreviewCategoryBatch, ItemPreviewCategoryBatchBinding>(ItemPreviewCategoryBatchBinding::inflate) {

    init {
        data = listOf(
//            PreviewCategoryBatch(display = "Anime", preview = R.drawable.first_preview_zzz1xxx1zzz_1),
//            PreviewCategoryBatch(display = "Anything V5", preview = R.drawable.first_preview_zzz1xxx1zzz_1),
//            PreviewCategoryBatch(display = "Anything V4", preview = R.drawable.first_preview_zzz1xxx1zzz_1),
//            PreviewCategoryBatch(display = "Anything V3", preview = R.drawable.first_preview_zzz1xxx1zzz_1),
//            PreviewCategoryBatch(display = "Anything V2", preview = R.drawable.first_preview_zzz1xxx1zzz_1)
        )
    }

    override fun bindItem(
        item: PreviewCategoryBatch,
        binding: ItemPreviewCategoryBatchBinding,
        position: Int
    ) {
        binding.display.text = item.display
    }

}