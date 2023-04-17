package com.sola.anime.ai.generator.feature.iap

import android.os.Bundle
import com.basic.common.base.LsActivity
import com.basic.common.extension.lightStatusBar
import com.basic.common.extension.transparent
import com.sola.anime.ai.generator.databinding.ActivityIapBinding
import com.sola.anime.ai.generator.feature.iap.adapter.PreviewAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class IapActivity : LsActivity() {

    @Inject lateinit var previewAdapter: PreviewAdapter

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

    }

}