package com.sola.anime.ai.generator.feature.main.batch

import android.view.ViewGroup
import com.basic.common.base.LsAdapter
import com.basic.common.base.LsViewHolder
import com.basic.common.extension.clicks
import com.basic.common.util.theme.TextViewStyler.Companion.FONT_REGULAR
import com.basic.common.util.theme.TextViewStyler.Companion.FONT_SEMI
import com.sola.anime.ai.generator.databinding.ItemCategoryBatchBinding
import com.sola.anime.ai.generator.databinding.ItemPromptBatchBinding
import com.sola.anime.ai.generator.domain.model.CategoryBatch
import com.sola.anime.ai.generator.domain.model.PromptBatch
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

class PromptAdapter @Inject constructor(): LsAdapter<PromptBatch, ItemPromptBatchBinding>() {

    init {
        data = listOf(
            PromptBatch(prompt = "Models")
        )
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LsViewHolder<ItemPromptBatchBinding> {
        return LsViewHolder(parent, ItemPromptBatchBinding::inflate)
    }

    override fun onBindViewHolder(holder: LsViewHolder<ItemPromptBatchBinding>, position: Int) {
        val item = getItem(position)
        val binding = holder.binding
        val context = binding.root.context


    }

}