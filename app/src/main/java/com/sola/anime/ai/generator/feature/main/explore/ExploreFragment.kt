package com.sola.anime.ai.generator.feature.main.explore

import android.annotation.SuppressLint
import android.os.Build
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.map
import com.basic.common.base.LsFragment
import com.basic.common.extension.clicks
import com.basic.common.extension.tryOrNull
import com.sola.anime.ai.generator.common.extension.*
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.data.db.query.ExploreDao
import com.sola.anime.ai.generator.data.db.query.LoRAGroupDao
import com.sola.anime.ai.generator.data.db.query.ModelDao
import com.sola.anime.ai.generator.databinding.FragmentExploreBinding
import com.sola.anime.ai.generator.domain.model.ModelOrLoRA
import com.sola.anime.ai.generator.domain.model.config.lora.LoRAGroup
import com.sola.anime.ai.generator.domain.model.config.model.Model
import com.sola.anime.ai.generator.domain.repo.SyncRepository
import com.sola.anime.ai.generator.feature.main.MainActivity
import com.sola.anime.ai.generator.feature.main.explore.adapter.ExploreAdapter
import com.sola.anime.ai.generator.feature.main.explore.adapter.ModelAndLoRAAdapter
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import com.trello.rxlifecycle2.kotlin.bindToLifecycle
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose

@AndroidEntryPoint
class ExploreFragment: LsFragment<FragmentExploreBinding>(FragmentExploreBinding::inflate) {

    @Inject lateinit var modelAndLoRAAdapter: ModelAndLoRAAdapter
    @Inject lateinit var exploreAdapter: ExploreAdapter
    @Inject lateinit var syncRepo: SyncRepository
    @Inject lateinit var modelDao: ModelDao
    @Inject lateinit var loRAGroupDao: LoRAGroupDao
    @Inject lateinit var exploreDao: ExploreDao
    @Inject lateinit var prefs: Preferences

    private val subjectDataModelsAndLoRAChanges: Subject<List<ModelOrLoRA>> = PublishSubject.create()
    private val subjectDataExploreChanges: Subject<Unit> = PublishSubject.create()
    private val subjectScrollAtBottom: Subject<Unit> = PublishSubject.create()

    private var markFavourite = false

    override fun onViewCreated() {
        lifecycleScope.launch(Dispatchers.Main) {
            delay(250L)

            initView()
            delay(250L)
            initData()
            listenerView()
        }
    }

    @SuppressLint("AutoDispose", "CheckResult")
    override fun initObservable() {
        subjectScrollAtBottom
            .autoDispose(scope())
            .subscribe { tryOrNull { exploreAdapter.loadMore() } }

        modelAndLoRAAdapter
            .favouriteClicks
            .autoDispose(scope())
            .subscribe { modelAndLoRA ->
                when {
                    modelAndLoRA.model != null -> modelDao.updates(modelAndLoRA.model)
                    modelAndLoRA.loRA != null && modelAndLoRA.loRAGroupId != -1L -> loRAGroupDao.findById(modelAndLoRA.loRAGroupId)?.let { loRAGroup ->
                        loRAGroup.childs.find { loRA -> loRA.id == modelAndLoRA.loRA.id }?.let { loRA ->
                            loRA.isFavourite = !loRA.isFavourite
                            loRAGroupDao.updates(loRAGroup)
                        }
                    }
                }
            }

        modelAndLoRAAdapter
            .clicks
            .autoDispose(scope())
            .subscribe { modelAndLoRA ->
                when {
                    modelAndLoRA.isPremium && !prefs.isUpgraded.get() -> activity?.startIap()
                    modelAndLoRA.model != null -> (activity as? MainActivity)?.startDetailModelOrLoRA(modelId = modelAndLoRA.model.id, isFull = true)
                    modelAndLoRA.loRA != null -> (activity as? MainActivity)?.startDetailModelOrLoRA(loRAGroupId = modelAndLoRA.loRAGroupId, loRAId = modelAndLoRA.loRA.id, isFull = true)
                }
            }

        subjectDataModelsAndLoRAChanges
            .filter { it.isNotEmpty() }
            .autoDispose(scope())
            .subscribe { dataModelOrLoRA ->
                lifecycleScope
                    .launch(Dispatchers.Main) {
                        if (binding.recyclerModelAndLoRA.alpha != 1f){
                            delay(250L)
                        }

                        modelAndLoRAAdapter.data = dataModelOrLoRA

                        if (binding.recyclerModelAndLoRA.alpha != 1f){
                            delay(250L)

                            binding.recyclerModelAndLoRA.animate().alpha(1f).setDuration(250).start()
                        }
                    }
            }

        subjectDataExploreChanges
            .autoDispose(scope())
            .subscribe {
                lifecycleScope.launch(Dispatchers.Main) {
                    if (binding.recyclerExplore.alpha != 1f){
                        delay(250L)
                    }

                    binding.recyclerExplore.adapter = exploreAdapter

                    if (binding.recyclerExplore.alpha != 1f){
                        delay(250L)

                        binding.recyclerExplore.animate().alpha(1f).setDuration(250).start()
                    }
                }
            }

        exploreAdapter
            .clicks
            .autoDispose(scope())
            .subscribe { explore ->
                (activity as? MainActivity)?.startDetailExplore(exploreId = explore.id, isFull = true)
            }

        exploreAdapter
            .favouriteClicks
            .autoDispose(scope())
            .subscribe { explore ->
                markFavourite = true

                exploreDao.updates(explore)
            }
    }

