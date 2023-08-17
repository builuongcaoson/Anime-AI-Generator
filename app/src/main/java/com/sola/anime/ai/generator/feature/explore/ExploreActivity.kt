package com.sola.anime.ai.generator.feature.explore

import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.basic.common.base.LsActivity
import com.basic.common.extension.clicks
import com.sola.anime.ai.generator.common.ConfigApp
import com.sola.anime.ai.generator.common.extension.back
import com.sola.anime.ai.generator.common.extension.startIap
import com.sola.anime.ai.generator.common.ui.dialog.ExploreDialog
import com.sola.anime.ai.generator.data.db.query.ExploreDao
import com.sola.anime.ai.generator.databinding.ActivityExploreBinding
import com.sola.anime.ai.generator.domain.model.config.explore.Explore
import com.sola.anime.ai.generator.feature.explore.adapter.ExploreAdapter
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

@AndroidEntryPoint
class ExploreActivity : LsActivity<ActivityExploreBinding>(ActivityExploreBinding::inflate) {

    @Inject lateinit var exploreAdapter: ExploreAdapter
    @Inject lateinit var configApp: ConfigApp
    @Inject lateinit var exploreDao: ExploreDao
    @Inject lateinit var exploreDialog: ExploreDialog

    private val subjectUseClicks: Subject<Explore> = PublishSubject.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initView()
        initObservable()
        initData()
        listenerView()
    }

    private fun listenerView() {
        binding.back.clicks { onBackPressed() }
        binding.viewPro.clicks { startIap() }
        binding.recyclerView.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val alpha = dy.toFloat() / binding.viewShadow.height.toFloat()

                binding.viewShadow.alpha = alpha
            }
        })
    }

    private fun initData() {
        exploreDao.getAllLive().observe(this){
            exploreAdapter.data = it
        }
    }

    private fun initObservable() {
        exploreAdapter
            .clicks
            .autoDispose(scope())
            .subscribe { exploreDialog.show(activity = this, explore = it, useClicks = subjectUseClicks) }

        subjectUseClicks
            .autoDispose(scope())
            .subscribe {
                exploreDialog.dismiss()

                configApp.subjectExploreClicks.onNext(it.id)

                back()
            }
    }

    private fun initView() {
        binding.recyclerView.apply {
            val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL).apply {
                
            }
            this.layoutManager = layoutManager
            this.adapter = exploreAdapter
        }
    }

    @Deprecated("Deprecated in Java", ReplaceWith("finish()"))
    override fun onBackPressed() {
        back()
    }

}