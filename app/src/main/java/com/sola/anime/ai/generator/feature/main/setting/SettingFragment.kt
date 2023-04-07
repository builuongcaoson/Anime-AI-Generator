package com.sola.anime.ai.generator.feature.main.setting

import android.view.LayoutInflater
import android.view.ViewGroup
import com.basic.common.base.LsFragment
import com.sola.anime.ai.generator.databinding.FragmentSettingBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingFragment : LsFragment<FragmentSettingBinding>() {

    override fun initViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSettingBinding {
        return FragmentSettingBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated() {

    }

}