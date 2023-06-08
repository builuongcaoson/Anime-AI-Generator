package com.sola.anime.ai.generator.common.ui.sheet.history.adapter

import android.view.ViewGroup
import com.basic.common.base.LsAdapter
import com.basic.common.extension.clicks
import com.sola.anime.ai.generator.common.extension.copyToClipboard
import com.sola.anime.ai.generator.databinding.ItemPromptHistoryBinding
import com.sola.anime.ai.generator.domain.model.history.History
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

class PromptAdapter @Inject constructor(): LsAdapter<History, ItemPromptHistoryBinding>(ItemPromptHistoryBinding::inflate) {

    val closeClicks: Subject<History> = PublishSubject.create()

    override fun bindItem(item: History, binding: ItemPromptHistoryBinding, position: Int) {
        val context = binding.root.context

        binding.prompt.text = item.childs.lastOrNull()?.prompt ?: ""

        binding.close.clicks { closeClicks.onNext(item) }
        binding.copy.clicks { item.childs.lastOrNull()?.prompt?.copyToClipboard(context) }
    }

}