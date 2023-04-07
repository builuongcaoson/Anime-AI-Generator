package com.sola.anime.ai.generator.feature.main.image

import android.view.LayoutInflater
import android.view.ViewGroup
import com.basic.common.base.LsFragment
import com.sola.anime.ai.generator.databinding.FragmentImageBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ImageFragment : LsFragment<FragmentImageBinding>() {

    override fun initViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentImageBinding {
        return FragmentImageBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated() {

    }

}