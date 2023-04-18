package com.sola.anime.ai.generator.feature.processing.art

import android.os.Bundle
import com.basic.common.base.LsActivity
import com.sola.anime.ai.generator.databinding.ActivityArtProcessingBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ArtProcessingActivity : LsActivity() {

    private val binding by lazy { ActivityArtProcessingBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initView()
        initObservable()
        initData()
    }

    private fun initData() {

    }

    private fun initObservable() {

    }

    private fun initView() {

    }

}