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
    var loRA: LoRA? = null
        set(value) {
            if (field == value){
                return
            }

            if (value == null){
                val oldIndex = data.indexOfFirst { it.second.any { loRAPreview -> loRAPreview.loRA == field } }

                field = null

                oldIndex.takeIf { it != -1 }?.let { notifyItemChanged(oldIndex) }
                return
            }

            data.indexOfFirst { it.second.any { loRAPreview -> loRAPreview.loRA == field } }.takeIf { it != -1 }?.let { notifyItemChanged(it) }
            data.indexOfFirst { it.second.any { loRAPreview -> loRAPreview.loRA == field } }.takeIf { it != -1 }?.let { notifyItemChanged(it) }

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
                this.loRA = this@GroupLoRAAdapter.loRA
                this.data = item.second
                this.emptyView = binding.viewEmpty
            }
        }
    }

}

class LoRAAdapter: LsAdapter<LoRAPreview, ItemLoraBinding>(ItemLoraBinding::inflate) {

    var clicks: Subject<LoRAPreview> = PublishSubject.create()
    var detailsClicks: Subject<LoRAPreview> = PublishSubject.create()
    var loRA: LoRA? = null
        set(value) {
            if (field == value){
                return
            }

            if (value == null){
                val oldIndex = data.indexOfFirst { it.loRA == field }

                field = null

                notifyItemChanged(oldIndex)
                return
            }

            data.indexOfFirst { it.loRA == field }.takeIf { it != -1 }?.let { notifyItemChanged(it) }
            data.indexOfFirst { it.loRA == value }.takeIf { it != -1 }?.let { notifyItemChanged(it) }

            field = value
        }

    override fun bindItem(item: LoRAPreview, binding: ItemLoraBinding, position: Int) {
        binding.preview.load(item.loRA.previews.firstOrNull(), errorRes = R.drawable.place_holder_image){ drawable ->
            binding.viewShadow.isVisible = drawable != null
        }
        binding.display.text = item.loRA.display
        binding.viewSelected.isVisible = item.loRA == loRA

        binding.viewPreview.clicks { clicks.onNext(item) }
        binding.viewDetails.clicks { detailsClicks.onNext(item) }
    }

}