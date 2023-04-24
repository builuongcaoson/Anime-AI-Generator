package com.sola.anime.ai.generator.common.ui.sheet.advanced

import com.sola.anime.ai.generator.common.base.LsBottomSheet
import com.sola.anime.ai.generator.databinding.SheetAdvancedBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AdvancedSheet: LsBottomSheet<SheetAdvancedBinding>(SheetAdvancedBinding::inflate) {

    override fun onViewCreated() {
        initView()
    }

    private fun initView() {

    }

}