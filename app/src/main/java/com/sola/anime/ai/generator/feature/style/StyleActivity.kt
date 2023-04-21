package com.sola.anime.ai.generator.feature.style

import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.basic.common.base.LsActivity
import com.basic.common.extension.clicks
import com.sola.anime.ai.generator.common.ConfigApp
import com.sola.anime.ai.generator.common.extension.back
import com.sola.anime.ai.generator.data.db.query.StyleDao
import com.sola.anime.ai.generator.databinding.ActivityStyleBinding
import com.sola.anime.ai.generator.feature.style.adapter.PreviewAdapter
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class StyleActivity : LsActivity() {

    @Inject lateinit var previewAdapter: PreviewAdapter
    @Inject lateinit var styleDao: StyleDao
    @Inject lateinit var configApp: ConfigApp

    private val binding by lazy { ActivityStyleBinding.inflate(layoutInflater) }

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
        binding.recyclerView.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val alpha = dy.toFloat() / binding.viewShadow.height.toFloat()

                binding.viewShadow.alpha = alpha
            }
        })
    }

    private fun initData() {
        styleDao.getAllLive().observe(this){
            previewAdapter.data = it
        }
    }

    private fun initObservable() {
        previewAdapter
            .clicks
            .autoDispose(scope())
            .subscribe {
                configApp.subjectStyleClicks.onNext(it.id)

                previewAdapter.style = it
                back()
            }

        configApp
            .subjectStyleClicks
            .take(1)
            .map { styleDao.findById(it) }
            .autoDispose(scope())
            .subscribe {
                Timber.e("Style id: ${it?.id}")

                previewAdapter.style = it
            }
    }

    private fun initView() {
        binding.recyclerView.apply {
            this.layoutManager = GridLayoutManager(this@StyleActivity, 3, GridLayoutManager.VERTICAL, false)
            this.adapter = previewAdapter
        }
    }

    override fun onBackPressed() {
        back()
    }

}