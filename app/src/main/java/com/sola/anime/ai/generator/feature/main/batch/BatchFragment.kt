package com.sola.anime.ai.generator.feature.main.batch

import android.graphics.Paint
import android.os.Build
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import com.basic.common.base.LsFragment
import com.basic.common.extension.clicks
import com.basic.common.extension.getDimens
import com.basic.common.extension.isNetworkAvailable
import com.basic.common.extension.tryOrNull
import com.sola.anime.ai.generator.common.ConfigApp
import com.sola.anime.ai.generator.common.Constraint
import com.sola.anime.ai.generator.common.extension.*
import com.sola.anime.ai.generator.common.ui.dialog.NetworkDialog
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.data.db.query.ExploreDao
import com.sola.anime.ai.generator.data.db.query.ModelDao
import com.sola.anime.ai.generator.databinding.FragmentBatchBinding
import com.sola.anime.ai.generator.domain.manager.AnalyticManager
import com.sola.anime.ai.generator.domain.model.PreviewCategoryBatch
import com.sola.anime.ai.generator.domain.model.PromptBatch
import com.sola.anime.ai.generator.domain.model.Sampler
import com.sola.anime.ai.generator.feature.main.batch.adapter.CategoryAdapter
import com.sola.anime.ai.generator.feature.main.batch.adapter.PreviewCategoryAdapter
import com.sola.anime.ai.generator.feature.main.batch.adapter.PromptAdapter
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class BatchFragment : LsFragment<FragmentBatchBinding>(FragmentBatchBinding::inflate) {

    companion object {
        private const val MAX_PROMPT = 5
    }

    @Inject lateinit var categoryAdapter: CategoryAdapter
    @Inject lateinit var previewCategoryAdapter: PreviewCategoryAdapter
    @Inject lateinit var promptAdapter: PromptAdapter
    @Inject lateinit var configApp: ConfigApp
    @Inject lateinit var exploreDao: ExploreDao
    @Inject lateinit var prefs: Preferences
    @Inject lateinit var networkDialog: NetworkDialog
    @Inject lateinit var analyticManager: AnalyticManager
    @Inject lateinit var modelDao: ModelDao

    override fun onViewCreated() {
        initView()
        initData()
        listenerView()
    }

    private fun initData() {
        modelDao.getAllLive().observe(viewLifecycleOwner){ models ->
            previewCategoryAdapter.data = ArrayList(models.map {
                PreviewCategoryBatch(preview = it.preview, display = it.display, model = it.modelId, description = it.description)
            })
            previewCategoryAdapter.data.firstOrNull()?.also { previewCategory ->
                previewCategoryAdapter.category = previewCategory
                configApp.modelBatchChoice = models.find { it.display == previewCategory?.display }
            }
        }
    }

    private fun listenerView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            binding.nestedScrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
                val alpha = scrollY.toFloat() / binding.viewShadow.height.toFloat()
                val alphaBottom = 1 - scrollY.toFloat() / binding.cardGenerate.height.toFloat()

                binding.viewShadow.alpha = alpha
                binding.viewShadowBottom.alpha = alphaBottom
            }
        }

        binding.viewPlusPrompt.clicks(withAnim = false) { plusPrompt() }
        binding.textSeeAll.clicks(withAnim = true) { activity?.startModel(isBatch = true) }
        binding.viewCredit.clicks(withAnim = true) { activity?.startCredit() }
        binding.cardGenerate.clicks(withAnim = false) { generateClicks() }
    }

    private fun generateClicks() {
        val task = {
            activity?.let { activity ->
                analyticManager.logEvent(AnalyticManager.TYPE.GENERATE_BATCH_CLICKED)

                val numberOfImagesCreate = promptAdapter.data.sumOf { it.numberOfImages.number }

                val dezgoBodies = promptAdapter.data.flatMapIndexed { index: Int, item: PromptBatch ->
                    val prompt = tryOrNull { item.prompt.takeIf { it.isNotEmpty() } } ?: tryOrNull { exploreDao.getAll().random().prompt } ?: listOf("Girl", "Boy").random()
                    val negative = tryOrNull { item.negativePrompt.takeIf { it.isNotEmpty() }?.let { Constraint.Dezgo.DEFAULT_NEGATIVE + ", $it" } ?: Constraint.Dezgo.DEFAULT_NEGATIVE } ?: Constraint.Dezgo.DEFAULT_NEGATIVE

                    initDezgoBodyTextsToImages(
                        context = activity,
                        prefs = prefs,
                        configApp = configApp,
                        creditsPerImage = configApp.discountCreditBatch.toFloat() / numberOfImagesCreate.toFloat(),
                        groupId = index.toLong(),
                        maxChildId = item.numberOfImages.number - 1,
                        prompt = prompt,
                        negative = negative,
                        guidance = item.guidance.toString(),
                        steps = item.step.toString(),
                        model = configApp.modelBatchChoice?.modelId ?: Constraint.Dezgo.DEFAULT_MODEL,
                        sampler = if (item.sampler == Sampler.Random) listOf(Sampler.Ddim, Sampler.Dpm, Sampler.Euler, Sampler.EulerA).random().sampler else item.sampler.sampler,
                        upscale = "2",
                        styleId = -1,
                        ratio = item.ratio,
                        seed = null,
                        type = 1
                    )
                }

                configApp.dezgoBodiesTextsToImages = dezgoBodies

                activity.startBatchProcessing()
            }
        }

        activity?.let { activity ->
            when {
                !activity.isNetworkAvailable() -> networkDialog.show(activity) {
                    networkDialog.dismiss()
                }
                configApp.discountCreditBatch > prefs.getCredits().roundToInt() -> activity.startCredit()
                else -> task()
            }
        }
    }

    private fun plusPrompt() {
        promptAdapter.data = ArrayList(promptAdapter.data).apply {
            add(PromptBatch())
        }
        binding.nestedScrollView.post { binding.nestedScrollView.smoothScrollTo(0, binding.nestedScrollView.getChildAt(0).height) }

        binding.viewPlusPrompt.isVisible = promptAdapter.data.size in 0 .. MAX_PROMPT

        updateUiCredit()
    }

    override fun onResume() {
        initObservable()
        super.onResume()
    }

    private fun initObservable() {
        configApp
            .subjectModelBatchChanges
            .map { configApp.modelBatchChoice }
            .autoDispose(scope())
            .subscribe { model ->
                previewCategoryAdapter.category = previewCategoryAdapter.data.find { it.display == model?.display }
            }

        categoryAdapter
            .clicks
            .autoDispose(scope())
            .subscribe { categoryBatch ->
                categoryAdapter.category = categoryBatch
            }

        previewCategoryAdapter
            .clicks
            .autoDispose(scope())
            .subscribe { previewCategory ->
                previewCategoryAdapter.category = previewCategory
                configApp.modelBatchChoice = modelDao.getAll().find { it.display == previewCategory.display }
            }

        promptAdapter
            .fullHdChanges
            .autoDispose(scope())
            .subscribe { updateUiCredit() }

        promptAdapter
            .numberOfImagesChanges
            .autoDispose(scope())
            .subscribe {
                updateUiCredit()
            }

        promptAdapter
            .deleteClicks
            .autoDispose(scope())
            .subscribe { index ->
                promptAdapter.data = ArrayList(promptAdapter.data).apply {
                    removeAt(index)
                }

                binding.viewPlusPrompt.isVisible = promptAdapter.data.size in 0 .. MAX_PROMPT

                updateUiCredit()
            }

        prefs
            .creditsChanges
            .asObservable()
            .map { prefs.getCredits() }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribe { credits ->
                binding.credits.text = credits.roundToInt().toString()
            }
    }

    private fun updateUiCredit(){
        val creditForNumbersOfImages = promptAdapter.data.sumByDouble { (it.numberOfImages.number * if (it.isFullHd) 15.0 else 10.0) }
        val discount = 0.02

        configApp.discountCreditBatch = (creditForNumbersOfImages - (creditForNumbersOfImages * discount)).roundToInt()

        val totalCredit = creditForNumbersOfImages.roundToInt()

        binding.discountCredit.text = configApp.discountCreditBatch.toString()
        binding.totalCredit.apply {
            text = totalCredit.toString()
            paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            isVisible = configApp.discountCreditBatch != totalCredit
        }
        binding.timeGenerate.text = "About ${((promptAdapter.data.sumOf { it.numberOfImages.number } / 10) + 1)} minute"
    }

    private fun initView() {
        activity?.let { activity ->
            binding.viewTop.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                this.topMargin = when(val statusBarHeight = activity.getStatusBarHeight()) {
                    0 -> activity.getDimens(com.intuit.sdp.R.dimen._30sdp).toInt()
                    else -> statusBarHeight
                }
            }

            binding.recyclerCategory.apply {
                this.layoutManager = LinearLayoutManager(activity, HORIZONTAL, false)
                this.adapter = categoryAdapter
            }

            binding.recyclerPreviewCategory.apply {
                this.layoutManager = LinearLayoutManager(activity, HORIZONTAL, false)
                this.adapter = previewCategoryAdapter
            }

            binding.recyclerPrompt.apply {
                this.layoutManager = object: LinearLayoutManager(activity, VERTICAL, false){
                    override fun canScrollVertically(): Boolean {
                        return false
                    }
                }
                this.itemAnimator = null
                this.adapter = promptAdapter
            }
        }
    }

}