package com.sola.anime.ai.generator.feature.main.discover

import android.view.LayoutInflater
import android.view.ViewGroup
import com.basic.common.base.LsFragment
import com.sola.anime.ai.generator.databinding.FragmentDiscoverBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DiscoverFragment : LsFragment<FragmentDiscoverBinding>() {

    override fun initViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentDiscoverBinding {
        return FragmentDiscoverBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated() {

    }

}