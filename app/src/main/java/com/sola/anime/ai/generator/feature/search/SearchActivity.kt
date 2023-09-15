package com.sola.anime.ai.generator.feature.search

import android.os.Bundle
import com.basic.common.base.LsActivity
import com.jakewharton.rxbinding2.widget.textChanges
import com.sola.anime.ai.generator.common.extension.back
import com.sola.anime.ai.generator.common.extension.startDetailExplore
import com.sola.anime.ai.generator.common.extension.startDetailModelOrLoRA
import com.sola.anime.ai.generator.data.db.query.ExploreDao
import com.sola.anime.ai.generator.data.db.query.LoRAGroupDao
import com.sola.anime.ai.generator.data.db.query.ModelDao
import com.sola.anime.ai.generator.databinding.ActivitySearchBinding
import com.sola.anime.ai.generator.domain.model.LoRAPreview
import com.sola.anime.ai.generator.feature.search.adapter.GroupSearch
import com.sola.anime.ai.generator.feature.search.adapter.GroupSearchAdapter
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class SearchActivity : LsActivity<ActivitySearchBinding>(ActivitySearchBinding::inflate) {

    @Inject lateinit var groupSearchAdapter: GroupSearchAdapter
    @Inject lateinit var modelDao: ModelDao
    @Inject lateinit var loRAGroupDao: LoRAGroupDao
    @Inject lateinit var exploreDao: ExploreDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initView()
        initObservable()
        initData()
        listenerView()
    }

    private fun listenerView() {

    }

    private fun initData() {

    }

    private fun initObservable() {
        binding.search
            .textChanges()
            .debounce(250L, TimeUnit.MILLISECONDS)
            .map { it.toString() }
            .map { text ->
                val models = when {
                    text.isEmpty() -> listOf()
                    else -> modelDao.getAll().filter { model -> model.display.lowercase().contains(text.lowercase()) }
                }
                val loRAs = when {
                    text.isEmpty() -> listOf()
                    else -> loRAGroupDao.getAll().flatMap { loRAGroup -> loRAGroup.childs.map { loRA -> LoRAPreview(loRA, loRAGroup.id, 0.7f) } }.filter { loRAPreview -> loRAPreview.loRA.display.lowercase().contains(text.lowercase()) }
                }
                val explores = when {
                    text.isEmpty() -> listOf()
                    else -> exploreDao.getAll().filter { explore -> explore.prompt.lowercase().contains(text.lowercase()) }
                }

                Triple(models, loRAs, explores)
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .autoDispose(scope())
            .subscribe { triple ->
                val data = arrayListOf<GroupSearch>()

                when {
                    triple.first.isNotEmpty() -> data.add(GroupSearch.Models(triple.first))
                }

                when {
                    triple.second.isNotEmpty() -> data.add(GroupSearch.LoRAs(triple.second))
                }

                when {
                    triple.third.isNotEmpty() -> data.add(GroupSearch.Explores(triple.third))
                }

                groupSearchAdapter.data = data
            }

        groupSearchAdapter
            .modelClicks
            .autoDispose(scope())
            .subscribe { model ->
                startDetailModelOrLoRA(modelId = model.id)
            }

        groupSearchAdapter
            .loRAClicks
            .autoDispose(scope())
            .subscribe { loRAPreview ->
                startDetailModelOrLoRA(loRAId = loRAPreview.loRA.id, loRAGroupId = loRAPreview.loRAGroupId)
            }

        groupSearchAdapter
            .exploreCLicks
            .autoDispose(scope())
            .subscribe { explore ->
                startDetailExplore(exploreId = explore.id)
            }
    }

    private fun initView() {
        binding.recyclerSearch.apply {
            this.adapter = groupSearchAdapter
        }
    }

    @Deprecated("Deprecated in Java", ReplaceWith("finish()"))
    override fun onBackPressed() {
        back()
    }

}