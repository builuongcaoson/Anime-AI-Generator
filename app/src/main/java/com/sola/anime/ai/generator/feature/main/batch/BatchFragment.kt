package com.sola.anime.ai.generator.feature.main.batch

import android.view.LayoutInflater
import android.view.ViewGroup
import com.basic.common.base.LsFragment
import com.sola.anime.ai.generator.databinding.FragmentBatchBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BatchFragment : LsFragment<FragmentBatchBinding>() {

    override fun initViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentBatchBinding {
        return FragmentBatchBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated() {

    }

}