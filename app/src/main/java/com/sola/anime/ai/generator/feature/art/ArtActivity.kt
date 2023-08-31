package com.sola.anime.ai.generator.feature.art

import android.os.Bundle
import com.basic.common.base.LsActivity
import com.basic.common.base.LsPageAdapter
import com.basic.common.extension.clicks
import com.sola.anime.ai.generator.common.extension.back
import com.sola.anime.ai.generator.common.extension.backTopToBottom
import com.sola.anime.ai.generator.databinding.ActivityArtBinding
import com.sola.anime.ai.generator.feature.art.art.ArtFragment
import com.sola.anime.ai.generator.feature.art.comingsoon.ComingSoonFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ArtActivity : LsActivity<ActivityArtBinding>(ActivityArtBinding::inflate) {

    private val artFragment by lazy { ArtFragment() }
    private val comingSoonFragment by lazy { ComingSoonFragment() }

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
    }

    private fun initData() {

    }

    private fun initObservable() {

    }

    private fun initView() {
        binding.viewPager.apply {
            this.adapter = LsPageAdapter(supportFragmentManager).apply {
                this.addFragment(fragment = artFragment, title = "Generate Image")
                this.addFragment(fragment = comingSoonFragment, title = "Coming Soon")
            }
            this.offscreenPageLimit = this.adapter?.count ?: 1
        }
        binding.tabLayout.apply {
            this.setupWithViewPager(binding.viewPager)
        }
    }

    @Deprecated("Deprecated in Java", ReplaceWith("finish()"))
    override fun onBackPressed() {
        backTopToBottom()
    }

}