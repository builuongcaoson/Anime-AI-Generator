package com.sola.anime.ai.generator.feature.main.mine.adapter

import android.view.ViewGroup
import androidx.core.view.isVisible
import com.basic.common.base.LsAdapter
import com.basic.common.base.LsViewHolder
import com.basic.common.extension.clicks
import com.basic.common.extension.resolveAttrColor
import com.basic.common.util.theme.FontManager.Companion.FONT_REGULAR
import com.basic.common.util.theme.FontManager.Companion.FONT_SEMI
import com.sola.anime.ai.generator.databinding.ItemFolderMineBinding
import com.sola.anime.ai.generator.domain.model.Folder
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

class FolderAdapter @Inject constructor(): LsAdapter<Folder?, ItemFolderMineBinding>(ItemFolderMineBinding::inflate) {

    val clicks: Subject<Folder> = PublishSubject.create()
    val plusClicks: Subject<Unit> = PublishSubject.create()
    var folder: Folder? = null
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

    override fun bindItem(item: Folder?, binding: ItemFolderMineBinding, position: Int) {
        val context = binding.root.context

        when {
            item != null -> {
                binding.display.text = item.display

                when (folder?.display) {
                    item.display -> {
                        binding.display.setTextColor(context.resolveAttrColor(com.google.android.material.R.attr.colorOnPrimary))
                        binding.viewFolder.setCardBackgroundColor(context.resolveAttrColor(android.R.attr.colorAccent))
                    }
                    else -> {
                        binding.display.setTextColor(context.resolveAttrColor(android.R.attr.textColorPrimary))
                        binding.viewFolder.setCardBackgroundColor(context.resolveAttrColor(android.R.attr.colorBackground))
                    }
                }

                binding.viewFolder.clicks { clicks.onNext(item) }
            }
            else -> {
                binding.viewPlus.clicks { plusClicks.onNext(Unit) }
            }
        }

        binding.viewFolder.isVisible = item != null
        binding.viewPlus.isVisible = item == null
    }

}