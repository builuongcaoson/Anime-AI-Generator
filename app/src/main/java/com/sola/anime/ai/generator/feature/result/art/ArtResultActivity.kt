package com.sola.anime.ai.generator.feature.result.art

import android.graphics.Bitmap
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.basic.common.base.LsActivity
import com.basic.common.extension.clicks
import com.basic.common.extension.getDimens
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.common.extension.back
import com.sola.anime.ai.generator.common.extension.startIap
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
        historyDao.getWithIdLive(id = historyId).observe(this){ history ->
            history?.let {
                previewAdapter.data = history.childs

                when {
                    childHistoryId ==-1L && history.childs.isNotEmpty() -> {
                        Glide
                            .with(this)
                            .asBitmap()
                            .load(history.childs.lastOrNull()?.pathPreview)
                            .transition(BitmapTransitionOptions.withCrossFade())
                            .error(R.drawable.place_holder_image)
                            .placeholder(R.drawable.place_holder_image)
                            .listener(object: RequestListener<Bitmap>{
                                override fun onLoadFailed(
                                    e: GlideException?,
                                    model: Any?,
                                    target: Target<Bitmap>?,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    binding.cardPreview.cardElevation = 0f
                                    binding.preview.setImageResource(R.drawable.place_holder_image)
                                    return false
                                }

                                override fun onResourceReady(
                                    resource: Bitmap?,
                                    model: Any?,
                                    target: Target<Bitmap>?,
                                    dataSource: DataSource?,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    resource?.let { bitmap ->
                                        binding.cardPreview.cardElevation = getDimens(com.intuit.sdp.R.dimen._3sdp)
                                        binding.preview.setImageBitmap(bitmap)
                                    } ?: run {
                                        binding.cardPreview.cardElevation = 0f
                                        binding.preview.setImageResource(R.drawable.place_holder_image)
                                    }
                                    return false
                                }
                            })
                            .preload()
                    }
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