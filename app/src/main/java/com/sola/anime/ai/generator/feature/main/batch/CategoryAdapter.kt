package com.sola.anime.ai.generator.feature.main.batch

import android.view.ViewGroup
import com.basic.common.base.LsAdapter
import com.basic.common.base.LsViewHolder
import com.basic.common.extension.clicks
import com.basic.common.util.theme.TextViewStyler.Companion.FONT_REGULAR
import com.basic.common.util.theme.TextViewStyler.Companion.FONT_SEMI
import com.sola.anime.ai.generator.databinding.ItemCategoryBatchBinding
import com.sola.anime.ai.generator.domain.model.CategoryBatch
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

class CategoryAdapter @Inject constructor(): LsAdapter<CategoryBatch, ItemCategoryBatchBinding>() {

    init {
        data = listOf(
            CategoryBatch(display = "Models"),
            CategoryBatch(display = "Characters")
        )
    }

    val clicks: Subject<CategoryBatch> = PublishSubject.create()
    var category = data.first()
        set(value) {
            if (field == value){
                return
            }

            val oldIndex = data.indexOf(field)
            val newIndex = data.indexOf(value)

            notifyItemChanged(oldIndex)
            notifyItemChanged(newIndex)

            field = value
        }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LsViewHolder<ItemCategoryBatchBinding> {
        return LsViewHolder(parent, ItemCategoryBatchBinding::inflate)
    }

    override fun onBindViewHolder(holder: LsViewHolder<ItemCategoryBatchBinding>, position: Int) {
        val item = getItem(position)
        val binding = holder.binding
        val context = binding.root.context

        binding.display.text = item.display

        when (item){
            category -> {
                binding.display.setTextFont(FONT_SEMI)
            }
            else -> {
                binding.display.setTextFont(FONT_REGULAR)
            }
        }

        binding.display.clicks { clicks.onNext(item) }
    }

}