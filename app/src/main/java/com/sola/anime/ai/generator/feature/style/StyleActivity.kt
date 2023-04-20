package com.sola.anime.ai.generator.feature.style

import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.basic.common.base.LsActivity
import com.basic.common.extension.clicks
import com.sola.anime.ai.generator.common.extension.back
import com.sola.anime.ai.generator.data.db.query.StyleDao
import com.sola.anime.ai.generator.databinding.ActivityStyleBinding
import com.sola.anime.ai.generator.feature.style.adapter.PreviewAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class StyleActivity : LsActivity() {

    @Inject lateinit var previewAdapter: PreviewAdapter
    @Inject lateinit var styleDao: StyleDao

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