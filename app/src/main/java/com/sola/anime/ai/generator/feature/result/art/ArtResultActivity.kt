package com.sola.anime.ai.generator.feature.result.art

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.basic.common.base.LsActivity
import com.basic.common.extension.clicks
import com.sola.anime.ai.generator.common.Navigator
import com.sola.anime.ai.generator.common.extension.back
import com.sola.anime.ai.generator.common.extension.startIap
import com.sola.anime.ai.generator.databinding.ActivityArtResultBinding
import com.sola.anime.ai.generator.feature.result.art.adapter.PreviewAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ArtResultActivity : LsActivity() {

    companion object {
        const val HISTORY_ID_EXTRA = "HISTORY_ID_EXTRA"
    }

    @Inject lateinit var previewAdapter: PreviewAdapter

    private val binding by lazy { ActivityArtResultBinding.inflate(layoutInflater) }
    private val historyId by lazy { intent.getLongExtra(HISTORY_ID_EXTRA, -1L) }

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

    }

    private fun initObservable() {

    }

    private fun initView() {
        binding.recyclerPreview.apply {
            this.layoutManager = LinearLayoutManager(this@ArtResultActivity, LinearLayoutManager.HORIZONTAL, false)
            this.adapter = previewAdapter
        }

//        binding.preview.setimage
    }

    @Deprecated("Deprecated in Java", ReplaceWith("finish()"))
    override fun onBackPressed() {
        back()
    }

}