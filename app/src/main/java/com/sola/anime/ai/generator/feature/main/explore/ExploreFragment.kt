package com.sola.anime.ai.generator.feature.main.explore

import com.basic.common.base.LsFragment
import com.basic.common.extension.clicks
import com.sola.anime.ai.generator.databinding.FragmentExploreBinding
import com.sola.anime.ai.generator.domain.repo.SyncRepository
import com.sola.anime.ai.generator.feature.main.explore.adapter.ModelAndLoRAPreviewAdapter
import com.sola.anime.ai.generator.feature.main.explore.adapter.TopPreviewAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ExploreFragment: LsFragment<FragmentExploreBinding>(FragmentExploreBinding::inflate) {

    @Inject lateinit var topPreviewAdapter: TopPreviewAdapter
    @Inject lateinit var modelAndLoRAPreviewAdapter: ModelAndLoRAPreviewAdapter
    @Inject lateinit var syncRepo: SyncRepository

    override fun onViewCreated() {
        initView()
        initData()
        listenerView()
    }

    private fun listenerView() {
        binding.viewGenerate.clicks {  }
    }

    private fun initData() {

    }

    private fun initView() {
        binding.viewPager.apply {
            this.adapter = topPreviewAdapter
            this.isUserInputEnabled = false
        }
        binding.recyclerModelAndLoRA.apply {
            this.adapter = modelAndLoRAPreviewAdapter
        }
    }

}