package com.sola.anime.ai.generator.feature.main.mine.feature

import android.annotation.SuppressLint
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.basic.common.base.LsFragment
import com.basic.common.extension.clicks
import com.sola.anime.ai.generator.common.extension.show
import com.sola.anime.ai.generator.common.extension.startArt
import com.sola.anime.ai.generator.common.extension.startArtResult
import com.sola.anime.ai.generator.common.extension.startDetailExplore
import com.sola.anime.ai.generator.common.extension.startIap
import com.sola.anime.ai.generator.common.ui.dialog.ExploreDialog
import com.sola.anime.ai.generator.common.ui.sheet.explore.SheetExplore
import com.sola.anime.ai.generator.common.ui.sheet.folder.AddFolderSheet
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.data.db.query.ExploreDao
import com.sola.anime.ai.generator.data.db.query.FolderDao
import com.sola.anime.ai.generator.data.db.query.HistoryDao
import com.sola.anime.ai.generator.databinding.FragmentArtMineBinding
import com.sola.anime.ai.generator.domain.model.config.explore.Explore
import com.sola.anime.ai.generator.feature.main.mine.adapter.FolderAdapter
import com.sola.anime.ai.generator.feature.main.mine.adapter.HistoryAdapter
import com.trello.rxlifecycle2.kotlin.bindToLifecycle
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

@AndroidEntryPoint
class ArtFragment : LsFragment<FragmentArtMineBinding>(FragmentArtMineBinding::inflate) {

    @Inject lateinit var folderAdapter: FolderAdapter
    @Inject lateinit var historyAdapter: HistoryAdapter
    @Inject lateinit var historyDao: HistoryDao
    @Inject lateinit var folderDao: FolderDao
    @Inject lateinit var prefs: Preferences
    @Inject lateinit var exploreDialog: ExploreDialog
    @Inject lateinit var exploreDao: ExploreDao

    private val useExploreClicks: Subject<Explore> = PublishSubject.create()
    private val detailExploreClicks: Subject<Explore> = PublishSubject.create()

    private val addFolderSheet by lazy { AddFolderSheet() }
    private val sheetExplore by lazy { SheetExplore() }

    override fun onViewCreated() {
        initView()
        initData()
        initObservable()
        listenerView()
    }

    private fun listenerView() {
        binding.viewExplore.clicks {
            sheetExplore.clicks = { explore ->
                activity?.let { activity ->
                    exploreDialog.show(activity, explore, useExploreClicks, detailExploreClicks)
                }
            }
            sheetExplore.detailsClicks = { explore ->
                activity?.startDetailExplore(exploreId = explore.id)
            }
            sheetExplore.show(this)
        }
    }

    @SuppressLint("AutoDispose", "CheckResult")
    private fun initObservable() {
        useExploreClicks
            .bindToLifecycle(binding.root)
            .subscribe { explore ->
                exploreDialog.dismiss()

                activity?.startArt(exploreId = explore.id)
            }

        detailExploreClicks
            .bindToLifecycle(binding.root)
            .subscribe { explore ->
                activity?.startDetailExplore(exploreId = explore.id)
            }

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
    }

    private fun initData() {
        historyDao.getAllLive().observe(viewLifecycleOwner){ histories ->
            val historiesWithType = histories.filter { history -> history.childs.any { it.type == 0 } }

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

        exploreDao.getAllLive().observe(viewLifecycleOwner) { explores ->
            val pairExploresFavourite = "Favourite" to explores.filter { it.isFavourite }
            val pairExploresOther = "Other" to explores.filter { !it.isFavourite && !it.isDislike }
            val pairExploresDislike = "Dislike" to explores.filter { it.isDislike }

            sheetExplore.pairs = listOf(pairExploresFavourite, pairExploresOther, pairExploresDislike)
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