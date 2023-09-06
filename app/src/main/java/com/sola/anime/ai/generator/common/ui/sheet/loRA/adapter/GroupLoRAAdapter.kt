package com.sola.anime.ai.generator.common.ui.sheet.loRA.adapter

import androidx.core.view.isVisible
import com.basic.common.base.LsAdapter
import com.basic.common.extension.clicks
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.common.extension.load
import com.sola.anime.ai.generator.databinding.ItemGroupLoraBinding
import com.sola.anime.ai.generator.databinding.ItemLoraBinding
import com.sola.anime.ai.generator.domain.model.LoRAPreview
import com.sola.anime.ai.generator.domain.model.config.lora.LoRA
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

class GroupLoRAAdapter @Inject constructor(): LsAdapter<Pair<String, List<LoRAPreview>>, ItemGroupLoraBinding>(ItemGroupLoraBinding::inflate) {

    val clicks: Subject<LoRAPreview> = PublishSubject.create()
    val detailsClicks: Subject<LoRAPreview> = PublishSubject.create()
    var loRAs: List<LoRA> = listOf()
        set(value) {
            if (field == value){
                return
            }

//            if (value == null){
//                field?.forEach { loRA ->
//                    val oldIndex = data.indexOfFirst { it.second.any { loRAPreview -> loRAPreview.loRA == loRA } }
//
//                    field = null
//
//                    oldIndex.takeIf { it != -1 }?.let { notifyItemChanged(oldIndex) }
//                }
//                return
//            }

            field?.forEach { loRA ->
                val index = data.indexOfFirst { it.second.any { loRAPreview -> loRAPreview.loRA == loRA } }

                index.takeIf { it != -1 }?.let { notifyItemChanged(index) }
            }

            value.forEach { loRA ->
                val index = data.indexOfFirst { it.second.any { loRAPreview -> loRAPreview.loRA == loRA } }

                index.takeIf { it != -1 }?.let { notifyItemChanged(index) }
            }

            field = value
        }

    override fun bindItem(
        item: Pair<String, List<LoRAPreview>>,
        binding: ItemGroupLoraBinding,
        position: Int
    ) {
        binding.display.text = item.first
        binding.display.isVisible = item.first.isNotEmpty()
        binding.recyclerStyle.apply {
            this.adapter = LoRAAdapter().apply {
                this.clicks = this@GroupLoRAAdapter.clicks
                this.detailsClicks = this@GroupLoRAAdapter.detailsClicks
                this.loRAs = this@GroupLoRAAdapter.loRAs
                this.data = item.second
                this.emptyView = binding.viewEmpty
            }
        }
    }

}

class LoRAAdapter: LsAdapter<LoRAPreview, ItemLoraBinding>(ItemLoraBinding::inflate) {

    var clicks: Subject<LoRAPreview> = PublishSubject.create()
    var detailsClicks: Subject<LoRAPreview> = PublishSubject.create()
    var loRAs: List<LoRA> = listOf()
        set(value) {
            if (field == value){
                return
            }

//            if (value == null){
//                field?.forEach { loRA ->
//                    val oldIndex = data.indexOfFirst { loRAPreview -> loRAPreview.loRA == loRA }
//
//                    field = null
//
//                    oldIndex.takeIf { it != -1 }?.let { notifyItemChanged(oldIndex) }
//                }
//                return
//            }

            field.forEach { loRA ->
                val index = data.indexOfFirst { loRAPreview -> loRAPreview.loRA == loRA }

                index.takeIf { it != -1 }?.let { notifyItemChanged(index) }
            }

            value.forEach { loRA ->
                val index = data.indexOfFirst { loRAPreview -> loRAPreview.loRA == loRA }

                index.takeIf { it != -1 }?.let { notifyItemChanged(index) }
            }

            field = value
        }

    override fun bindItem(item: LoRAPreview, binding: ItemLoraBinding, position: Int) {
        binding.preview.load(item.loRA.previews.firstOrNull(), errorRes = R.drawable.place_holder_image){ drawable ->
            binding.viewShadow.isVisible = drawable != null
        }
        binding.display.text = item.loRA.display
        binding.viewSelected.isVisible = loRAs.contains(item.loRA)

        binding.viewPreview.clicks { clicks.onNext(item) }
        binding.viewDetails.clicks { detailsClicks.onNext(item) }
    }

}