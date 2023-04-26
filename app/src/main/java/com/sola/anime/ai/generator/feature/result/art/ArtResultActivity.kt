package com.sola.anime.ai.generator.feature.result.art

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.basic.common.base.LsActivity
import com.basic.common.extension.clicks
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.common.Navigator
import com.sola.anime.ai.generator.common.extension.back
import com.sola.anime.ai.generator.common.extension.startIap
import com.sola.anime.ai.generator.data.db.query.ChildHistoryDao
import com.sola.anime.ai.generator.data.db.query.HistoryDao
import com.sola.anime.ai.generator.databinding.ActivityArtResultBinding
import com.sola.anime.ai.generator.feature.result.art.adapter.PreviewAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ArtResultActivity : LsActivity() {

    companion object {
        const val HISTORY_ID_EXTRA = "HISTORY_ID_EXTRA"
        const val CHILD_HISTORY_ID_EXTRA = "CHILD_HISTORY_ID_EXTRA"
    }

    @Inject lateinit var previewAdapter: PreviewAdapter
    @Inject lateinit var historyDao: HistoryDao
    @Inject lateinit var childHistoryDao: ChildHistoryDao

    private val binding by lazy { ActivityArtResultBinding.inflate(layoutInflater) }
    private val historyId by lazy { intent.getLongExtra(HISTORY_ID_EXTRA, -1L) }
    private val childHistoryId by lazy { intent.getLongExtra(CHILD_HISTORY_ID_EXTRA, -1L) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        when (historyId) {
            -1L -> {
                finish()
                return
            }
        }

        initView()
        initObservable()
        initData()
        listenerView()
    }

    private fun listenerView() {
        binding.back.clicks { onBackPressed() }
        binding.viewPro.clicks { startIap() }
    }

    private fun initData() {
        childHistoryDao.getAllWithHistoryIdLive(historyId = historyId).observe(this){ childHistories ->
            previewAdapter.data = childHistories

            when {
                childHistoryId ==-1L && childHistories.isNotEmpty() -> {
                    Glide
                        .with(this)
                        .asBitmap()
                        .load(childHistories.firstOrNull())
                        .transition(BitmapTransitionOptions.withCrossFade())
                        .error(R.drawable.place_holder_image)
                        .placeholder(R.drawable.place_holder_image)
                        .into(binding.preview)
                }
            }
        }
    }

    private fun initObservable() {

    }

    private fun initView() {
        binding.recyclerPreview.apply {
            this.layoutManager = LinearLayoutManager(this@ArtResultActivity, LinearLayoutManager.HORIZONTAL, false)
            this.adapter = previewAdapter
        }
    }

    @Deprecated("Deprecated in Java", ReplaceWith("finish()"))
    override fun onBackPressed() {
        back()
    }

}