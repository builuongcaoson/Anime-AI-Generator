package com.sola.anime.ai.generator.feature.art

import android.os.Bundle
import com.basic.common.base.LsActivity
import com.sola.anime.ai.generator.common.extension.back
import com.sola.anime.ai.generator.common.extension.backTopToBottom
import com.sola.anime.ai.generator.databinding.ActivityArtBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ArtActivity : LsActivity<ActivityArtBinding>(ActivityArtBinding::inflate) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initView()
        initObservable()
        initData()
        listenerView()
    }

    private fun listenerView() {

    }

    private fun initData() {

    }

    private fun initObservable() {

    }

    private fun initView() {

    }

    @Deprecated("Deprecated in Java", ReplaceWith("finish()"))
    override fun onBackPressed() {
        backTopToBottom()
    }

}