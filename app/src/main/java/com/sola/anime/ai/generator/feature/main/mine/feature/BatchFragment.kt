package com.sola.anime.ai.generator.feature.main.mine.feature

import android.annotation.SuppressLint
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.basic.common.base.LsFragment
import com.basic.common.extension.clicks
import com.basic.common.extension.tryOrNull
import com.sola.anime.ai.generator.common.extension.show
import com.sola.anime.ai.generator.common.extension.startArtResult
import com.sola.anime.ai.generator.common.ui.sheet.folder.AddFolderSheet
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.data.db.query.FolderDao
import com.sola.anime.ai.generator.data.db.query.HistoryDao
import com.sola.anime.ai.generator.databinding.FragmentBatchMineBinding
import com.sola.anime.ai.generator.feature.main.MainActivity
import com.sola.anime.ai.generator.feature.main.mine.adapter.FolderAdapter
import com.sola.anime.ai.generator.feature.main.mine.adapter.HistoryAdapter
import com.trello.rxlifecycle2.kotlin.bindToLifecycle
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BatchFragment : LsFragment<FragmentBatchMineBinding>(FragmentBatchMineBinding::inflate) {

    @Inject lateinit var folderAdapter: FolderAdapter
    @Inject lateinit var historyAdapter: HistoryAdapter
    @Inject lateinit var historyDao: HistoryDao
    @Inject lateinit var folderDao: FolderDao
    @Inject lateinit var prefs: Preferences

    private val addFolderSheet by lazy { AddFolderSheet() }

    override fun onViewCreated() {
        initView()
        initObservable()
        initData()
        listenerView()
    }

    private fun listenerView() {
        binding.viewExplore.clicks { (activity as? MainActivity)?.binding?.viewPager?.currentItem = 1 }
    }

    @SuppressLint("AutoDispose", "CheckResult")
    private fun initObservable() {
        historyAdapter
            .clicks
            .bindToLifecycle(binding.root)
            .subscribe { activity?.startArtResult(historyId = it.id, childHistoryIndex = it.childs.lastIndex, isGallery = true) }

        folderAdapter
            .plusClicks
            .bindToLifecycle(binding.root)
            .subscribe {
                if (addFolderSheet.isAdded){
                    return@subscribe
                }

                addFolderSheet.show(this)
            }

        historyAdapter
            .longClicks
            .bindToLifecycle(binding.root)
            .subscribe { history ->
                activity?.let { activity ->
                    MaterialDialog(activity)
                        .show {
                            title(text = "Delete artworks?")
                            message(text = "Are you sure you want to delete artworks? You can't undo this action.")
                            positiveButton(text = "Delete") { dialog ->
                                dialog.dismiss()

                                tryOrNull { historyDao.delete(history) }
                            }
                            negativeButton(text = "Cancel") { dialog ->
                                dialog.dismiss()
                            }
                        }
                }
            }
    }

    private fun initData() {
        historyDao.getAllLive().observe(viewLifecycleOwner){ histories ->
            val historiesWithType = histories.filter { history -> history.childs.any { it.type == 1 } }

            historyAdapter.data = historiesWithType
        }

        folderDao.getAllLive().observe(viewLifecycleOwner){ folders ->
            if (folders.isEmpty()){
                return@observe
            }

            folderAdapter.data = ArrayList(folders).apply {
                add(null)
            }
            folderAdapter.folder = folders.first()
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
//                    override fun canScrollVertically(): Boolean {
//                        return false
//                    }
                }
                this.adapter = historyAdapter.apply {
                    this.emptyView = binding.viewEmpty
                }
            }
        }
    }

}