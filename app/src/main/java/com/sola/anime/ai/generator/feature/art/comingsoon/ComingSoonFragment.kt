package com.sola.anime.ai.generator.feature.art.comingsoon

import com.basic.common.base.LsFragment
import com.sola.anime.ai.generator.databinding.FragmentComingSoonBinding
import com.sola.anime.ai.generator.domain.model.config.explore.Explore
import com.sola.anime.ai.generator.domain.model.config.model.Model
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ComingSoonFragment : LsFragment<FragmentComingSoonBinding>(FragmentComingSoonBinding::inflate) {

    override fun onViewCreated() {
        initView()
        initData()
        listenerView()
    }

    private fun initData() {

    }

    private fun updateUiModel(model: Model?) {

    }

    override fun initObservable() {

    }

    private fun updateUiExplore(explore: Explore?) {

    }

    private fun listenerView() {

    }

    private fun initView() {

    }

}