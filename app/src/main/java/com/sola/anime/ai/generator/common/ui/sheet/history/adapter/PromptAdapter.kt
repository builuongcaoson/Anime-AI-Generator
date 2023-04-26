package com.sola.anime.ai.generator.common.ui.sheet.history.adapter

import android.view.ViewGroup
import com.basic.common.base.LsAdapter
import com.basic.common.base.LsViewHolder
import com.basic.common.extension.clicks
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.sola.anime.ai.generator.common.extension.copyToClipboard
import com.sola.anime.ai.generator.databinding.ItemPreviewArtBinding
import com.sola.anime.ai.generator.databinding.ItemPromptHistoryBinding
import com.sola.anime.ai.generator.domain.model.config.explore.Explore
import com.sola.anime.ai.generator.domain.model.history.History
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

class PromptAdapter @Inject constructor(): LsAdapter<History, ItemPromptHistoryBinding>() {

    val closeClicks: Subject<History> = PublishSubject.create()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LsViewHolder<ItemPromptHistoryBinding> {
        return LsViewHolder(parent, ItemPromptHistoryBinding::inflate)
    }

    override fun onBindViewHolder(holder: LsViewHolder<ItemPromptHistoryBinding>, position: Int) {
        val item = getItem(position)
        val binding = holder.binding
        val context = binding.root.context

        binding.prompt.text = item.childs.lastOrNull()?.prompt ?: ""

        binding.close.clicks { closeClicks.onNext(item) }
        binding.copy.clicks { item.childs.lastOrNull()?.prompt?.copyToClipboard(context) }
    }

}