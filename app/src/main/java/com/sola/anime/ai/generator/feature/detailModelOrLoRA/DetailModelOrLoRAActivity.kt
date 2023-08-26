package com.sola.anime.ai.generator.feature.detailModelOrLoRA

import android.os.Bundle
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.lifecycleScope
import coil.load
import com.basic.common.base.LsActivity
import com.basic.common.extension.*
import com.basic.common.util.theme.TextViewStyler
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.common.extension.back
import com.sola.anime.ai.generator.common.extension.startDetailExplore
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.data.db.query.ExploreDao
import com.sola.anime.ai.generator.data.db.query.LoRAGroupDao
import com.sola.anime.ai.generator.data.db.query.ModelDao
import com.sola.anime.ai.generator.databinding.ActivityDetailModelOrLoraBinding
import com.sola.anime.ai.generator.domain.model.ExploreOrLoRAPreview
import com.sola.anime.ai.generator.domain.model.ModelOrLoRA
import com.sola.anime.ai.generator.domain.model.TabModelOrLoRA
import com.sola.anime.ai.generator.domain.model.config.lora.LoRA
import com.sola.anime.ai.generator.domain.model.config.lora.LoRAGroup
import com.sola.anime.ai.generator.domain.model.config.model.Model
import com.sola.anime.ai.generator.feature.detailModelOrLoRA.adapter.ExploreOrLoRAPreviewAdapter
import com.sola.anime.ai.generator.feature.detailModelOrLoRA.adapter.ModelAndLoRAAdapter
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class DetailModelOrLoRAActivity : LsActivity<ActivityDetailModelOrLoraBinding>(ActivityDetailModelOrLoraBinding::inflate) {

    companion object {
        const val MODEL_ID_EXTRA = "MODEL_ID_EXTRA"
        const val LORA_GROUP_ID_EXTRA = "LORA_GROUP_ID_EXTRA"
        const val LORA_ID_EXTRA = "LORA_ID_EXTRA"
    }

    @Inject lateinit var modelDao: ModelDao
    @Inject lateinit var loRAGroupDao: LoRAGroupDao
    @Inject lateinit var prefs: Preferences
    @Inject lateinit var exploreOrLoRAPreviewAdapter: ExploreOrLoRAPreviewAdapter
    @Inject lateinit var modelAndLoRAAdapter: ModelAndLoRAAdapter
    @Inject lateinit var exploreDao: ExploreDao

    private val subjectDataExploreOrLoRAChanges: Subject<List<ExploreOrLoRAPreview>> = PublishSubject.create()
    private val subjectTabChanges: Subject<TabModelOrLoRA> = BehaviorSubject.createDefault(TabModelOrLoRA.Artworks)
    private val subjectDataModelsAndLoRAChanges: Subject<List<ModelOrLoRA>> = PublishSubject.create()

    private val modelId by lazy { intent.getLongExtra(MODEL_ID_EXTRA, -1) }
    private val loRAGroupId by lazy { intent.getLongExtra(LORA_GROUP_ID_EXTRA, -1) }
    private val loRAId by lazy { intent.getLongExtra(LORA_ID_EXTRA, -1) }
    private var hadDataModelsAndLoRAs = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transparent()
        setContentView(binding.root)

        initView()
        initObservable()
        initData()
        listenerView()
    }

    private fun listenerView() {
        binding.back.clicks { onBackPressed() }
        binding.save.clicks {  }
        binding.dislike.clicks {  }
        binding.report.clicks {  }
        binding.viewArtworksBy.clicks(withAnim = false) { subjectTabChanges.onNext(TabModelOrLoRA.Artworks) }
        binding.viewOther.clicks(withAnim = false) { subjectTabChanges.onNext(TabModelOrLoRA.Others) }
        binding.favourite.clicks {
            when {
                modelId != -1L -> {
                    val model = modelDao.findById(modelId) ?: return@clicks
                    model.isFavourite = !model.isFavourite
                    modelDao.updates(model)
                }
                loRAGroupId != -1L && loRAId != -1L -> {
                    val loRAGroup = loRAGroupDao.findById(loRAGroupId) ?: return@clicks
                    val loRA = loRAGroup.childs.find { loRA -> loRA.id == loRAId } ?: return@clicks
                    loRA.isFavourite = !loRA.isFavourite
                    loRAGroupDao.update(loRAGroup)
                }
            }
        }
    }

    private fun initData() {
        MediatorLiveData<Pair<List<Model>, List<LoRAGroup>>>().apply {
            addSource(modelDao.getAllLive()) { value = it to (value?.second ?: listOf()) }
            addSource(loRAGroupDao.getAllLive()) { value = (value?.first ?: listOf()) to it }
        }.observe(this) { pair ->
            Timber.e("Data model or loRA size: ${pair.first.size} --- ${pair.second.size}")
            val modelsItem = pair.first.map { model -> ModelOrLoRA(display = model.display, model = model, favouriteCount = model.favouriteCount, isFavourite = model.isFavourite) }
            val loRAsItem = pair.second.flatMap { it.childs.map { loRA -> ModelOrLoRA(display = loRA.display, loRA = loRA, loRAGroupId = it.id, favouriteCount = loRA.favouriteCount, isFavourite = loRA.isFavourite) } }

            val datas = when {
                modelId != -1L -> modelsItem.filter { it.model?.id != modelId }
                loRAId != -1L -> loRAsItem.filter { it.loRA?.id != loRAId }
                else -> modelsItem.filter { it.model?.id != modelId } + loRAsItem.filter { it.loRA?.id != loRAId }
            }

            subjectDataModelsAndLoRAChanges.onNext(datas)
        }
    }

    private fun initObservable() {
        subjectDataModelsAndLoRAChanges
            .filter { !hadDataModelsAndLoRAs }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribe { dataModelOrLoRA ->
                Timber.e("Data model or loRA size: ${dataModelOrLoRA.size}")

                modelAndLoRAAdapter.data = dataModelOrLoRA

                hadDataModelsAndLoRAs = true
            }

        subjectTabChanges
            .skip(1)
            .distinctUntilChanged()
            .autoDispose(scope())
            .subscribe { tab ->
                binding.textArtworksBy.setTextColor(resolveAttrColor(if (tab == TabModelOrLoRA.Artworks) android.R.attr.textColorPrimary else android.R.attr.textColorSecondary))
                binding.textOther.setTextColor(resolveAttrColor(if (tab == TabModelOrLoRA.Others) android.R.attr.textColorPrimary else android.R.attr.textColorSecondary))

                binding.textArtworksBy.setTextFont(if (tab == TabModelOrLoRA.Artworks) TextViewStyler.FONT_SEMI else TextViewStyler.FONT_REGULAR)
                binding.textOther.setTextFont(if (tab == TabModelOrLoRA.Others) TextViewStyler.FONT_SEMI else TextViewStyler.FONT_REGULAR)

                binding.viewDividerArtworksBy.isVisible = tab == TabModelOrLoRA.Artworks
                binding.viewDividerOther.isVisible = tab == TabModelOrLoRA.Others

                binding
                    .recyclerExploreOrLoRA
                    .animate()
                    .alpha(0f)
                    .setDuration(100)
                    .withEndAction {
                        binding.recyclerExploreOrLoRA.adapter = when (tab) {
                            TabModelOrLoRA.Artworks -> exploreOrLoRAPreviewAdapter
                            else -> modelAndLoRAAdapter
                        }
                        binding.recyclerExploreOrLoRA
                            .animate()
                            .alpha(1f)
                            .setDuration(100)
                            .start()
                    }
                    .start()
            }

        subjectDataExploreOrLoRAChanges
            .debounce(250L, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribe { dataExploreOrLoRA ->
                Timber.e("Data size: ${dataExploreOrLoRA.size}")

                lifecycleScope.launch(Dispatchers.Main) {
                    exploreOrLoRAPreviewAdapter.data = dataExploreOrLoRA
                    delay(250L)
                    binding.loadingExploreOrLoRA.animate().alpha(0f).setDuration(250).start()
                    binding.recyclerExploreOrLoRA.animate().alpha(1f).setDuration(250).start()
                }
            }

        exploreOrLoRAPreviewAdapter
            .clicks
            .autoDispose(scope())
            .subscribe { exploreOrLoRAPreview ->
                when {
                    exploreOrLoRAPreview.explore != null -> startDetailExplore(exploreId = exploreOrLoRAPreview.explore.id)
                }
            }

        exploreOrLoRAPreviewAdapter
            .favouriteClicks
            .autoDispose(scope())
            .subscribe { exploreOrLoRAPreview ->
                when {
                    exploreOrLoRAPreview.explore != null -> exploreDao.update(exploreOrLoRAPreview.explore)
                }
            }

        modelAndLoRAAdapter
            .favouriteClicks
            .autoDispose(scope())
            .subscribe { modelOrLoRA ->
                when {
                    modelOrLoRA.model != null -> modelDao.updates(modelOrLoRA.model)
                    modelOrLoRA.loRA != null && loRAGroupId != -1L -> loRAGroupDao.findById(loRAGroupId)?.let { loRAGroup ->
                        loRAGroup.childs.find { loRA -> loRA.id == loRAId }?.let { loRA ->
                            loRA.isFavourite = !loRA.isFavourite
                            loRAGroupDao.update(loRAGroup)
                        }
                    }
                }
            }
    }

    private fun initView() {
        when {
            modelId != -1L -> initModelView()
            loRAGroupId != -1L -> initLoRAView()
            else -> {
                lifecycleScope.launch(Dispatchers.Main) {
                    delay(1000)

                    makeToast("Something wrong, please try again!")
                    finish()
                }
                return
            }
        }

        binding.recyclerExploreOrLoRA.adapter = exploreOrLoRAPreviewAdapter
    }

    private fun initLoRAView() {
        loRAGroupDao.findByIdLive(loRAGroupId).observe(this) { loRAGroup ->
            loRAGroup ?: return@observe

            val loRA = loRAGroup.childs.find { it.id == loRAId } ?: return@observe

            ConstraintSet().apply {
                this.clone(binding.viewPreview)
                this.setDimensionRatio(binding.preview.id, "3:4")
                this.applyTo(binding.viewPreview)
            }

            binding.preview.load(loRA.previews.firstOrNull()) {
                listener(
                    onSuccess = { _, result ->
                        binding.preview.setImageDrawable(result.drawable)
                        binding.preview.animate().alpha(1f).setDuration(250).start()
                        binding.viewShadow.animate().alpha(1f).setDuration(250).start()
                    }
                )
                crossfade(true)
                error(R.drawable.place_holder_image)
            }

            binding.viewDetail.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                this.bottomMargin = getDimens(com.intuit.sdp.R.dimen._70sdp).toInt()
            }

            binding.modelOrLoRA.text = "LoRA"
            binding.use.text = "Use this LoRA"
            binding.textArtworksBy.text = "Artworks by LoRA"
            binding.textOther.text = "Other LoRAs"
            binding.imgModelOrLoRA.setImageResource(R.drawable.star_of_david)
            binding.viewModelOrLoRA.setCardBackgroundColor(getColorCompat(R.color.red))
            binding.viewUse.setCardBackgroundColor(getColorCompat(R.color.red))
            binding.display.text = loRA.display
            binding.favouriteCount.text = "${if (loRA.isFavourite) loRA.favouriteCount + 1 else loRA.favouriteCount} Uses"
            binding.favourite.setTint(if (loRA.isFavourite) getColorCompat(R.color.red) else resolveAttrColor(android.R.attr.textColorPrimary))

            initLoRAData(loRA = loRA)
        }
    }

    private fun initLoRAData(loRA: LoRA) {
        lifecycleScope.launch(Dispatchers.Main) {
            val loRAPreviews = loRA.previews.map { preview -> ExploreOrLoRAPreview(loRAPreview = preview, ratio = listOf("2:3","3:4").random(), favouriteCount = loRA.favouriteCount, isFavourite = loRA.isFavourite) }
            subjectDataExploreOrLoRAChanges.onNext(ArrayList(loRAPreviews).apply {
                tryOrNull { this.removeFirst() }
            })
        }
    }

    private fun initModelView() {
        modelDao.findByIdLive(modelId).observe(this) { model ->
            model ?: return@observe

            ConstraintSet().apply {
                this.clone(binding.viewPreview)
                this.setDimensionRatio(binding.preview.id, "1:1")
                this.applyTo(binding.viewPreview)
            }

            binding.preview.load(model.preview) {
                listener(
                    onSuccess = { _, result ->
                        binding.preview.setImageDrawable(result.drawable)
                        binding.preview.animate().alpha(1f).setDuration(250).start()
                        binding.viewShadow.animate().alpha(1f).setDuration(250).start()
                    }
                )
                crossfade(true)
                error(R.drawable.place_holder_image)
            }

            binding.viewDetail.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                this.bottomMargin = getDimens(com.intuit.sdp.R.dimen._30sdp).toInt()
            }

            binding.modelOrLoRA.text = "Model"
            binding.use.text = "Use this Model"
            binding.textArtworksBy.text = "Artworks by Model"
            binding.textOther.text = "Other Models"
            binding.imgModelOrLoRA.setImageResource(R.drawable.user_robot)
            binding.viewModelOrLoRA.setCardBackgroundColor(getColorCompat(R.color.blue))
            binding.viewUse.setCardBackgroundColor(getColorCompat(R.color.blue))
            binding.display.text = model.display
            binding.favouriteCount.text = "${if (model.isFavourite) model.favouriteCount + 1 else model.favouriteCount} Uses"
            binding.favourite.setTint(if (model.isFavourite) getColorCompat(R.color.red) else resolveAttrColor(android.R.attr.textColorPrimary))

            initExploreData(model)
        }
    }

    private fun initExploreData(model: Model) {
        exploreDao.getAllLive().observe(this) { explores ->
            subjectDataExploreOrLoRAChanges.onNext(explores.filter { explore -> explore.modelIds.contains(model.id) }.map { explore -> ExploreOrLoRAPreview(explore = explore, ratio = explore.ratio, favouriteCount = explore.favouriteCount, isFavourite = explore.isFavourite) })
        }
    }

    @Deprecated("Deprecated in Java", ReplaceWith("finish()"))
    override fun onBackPressed() {
        back()
    }

}