package com.sola.anime.ai.generator.feature.main.mine.feature

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.basic.common.base.LsFragment
import com.sola.anime.ai.generator.common.extension.startArtResult
import com.sola.anime.ai.generator.data.db.query.HistoryDao
import com.sola.anime.ai.generator.databinding.FragmentArtMineBinding
import com.sola.anime.ai.generator.feature.main.mine.adapter.FolderAdapter
import com.sola.anime.ai.generator.feature.main.mine.adapter.HistoryAdapter
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ArtFragment : LsFragment<FragmentArtMineBinding>(FragmentArtMineBinding::inflate) {

    @Inject lateinit var folderAdapter: FolderAdapter
    @Inject lateinit var historyAdapter: HistoryAdapter
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
        historyAdapter
            .clicks
            .autoDispose(scope())
            .subscribe { activity?.startArtResult(historyId = it.id) }
    }

    private fun initData() {
        historyDao.getAllLive().observe(viewLifecycleOwner){ histories ->
            historyAdapter.data = histories
        }
    }

    private fun initView() {
        activity?.let { activity ->
            binding.recyclerFolder.apply {
                this.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
                this.adapter = folderAdapter
            }
            binding.recyclerHistory.apply {
                this.layoutManager = object: StaggeredGridLayoutManager(2, VERTICAL){
                    override fun canScrollVertically(): Boolean {
                        return false
                    }
                }
                this.adapter = historyAdapter.apply {
                    this.emptyView = binding.viewEmpty
                }
            }
        }
    }

}