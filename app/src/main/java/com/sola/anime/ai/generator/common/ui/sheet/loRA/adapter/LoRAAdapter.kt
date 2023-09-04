package com.sola.anime.ai.generator.common.ui.sheet.loRA.adapter

import android.view.View
import androidx.core.view.isVisible
import com.basic.common.base.LsAdapter
import com.basic.common.extension.clicks
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.common.extension.load
import com.sola.anime.ai.generator.databinding.ItemPreviewLoraBinding
import com.sola.anime.ai.generator.domain.model.config.lora.LoRA
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

class LoRAAdapter @Inject constructor(): LsAdapter<LoRA, ItemPreviewLoraBinding>(ItemPreviewLoraBinding::inflate) {

    val clicks: Subject<LoRA> = PublishSubject.create()
    var loRA: LoRA? = null
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

    override fun bindItem(item: LoRA, binding: ItemPreviewLoraBinding, position: Int) {
        binding.preview.load(item.previews.firstOrNull(), errorRes = R.drawable.place_holder_image)
        binding.display.text = item.display
        binding.viewSelected.isVisible = item.id == loRA?.id

        binding.viewPreview.clicks { clicks.onNext(item) }
    }

}