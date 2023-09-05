package com.sola.anime.ai.generator.feature.main.explore

import android.annotation.SuppressLint
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.lifecycleScope
import com.basic.common.base.LsFragment
import com.basic.common.extension.clicks
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.common.extension.*
import com.sola.anime.ai.generator.data.db.query.ExploreDao
import com.sola.anime.ai.generator.data.db.query.LoRAGroupDao
import com.sola.anime.ai.generator.data.db.query.ModelDao
import com.sola.anime.ai.generator.databinding.FragmentExploreBinding
import com.sola.anime.ai.generator.domain.model.ModelOrLoRA
import com.sola.anime.ai.generator.domain.model.config.explore.Explore
import com.sola.anime.ai.generator.domain.model.config.lora.LoRAGroup
import com.sola.anime.ai.generator.domain.model.config.model.Model
import com.sola.anime.ai.generator.domain.repo.SyncRepository
import com.sola.anime.ai.generator.feature.main.explore.adapter.ExploreAdapter
import com.sola.anime.ai.generator.feature.main.explore.adapter.ModelAndLoRAAdapter
import com.sola.anime.ai.generator.feature.main.explore.adapter.TopPreviewAdapter
import com.trello.rxlifecycle2.LifecycleProvider
import com.trello.rxlifecycle2.RxLifecycle.bindUntilEvent
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import com.trello.rxlifecycle2.android.FragmentEvent
import com.trello.rxlifecycle2.kotlin.bindToLifecycle

@AndroidEntryPoint
class ExploreFragment: LsFragment<FragmentExploreBinding>(FragmentExploreBinding::inflate) {

//    @Inject lateinit var topPreviewAdapter: TopPreviewAdapter
    @Inject lateinit var modelAndLoRAAdapter: ModelAndLoRAAdapter
    @Inject lateinit var exploreAdapter: ExploreAdapter
    @Inject lateinit var syncRepo: SyncRepository
    @Inject lateinit var modelDao: ModelDao
    @Inject lateinit var loRAGroupDao: LoRAGroupDao
    @Inject lateinit var exploreDao: ExploreDao

    private val subjectDataModelsAndLoRAChanges: Subject<List<ModelOrLoRA>> = PublishSubject.create()
    private val subjectDataExploreChanges: Subject<List<Explore>> = PublishSubject.create()

    private var markFavourite = false
    private var hadDataModelsAndLoRAs = false
    private var hadDataExplores = false

    override fun onViewCreated() {
        initView()
        initObservable()
        initData()
        listenerView()
    }

    @SuppressLint("AutoDispose", "CheckResult")
    private fun initObservable() {
        modelAndLoRAAdapter
            .favouriteClicks
            .bindToLifecycle(binding.root)
            .subscribe { modelAndLoRA ->
                when {
                    modelAndLoRA.model != null -> modelDao.updates(modelAndLoRA.model)
                    modelAndLoRA.loRA != null && modelAndLoRA.loRAGroupId != -1L -> loRAGroupDao.findById(modelAndLoRA.loRAGroupId)?.let { loRAGroup ->
                        loRAGroup.childs.find { loRA -> loRA.id == modelAndLoRA.loRA.id }?.let { loRA ->
                            loRA.isFavourite = !loRA.isFavourite
                            loRAGroupDao.update(loRAGroup)
                        }
                    }
                }
            }

        modelAndLoRAAdapter
            .clicks
            .bindToLifecycle(binding.root)
            .subscribe { modelOrLora ->
                when {
                    modelOrLora.model != null -> activity?.startDetailModelOrLoRA(modelId = modelOrLora.model.id)
                    modelOrLora.loRA != null -> activity?.startDetailModelOrLoRA(loRAGroupId = modelOrLora.loRAGroupId, loRAId = modelOrLora.loRA.id)
                }
            }

        subjectDataModelsAndLoRAChanges
            .debounce(if (hadDataModelsAndLoRAs) 0L else 250L, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
            .bindToLifecycle(binding.root)
            .subscribe { dataModelOrLoRA ->
                Timber.e("Data model or loRA size: ${dataModelOrLoRA.size}")

                lifecycleScope
                    .launch(Dispatchers.Main) {
                        modelAndLoRAAdapter.data = dataModelOrLoRA
                        delay(250L)
                        binding.loadingModelAndLoRA.animate().alpha(0f).setDuration(250).start()
                        binding.recyclerModelAndLoRA.animate().alpha(1f).setDuration(250).start()

                        hadDataModelsAndLoRAs = true
                    }
            }

        subjectDataExploreChanges
            .debounce(if (hadDataExplores) 0L else 250L, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
            .bindToLifecycle(binding.root)
            .subscribe { explores ->
                lifecycleScope
                    .launch(Dispatchers.Main) {
                        exploreAdapter.data = explores
                        delay(250L)
                        binding.loadingExplore.animate().alpha(0f).setDuration(250).start()
                        binding.recyclerExplore.animate().alpha(1f).setDuration(250).start()

                        hadDataExplores = true
                    }
            }

        exploreAdapter
            .clicks
            .bindToLifecycle(binding.root)
            .subscribe { explore ->
                activity?.startDetailExplore(exploreId = explore.id)
            }

        exploreAdapter
            .favouriteClicks
            .bindToLifecycle(binding.root)
            .subscribe { explore ->
                markFavourite = true

                exploreDao.update(explore)
            }
    }

    private fun listenerView() {
        binding.viewGenerate.clicks { activity?.startArt() }
        binding.viewSearch.clicks { activity?.startSearch() }
    }

    private fun initData() {
        exploreDao.getAllLive().observe(viewLifecycleOwner) { explores ->
            if (!markFavourite){
                subjectDataExploreChanges.onNext(explores)
            }
            markFavourite = false
        }

        MediatorLiveData<Pair<List<Model>, List<LoRAGroup>>>().apply {
            addSource(modelDao.getAllDislikeLive()) { value = it to (value?.second ?: listOf()) }
            addSource(loRAGroupDao.getAllLive()) { value = (value?.first ?: listOf()) to it }
        }.observe(viewLifecycleOwner) { pair ->
            val modelsItem = pair.first.map { model -> ModelOrLoRA(display = model.display, model = model, favouriteCount = model.favouriteCount, isFavourite = model.isFavourite) }
            val loRAsItem = pair.second.flatMap { it.childs.map { loRA -> ModelOrLoRA(display = loRA.display, loRA = loRA, loRAGroupId = it.id, favouriteCount = loRA.favouriteCount, isFavourite = loRA.isFavourite) } }

            subjectDataModelsAndLoRAChanges.onNext(modelsItem + loRAsItem)
        }

        lifecycleScope
            .launch(Dispatchers.IO) {
                syncRepo.syncModelsAndLoRAs {}
                syncRepo.syncExplores {}
            }
    }

    private fun initView() {
//        binding.viewPager.apply {
//            this.adapter = topPreviewAdapter
//            this.isUserInputEnabled = false
//        }
        binding.preview.load(R.drawable.preview_top_batch, errorRes = R.drawable.preview_top_batch) {
            binding.title.animate().alpha(1f).setDuration(250).start()
            binding.description.animate().alpha(1f).setDuration(250).start()
        }

        binding.recyclerModelAndLoRA.adapter = modelAndLoRAAdapter
        binding.recyclerExplore.adapter = exploreAdapter
    }

}