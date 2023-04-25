package com.sola.anime.ai.generator.feature.main.mine.adapter

import android.view.ViewGroup
import com.basic.common.base.LsAdapter
import com.basic.common.base.LsViewHolder
import com.sola.anime.ai.generator.databinding.ItemHistoryMineBinding
import com.sola.anime.ai.generator.domain.model.history.History
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

class HistoryAdapter @Inject constructor(): LsAdapter<History, ItemHistoryMineBinding>() {

    val clicks: Subject<History> = PublishSubject.create()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LsViewHolder<ItemHistoryMineBinding> {
        return LsViewHolder(parent, ItemHistoryMineBinding::inflate)
    }

    override fun onBindViewHolder(holder: LsViewHolder<ItemHistoryMineBinding>, position: Int) {
        val item = getItem(position)
        val binding = holder.binding
        val context = binding.root.context


    }

}