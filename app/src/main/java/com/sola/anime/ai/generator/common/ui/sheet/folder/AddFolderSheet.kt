package com.sola.anime.ai.generator.common.ui.sheet.folder

import com.sola.anime.ai.generator.common.base.LsBottomSheet
import com.sola.anime.ai.generator.databinding.SheetAddFolderBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddFolderSheet: LsBottomSheet<SheetAddFolderBinding>(SheetAddFolderBinding::inflate) {

    override fun onViewCreated() {
        initView()
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

    private fun initView(){

    }

}