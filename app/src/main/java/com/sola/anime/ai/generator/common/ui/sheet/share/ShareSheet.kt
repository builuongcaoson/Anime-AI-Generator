package com.sola.anime.ai.generator.common.ui.sheet.share

import com.sola.anime.ai.generator.common.base.LsBottomSheet
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.databinding.SheetUpscaleBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ShareSheet: LsBottomSheet<SheetUpscaleBinding>(SheetUpscaleBinding::inflate) {

    @Inject lateinit var prefs: Preferences

    override fun onViewCreated() {
        initView()
        initData()
        listenerView()
    }

    private fun listenerView() {

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