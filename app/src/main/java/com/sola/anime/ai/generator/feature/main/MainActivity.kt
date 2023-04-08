package com.sola.anime.ai.generator.feature.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.basic.common.base.LsActivity
import com.basic.common.base.LsPageAdapter
import com.basic.common.extension.lightStatusBar
import com.basic.common.extension.transparent
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.databinding.ActivityMainBinding
import com.sola.anime.ai.generator.feature.main.image.ImageFragment
import com.sola.anime.ai.generator.feature.main.setting.SettingFragment
import com.sola.anime.ai.generator.feature.main.text.TextFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : LsActivity() {

    private val fragments by lazy {
        listOf(
            TextFragment(),
            ImageFragment(),
            SettingFragment()
        )
    }

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transparent()
        lightStatusBar()
        setContentView(binding.root)

        initView()
    }

    private fun initView() {
        binding.viewPager.apply {
            this.adapter = LsPageAdapter(supportFragmentManager).apply {
                this.addFragment(fragments = fragments.toTypedArray())
            }
            this.offscreenPageLimit = this.adapter?.count ?: 0
        }
    }

}