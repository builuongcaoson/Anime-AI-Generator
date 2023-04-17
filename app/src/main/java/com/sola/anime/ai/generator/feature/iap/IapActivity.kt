package com.sola.anime.ai.generator.feature.iap

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.basic.common.base.LsActivity
import com.basic.common.extension.lightStatusBar
import com.basic.common.extension.transparent
import com.sola.anime.ai.generator.databinding.ActivityIapBinding
import com.sola.anime.ai.generator.feature.iap.adapter.PreviewAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class IapActivity : LsActivity() {

    @Inject lateinit var previewAdapter1: PreviewAdapter
    @Inject lateinit var previewAdapter2: PreviewAdapter
    @Inject lateinit var previewAdapter3: PreviewAdapter

    private val binding by lazy { ActivityIapBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transparent()
        lightStatusBar()
        setContentView(binding.root)

        initView()
        initObservable()
    }

    private fun initObservable() {

    }

    private fun initView() {
        binding.recyclerPreview1.apply {
            this.layoutManager = object: LinearLayoutManager(this@IapActivity, HORIZONTAL, false){
                override fun canScrollHorizontally(): Boolean {
                    return false
                }
            }
            this.adapter = previewAdapter1
        }
        binding.recyclerPreview2.apply {
            this.layoutManager = object: LinearLayoutManager(this@IapActivity, HORIZONTAL, false){
                override fun canScrollHorizontally(): Boolean {
                    return false
                }
            }
            this.adapter = previewAdapter2
        }
        binding.recyclerPreview3.apply {
            this.layoutManager = object: LinearLayoutManager(this@IapActivity, HORIZONTAL, false){
                override fun canScrollHorizontally(): Boolean {
                    return false
                }
            }
            this.adapter = previewAdapter3
        }
    }

}