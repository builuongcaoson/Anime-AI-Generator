package com.sola.anime.ai.generator.feature.main.batch

import android.view.ViewGroup
import com.basic.common.base.LsAdapter
import com.basic.common.base.LsViewHolder
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.databinding.ItemCategoryBatchBinding
import com.sola.anime.ai.generator.databinding.ItemPreviewCategoryBatchBinding
import com.sola.anime.ai.generator.domain.model.CategoryBatch
import com.sola.anime.ai.generator.domain.model.PreviewCategoryBatch
import javax.inject.Inject

class PreviewCategoryAdapter @Inject constructor(): LsAdapter<PreviewCategoryBatch, ItemPreviewCategoryBatchBinding>() {

    init {
        data = listOf(
            PreviewCategoryBatch(display = "Anime", preview = R.drawable.preview_1),
            PreviewCategoryBatch(display = "Anything V5", preview = R.drawable.preview_1),
            PreviewCategoryBatch(display = "Anything V4", preview = R.drawable.preview_1),
            PreviewCategoryBatch(display = "Anything V3", preview = R.drawable.preview_1),
            PreviewCategoryBatch(display = "Anything V2", preview = R.drawable.preview_1)
        )
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LsViewHolder<ItemPreviewCategoryBatchBinding> {
        return LsViewHolder(parent, ItemPreviewCategoryBatchBinding::inflate)
    }

    override fun onBindViewHolder(holder: LsViewHolder<ItemPreviewCategoryBatchBinding>, position: Int) {
        val item = getItem(position)
        val binding = holder.binding

        binding.display.text = item.display
    }

}