package com.sola.anime.ai.generator.common.ui.sheet.history

import androidx.recyclerview.widget.LinearLayoutManager
import com.basic.common.base.LsBottomSheet
import com.sola.anime.ai.generator.common.ui.sheet.history.adapter.PromptAdapter
import com.sola.anime.ai.generator.databinding.FragmentArtBinding
import com.sola.anime.ai.generator.databinding.SheetHistoryBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HistorySheet: LsBottomSheet<SheetHistoryBinding>(SheetHistoryBinding::inflate) {

    @Inject lateinit var promptAdapter: PromptAdapter

    override fun onViewCreated() {
        initView()
    }

    private fun initView() {
        activity?.let { activity ->
            binding.recyclerView.apply {
                this.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
                this.adapter = promptAdapter
            }
        }
    }

}