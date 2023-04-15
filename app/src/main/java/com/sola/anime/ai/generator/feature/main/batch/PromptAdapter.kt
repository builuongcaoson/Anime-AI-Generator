package com.sola.anime.ai.generator.feature.main.batch

import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.basic.common.base.LsAdapter
import com.basic.common.base.LsViewHolder
import com.basic.common.extension.clicks
import com.basic.common.util.theme.TextViewStyler.Companion.FONT_REGULAR
import com.basic.common.util.theme.TextViewStyler.Companion.FONT_SEMI
import com.sola.anime.ai.generator.databinding.ItemCategoryBatchBinding
import com.sola.anime.ai.generator.databinding.ItemImageDimensionsBatchBinding
import com.sola.anime.ai.generator.databinding.ItemNumberOfImagesBatchBinding
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

        binding.recyclerNumberOfImages.apply {
            this.layoutManager = object: GridLayoutManager(context, 4, VERTICAL, false){
                override fun canScrollVertically(): Boolean {
                    return false
                }
            }
            this.adapter = NumberOfImagesAdapter()
        }
        binding.recyclerImageDimensions.apply {
            this.layoutManager = object: GridLayoutManager(context, 3, VERTICAL, false){
                override fun canScrollVertically(): Boolean {
                    return false
                }
            }
            this.adapter = ImageDimensionsAdapter()
        }
    }

    class NumberOfImagesAdapter: LsAdapter<Unit, ItemNumberOfImagesBatchBinding>() {

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): LsViewHolder<ItemNumberOfImagesBatchBinding> {
            return LsViewHolder(parent, ItemNumberOfImagesBatchBinding::inflate)
        }

        override fun onBindViewHolder(holder: LsViewHolder<ItemNumberOfImagesBatchBinding>, position: Int) {
//        val item = getItem(position)
//        val binding = holder.binding
//        val context = binding.root.context

        }

        override fun getItemCount(): Int {
            return 8
        }

    }

    class ImageDimensionsAdapter: LsAdapter<Unit, ItemImageDimensionsBatchBinding>() {

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): LsViewHolder<ItemImageDimensionsBatchBinding> {
            return LsViewHolder(parent, ItemImageDimensionsBatchBinding::inflate)
        }

        override fun onBindViewHolder(holder: LsViewHolder<ItemImageDimensionsBatchBinding>, position: Int) {
//        val item = getItem(position)
//        val binding = holder.binding
//        val context = binding.root.context

        }

        override fun getItemCount(): Int {
            return 6
        }

    }

}