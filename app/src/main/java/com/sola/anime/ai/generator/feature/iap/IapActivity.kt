package com.sola.anime.ai.generator.feature.iap

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.basic.common.base.LsActivity
import com.basic.common.extension.clicks
import com.basic.common.extension.transparent
import com.basic.common.extension.tryOrNull
import com.sola.anime.ai.generator.common.ConfigApp
import com.sola.anime.ai.generator.common.extension.back
import com.sola.anime.ai.generator.common.extension.backTopToBottom
import com.sola.anime.ai.generator.common.util.AutoScrollHorizontalLayoutManager
import com.sola.anime.ai.generator.data.db.query.IapPreviewDao
import com.sola.anime.ai.generator.databinding.ActivityIapBinding
import com.sola.anime.ai.generator.feature.iap.adapter.PreviewAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class IapActivity : LsActivity() {

    @Inject lateinit var previewAdapter1: PreviewAdapter
    @Inject lateinit var previewAdapter2: PreviewAdapter
    @Inject lateinit var previewAdapter3: PreviewAdapter
    @Inject lateinit var iapPreviewDao: IapPreviewDao

    private val binding by lazy { ActivityIapBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transparent()
        setContentView(binding.root)

        initView()
        initObservable()
        initData()
        listenerView()
    }

    private fun initData() {
        iapPreviewDao.getAllLive().observe(this){ data ->
            val dataAfterChunked = data.chunked(10)

            previewAdapter1.let { adapter ->
                adapter.data = dataAfterChunked.getOrNull(0) ?: listOf()
                adapter.totalCount = adapter.data.size
                binding.recyclerPreview1.apply {
                    this.post { this.smoothScrollToPosition(adapter.data.size - 1) }
                }
            }
            previewAdapter2.let { adapter ->
                adapter.data = dataAfterChunked.getOrNull(1) ?: listOf()
                adapter.totalCount = adapter.data.size
                binding.recyclerPreview2.apply {
                    this.post { this.smoothScrollToPosition(adapter.data.size - 1) }
                }
            }
            previewAdapter3.let { adapter ->
                adapter.data = dataAfterChunked.getOrNull(2) ?: listOf()
                adapter.totalCount = adapter.data.size
                binding.recyclerPreview3.apply {
                    this.post { this.smoothScrollToPosition(adapter.data.size - 1) }
                }
            }
        }
    }

    private fun listenerView() {
        binding.back.clicks { onBackPressed() }
    }

    override fun onResume() {
        registerScrollListener()
        super.onResume()
    }

    override fun onDestroy() {
        unregisterScrollListener()
        super.onDestroy()
    }

    private fun registerScrollListener(){
        binding.recyclerPreview1.addOnScrollListener(scrollListener)
        binding.recyclerPreview2.addOnScrollListener(scrollListener)
        binding.recyclerPreview3.addOnScrollListener(scrollListener)
    }

    private fun unregisterScrollListener(){
        binding.recyclerPreview1.removeOnScrollListener(scrollListener)
        binding.recyclerPreview2.removeOnScrollListener(scrollListener)
        binding.recyclerPreview3.removeOnScrollListener(scrollListener)
    }

    private val scrollListener = object: RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            tryOrNull {
                val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return@tryOrNull

                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val pastVisibleItems = layoutManager.findFirstVisibleItemPosition()

                tryOrNull {
                    if (pastVisibleItems + visibleItemCount >= totalItemCount) {
                        when (recyclerView) {
                            binding.recyclerPreview1 -> {
                                tryOrNull { recyclerView.post { previewAdapter1.insert() } }
                            }
                            binding.recyclerPreview2 -> {
                                tryOrNull { recyclerView.post { previewAdapter2.insert() } }
                            }
                            binding.recyclerPreview3 -> {
                                tryOrNull { recyclerView.post { previewAdapter3.insert() } }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun initObservable() {

    }

    private fun initView() {
        binding.recyclerPreview1.apply {
            this.layoutManager = AutoScrollHorizontalLayoutManager(this@IapActivity)
            this.adapter = previewAdapter1
        }
        binding.recyclerPreview2.apply {
            this.layoutManager =  AutoScrollHorizontalLayoutManager(this@IapActivity).apply {
                this.reverseLayout = true
            }
            this.adapter = previewAdapter2
        }
        binding.recyclerPreview3.apply {
            this.layoutManager =  AutoScrollHorizontalLayoutManager(this@IapActivity)
            this.adapter = previewAdapter3
        }
    }

    @Deprecated("Deprecated in Java", ReplaceWith("finish()"))
    override fun onBackPressed() {
        backTopToBottom()
    }

}