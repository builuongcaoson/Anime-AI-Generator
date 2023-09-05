package com.sola.anime.ai.generator.common.ui.sheet.style.adapter

import androidx.core.view.isVisible
import com.basic.common.base.LsAdapter
import com.basic.common.extension.clicks
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.common.extension.load
import com.sola.anime.ai.generator.databinding.ItemGroupStyleBinding
import com.sola.anime.ai.generator.databinding.ItemStyleBinding
import com.sola.anime.ai.generator.domain.model.config.style.Style
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

class GroupStyleAdapter @Inject constructor(): LsAdapter<Pair<String, List<Style>>, ItemGroupStyleBinding>(ItemGroupStyleBinding::inflate) {

    val clicks: Subject<Style> = PublishSubject.create()
    var style: Style? = null
        set(value) {
            if (field == value){
                return
            }

            if (value == null){
                val oldIndex = data.indexOfFirst { it.second.any { style -> style == field } }

                field = null

                oldIndex.takeIf { it != -1 }?.let { notifyItemChanged(oldIndex) }
                return
            }

            data.indexOfFirst { it.second.any { style -> style == field } }.takeIf { it != -1 }?.let { notifyItemChanged(it) }
            data.indexOfFirst { it.second.any { style -> style == field } }.takeIf { it != -1 }?.let { notifyItemChanged(it) }

            field = value
        }

    override fun bindItem(
        item: Pair<String, List<Style>>,
        binding: ItemGroupStyleBinding,
        position: Int
    ) {
        binding.display.text = item.first
        binding.display.isVisible = item.first.isNotEmpty()
        binding.recyclerStyle.apply {
            this.adapter = StyleAdapter().apply {
                this.clicks = this@GroupStyleAdapter.clicks
                this.style = this@GroupStyleAdapter.style
                this.data = item.second
                this.emptyView = binding.viewEmpty
            }
        }
    }

}

class StyleAdapter: LsAdapter<Style, ItemStyleBinding>(ItemStyleBinding::inflate) {

    var clicks: Subject<Style> = PublishSubject.create()
    var style: Style? = null
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

    override fun bindItem(item: Style, binding: ItemStyleBinding, position: Int) {
        binding.preview.load(item.preview, errorRes = R.drawable.place_holder_image)
        binding.display.text = item.display
        binding.viewSelected.isVisible = item.id == style?.id

        binding.viewPreview.clicks { clicks.onNext(item) }
    }

}