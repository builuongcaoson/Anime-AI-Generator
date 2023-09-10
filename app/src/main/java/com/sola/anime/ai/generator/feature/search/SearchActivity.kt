package com.sola.anime.ai.generator.feature.search

import android.os.Bundle
import com.basic.common.base.LsActivity
import com.jakewharton.rxbinding2.widget.textChanges
import com.sola.anime.ai.generator.common.extension.back
import com.sola.anime.ai.generator.data.db.query.ExploreDao
import com.sola.anime.ai.generator.data.db.query.LoRAGroupDao
import com.sola.anime.ai.generator.data.db.query.ModelDao
import com.sola.anime.ai.generator.databinding.ActivitySearchBinding
import com.sola.anime.ai.generator.feature.search.adapter.GroupSearchAdapter
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import dagger.hilt.android.AndroidEntryPoint
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
            .autoDispose(scope())
            .subscribe { text ->

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