    private fun listenerView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            binding.nestedScrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
                if (binding.nestedScrollView.height + scrollY >= binding.nestedScrollView.getChildAt(0).height) {
                    subjectScrollAtBottom.onNext(Unit)
                }
            }
        }
        binding.viewGenerate.clicks { (activity as? MainActivity)?.startArt(isFull = true) }
        binding.viewSearch.clicks { (activity as? MainActivity)?.startSearch(isFull = true) }
    }

    private fun initData() {
        lifecycleScope
            .launch(Dispatchers.IO) {
                syncRepo.syncModelsAndLoRAs { progress ->
                    when (progress) {
                        is SyncRepository.Progress.SyncedModelsAndLoRAs -> {
                            launch(Dispatchers.Main) { initObserveModelsAndLoRAsDatas() }
                        }
                        else -> {}
                    }
                }
                syncRepo.syncExplores { progress ->
                    when (progress) {
                        is SyncRepository.Progress.SyncedExplores -> {
                            launch(Dispatchers.Main) { initObserveExploresDatas() }
                        }
                        else -> {}
                    }
                }
            }
    }

    private fun initObserveExploresDatas() {
        exploreDao.getAllDislikeLive().observe(viewLifecycleOwner) { explores ->
            if (explores.isEmpty()) return@observe

            if (!markFavourite){
                exploreAdapter.explores = explores

                subjectDataExploreChanges.onNext(Unit)
            }

            markFavourite = false
        }
    }

    private fun initObserveModelsAndLoRAsDatas() {
        MediatorLiveData<Pair<List<Model>, List<LoRAGroup>>>().apply {
            addSource(modelDao.getAllDislikeLive()) { value = it to (value?.second ?: listOf()) }
            addSource(loRAGroupDao.getAllLive()) { value = (value?.first ?: listOf()) to it }
        }.map { pair ->
            val modelsItem = pair.first.map { model -> ModelOrLoRA(display = model.display, model = model, favouriteCount = model.favouriteCount, isFavourite = model.isFavourite, isPremium = model.isPremium, sortOrder = model.sortOrder) }
            val loRAsItem = pair.second.flatMap { it.childs.map { loRA -> ModelOrLoRA(display = loRA.display, loRA = loRA, loRAGroupId = it.id, favouriteCount = loRA.favouriteCount, isFavourite = loRA.isFavourite, isPremium = false, sortOrder = loRA.sortOrder) } }

            ArrayList(modelsItem + loRAsItem).sortedBy { it.sortOrder }
        }.observe(viewLifecycleOwner) { modelsAndLoRAsItem ->
            subjectDataModelsAndLoRAChanges.onNext(modelsAndLoRAsItem)
        }
    }

    private fun initView() {
        binding.recyclerModelAndLoRA.adapter = modelAndLoRAAdapter
    }

    override fun onDestroy() {
        exploreAdapter.hashmapDrawables.clear()
        super.onDestroy()
    }

}