package com.sola.anime.ai.generator.feature.main.mine.adapter

import android.view.ViewGroup
import androidx.core.view.isVisible
import com.basic.common.base.LsAdapter
import com.basic.common.base.LsViewHolder
import com.basic.common.extension.clicks
import com.basic.common.util.theme.TextViewStyler.Companion.FONT_REGULAR
import com.basic.common.util.theme.TextViewStyler.Companion.FONT_SEMI
import com.sola.anime.ai.generator.databinding.ItemFolderMineBinding
import com.sola.anime.ai.generator.domain.model.Folder
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

class FolderAdapter @Inject constructor(): LsAdapter<Folder?, ItemFolderMineBinding>() {

    init {
        data = listOf(
            Folder(display = "Characters"),
            null
        )
    }

    val clicks: Subject<Folder> = PublishSubject.create()
    var folder = data.first()
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
    ): LsViewHolder<ItemFolderMineBinding> {
        return LsViewHolder(parent, ItemFolderMineBinding::inflate)
    }

    override fun onBindViewHolder(holder: LsViewHolder<ItemFolderMineBinding>, position: Int) {
        val item = getItem(position)
        val binding = holder.binding
        val context = binding.root.context

        when {
            item != null -> {
                binding.display.text = item.display

                binding.viewFolder.clicks { clicks.onNext(item) }
            }
            else -> {
                binding.viewPlus.clicks {  }
            }
        }

        binding.viewFolder.isVisible = item != null
        binding.viewPlus.isVisible = item == null
    }

}