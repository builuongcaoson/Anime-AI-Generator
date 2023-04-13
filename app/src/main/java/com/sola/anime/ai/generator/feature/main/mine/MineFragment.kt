package com.sola.anime.ai.generator.feature.main.mine

import android.view.LayoutInflater
import android.view.ViewGroup
import com.basic.common.base.LsFragment
import com.sola.anime.ai.generator.databinding.FragmentMineBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MineFragment : LsFragment<FragmentMineBinding>() {

    override fun initViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentMineBinding {
        return FragmentMineBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated() {

    }

}