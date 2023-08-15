package com.sola.anime.ai.generator.feature.main.explore

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.lifecycleScope
import com.basic.common.base.LsFragment
import com.basic.common.extension.clicks
import com.sola.anime.ai.generator.common.extension.combineWith
import com.sola.anime.ai.generator.data.db.query.LoRAGroupDao
import com.sola.anime.ai.generator.data.db.query.ModelDao
import com.sola.anime.ai.generator.databinding.FragmentExploreBinding
import com.sola.anime.ai.generator.domain.model.ModelOrLoRA
import com.sola.anime.ai.generator.domain.model.config.lora.LoRAGroup
import com.sola.anime.ai.generator.domain.model.config.model.Model
import com.sola.anime.ai.generator.domain.repo.SyncRepository
import com.sola.anime.ai.generator.feature.main.explore.adapter.ModelAndLoRAPreviewAdapter
import com.sola.anime.ai.generator.feature.main.explore.adapter.TopPreviewAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class ExploreFragment: LsFragment<FragmentExploreBinding>(FragmentExploreBinding::inflate) {

    @Inject lateinit var topPreviewAdapter: TopPreviewAdapter
    @Inject lateinit var modelAndLoRAPreviewAdapter: ModelAndLoRAPreviewAdapter
    @Inject lateinit var syncRepo: SyncRepository
    @Inject lateinit var modelDao: ModelDao
    @Inject lateinit var loRAGroupDao: LoRAGroupDao

    override fun onViewCreated() {
        initView()
        initData()
        listenerView()
    }

    private fun listenerView() {
        binding.viewGenerate.clicks {  }
    }

    private fun initData() {
        lifecycleScope.launch {
            MediatorLiveData<Pair<List<Model>, List<LoRAGroup>>>().apply {
                addSource(modelDao.getAllLive()) { value = it to (value?.second ?: listOf()) }
                addSource(loRAGroupDao.getAllLive()) { value = (value?.first ?: listOf()) to it }
            }.observe(viewLifecycleOwner) { pair ->
                Timber.e("Data model or loRA size: ${pair.first.size} --- ${pair.second.size}")
            }
        }

//        modelDao.getAllLive().combineWith(loRAGroupDao.getAllLive()) { models, loRAGroups ->
//            val modelsItem = models?.map { ModelOrLoRA(display = it.display, preview = it.preview, favouriteCount = it.favouriteCount, description = it.description, isFavourite = false) } ?: listOf()
//            val loRAsItem = loRAGroups?.flatMap { it.childs.map { loRA -> ModelOrLoRA(display = loRA.display, preview = loRA.previews.firstOrNull() ?: "", favouriteCount = loRA.favouriteCount, description = "", isFavourite = false) } } ?: listOf()

//            modelAndLoRAPreviewAdapter.data = ArrayList<ModelOrLoRA>().apply {
//                addAll(modelsItem)
//                addAll(loRAsItem)
//            }

//            Timber.e("Data model or loRA size: ${models?.size} --- ${loRAGroups?.size}")
//        }

        lifecycleScope
            .launch(Dispatchers.IO) {
                syncRepo.syncModelsAndLoRAs { progress ->
                    launch(Dispatchers.Main) {
                        Timber.e("progress: $progress")

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