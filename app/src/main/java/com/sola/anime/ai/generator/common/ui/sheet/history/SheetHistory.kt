package com.sola.anime.ai.generator.common.ui.sheet.history

import androidx.recyclerview.widget.LinearLayoutManager
import com.sola.anime.ai.generator.common.base.LsBottomSheet
import com.sola.anime.ai.generator.common.ui.sheet.history.adapter.PromptAdapter
import com.sola.anime.ai.generator.data.db.query.HistoryDao
import com.sola.anime.ai.generator.databinding.SheetHistoryBinding
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SheetHistory: LsBottomSheet<SheetHistoryBinding>(SheetHistoryBinding::inflate) {

    @Inject lateinit var promptAdapter: PromptAdapter
    @Inject lateinit var historyDao: HistoryDao

    override fun onViewCreated() {
        initView()
        initData()
    }

    override fun onResume() {
        initObservable()
        super.onResume()
    }

    private fun initObservable() {
        promptAdapter
            .closeClicks
            .autoDispose(scope())
            .subscribe { history ->
                history.isShowPromptHistory = false

                historyDao.update(history)
            }
    }

    private fun initData() {
        historyDao.getAllPromptHistoryLive().observe(viewLifecycleOwner){ history ->
            history?.let {
                promptAdapter.data = history
            }
        }
    }

    private fun initView() {
        activity?.let { activity ->
            binding.recyclerView.apply {
                this.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
                this.adapter = promptAdapter.apply {
                    this.emptyView = binding.viewEmpty
                }
            }
        }
    }

}