package com.sola.anime.ai.generator.feature.main.explore

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.lifecycleScope
import com.basic.common.base.LsFragment
import com.basic.common.extension.clicks
import com.sola.anime.ai.generator.data.db.query.LoRAGroupDao
import com.sola.anime.ai.generator.data.db.query.ModelDao
import com.sola.anime.ai.generator.databinding.FragmentExploreBinding
import com.sola.anime.ai.generator.domain.model.ModelOrLoRA
import com.sola.anime.ai.generator.domain.repo.SyncRepository
import com.sola.anime.ai.generator.feature.main.explore.adapter.ModelAndLoRAPreviewAdapter
import com.sola.anime.ai.generator.feature.main.explore.adapter.TopPreviewAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ExploreFragment: LsFragment<FragmentExploreBinding>(FragmentExploreBinding::inflate) {

    @Inject lateinit var topPreviewAdapter: TopPreviewAdapter
    @Inject lateinit var modelAndLoRAPreviewAdapter: ModelAndLoRAPreviewAdapter
    @Inject lateinit var syncRepo: SyncRepository
    @Inject lateinit var modelDao: ModelDao
    @Inject lateinit var loRAGroupDao: LoRAGroupDao

    private val modelsAndLoRAsLiveData = MediatorLiveData<List<ModelOrLoRA>>()

    override fun onViewCreated() {
        initView()
        initData()
        listenerView()
    }

    private fun listenerView() {
        binding.viewGenerate.clicks {  }
    }

    private fun initData() {
        lifecycleScope
            .launch(Dispatchers.IO) {
                syncRepo.syncModelsAndLoRAs { progress ->
                    launch(Dispatchers.Main) {
                        when (progress) {
                            is SyncRepository.Progress.Running -> {
                                binding.loadingModelAndLoRA.animate().alpha(1f).setDuration(250).start()
                                binding.recyclerModelAndLoRA.animate().alpha(0f).setDuration(250).start()
                            }
                            is SyncRepository.Progress.SyncedModelsAndLoRAs -> {
                                binding.loadingModelAndLoRA.animate().alpha(0f).setDuration(250).start()
                                binding.recyclerModelAndLoRA.animate().alpha(1f).setDuration(250).start()
                            }
                        }
                    }
                }
            }

        val modelLiveData = modelDao.getAllLive()
        val loRAGroupLiveData = loRAGroupDao.getAllLive()

        modelsAndLoRAsLiveData.addSource(modelLiveData) { models ->
            val modelsItem = models.map { ModelOrLoRA(display = it.display, preview = it.preview, favouriteCount = it.favouriteCount, description = it.description, isFavourite = false) }
            val loRAsItem = loRAGroupLiveData.value?.flatMap { it.childs.map { ModelOrLoRA(display = it.display, preview = it.previews.firstOrNull() ?: "", favouriteCount = it.favouriteCount, description = "", isFavourite = false) } } ?: listOf()

            val combinedList = mutableListOf<ModelOrLoRA>()
            combinedList.addAll(modelsItem)
            combinedList.addAll(loRAsItem)
            modelsAndLoRAsLiveData.value = combinedList
        }
        modelsAndLoRAsLiveData.addSource(loRAGroupLiveData) { loRAGroups ->
            val modelsItem = modelLiveData.value?.map { ModelOrLoRA(display = it.display, preview = it.preview, favouriteCount = it.favouriteCount, description = it.description, isFavourite = false) } ?: listOf()
            val loRAsItem = loRAGroups.flatMap { it.childs.map { ModelOrLoRA(display = it.display, preview = it.previews.firstOrNull() ?: "", favouriteCount = it.favouriteCount, description = "", isFavourite = false) } }

            val combinedList = mutableListOf<ModelOrLoRA>()
            combinedList.addAll(modelsItem)
            combinedList.addAll(loRAsItem)
            modelsAndLoRAsLiveData.value = combinedList
        }
        modelsAndLoRAsLiveData.observe(viewLifecycleOwner) { modelsAndLoRAs ->
            modelAndLoRAPreviewAdapter.data = modelsAndLoRAs
        }
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