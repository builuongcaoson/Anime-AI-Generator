package com.sola.anime.ai.generator.common.ui.sheet.upscale

import com.basic.common.extension.clicks
import com.sola.anime.ai.generator.common.base.LsBottomSheet
import com.sola.anime.ai.generator.databinding.SheetUpscaleBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UpscaleSheet: LsBottomSheet<SheetUpscaleBinding>(SheetUpscaleBinding::inflate) {

    override fun onViewCreated() {
        initView()
        initData()
        listenerView()
    }

    private fun listenerView() {
        binding.viewUpscale.clicks(withAnim = false){ }
        binding.viewPremium.clicks(withAnim = false){ }
    }

    override fun onResume() {
        initObservable()
        super.onResume()
    }

    private fun initObservable() {

    }

    private fun initData() {

    }

    private fun initView() {

    }

}