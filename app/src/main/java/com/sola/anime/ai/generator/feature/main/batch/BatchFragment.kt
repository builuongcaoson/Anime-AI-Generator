package com.sola.anime.ai.generator.feature.main.batch

import android.graphics.Paint
import android.os.Build
import android.util.Log
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import com.basic.common.base.LsFragment
import com.basic.common.extension.clicks
import com.basic.common.extension.getDimens
import com.basic.common.extension.isNetworkAvailable
import com.basic.common.extension.makeToast
import com.basic.common.extension.tryOrNull
import com.sola.anime.ai.generator.BuildConfig
import com.sola.anime.ai.generator.common.ConfigApp
import com.sola.anime.ai.generator.common.Constraint
import com.sola.anime.ai.generator.common.extension.getStatusBarHeight
import com.sola.anime.ai.generator.common.extension.initDezgoBodyTextsToImages
import com.sola.anime.ai.generator.common.extension.isNetworkAvailable
import com.sola.anime.ai.generator.common.extension.startArtProcessing
import com.sola.anime.ai.generator.common.extension.startBatchProcessing
import com.sola.anime.ai.generator.common.extension.startCredit
import com.sola.anime.ai.generator.common.extension.startIap
import com.sola.anime.ai.generator.common.ui.dialog.NetworkDialog
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.data.db.query.ExploreDao
import com.sola.anime.ai.generator.databinding.FragmentBatchBinding
import com.sola.anime.ai.generator.domain.manager.AnalyticManager
import com.sola.anime.ai.generator.domain.model.PromptBatch
import com.sola.anime.ai.generator.domain.model.Sampler
import com.sola.anime.ai.generator.feature.main.batch.adapter.CategoryAdapter
import com.sola.anime.ai.generator.feature.main.batch.adapter.PreviewCategoryAdapter
import com.sola.anime.ai.generator.feature.main.batch.adapter.PromptAdapter
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
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

    override fun onViewCreated() {
        initView()
        listenerView()
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
        binding.textSeeAll.clicks(withAnim = true) {  }
        binding.viewCredit.clicks(withAnim = true) { activity?.startCredit() }
        binding.cardGenerate.clicks(withAnim = false) { generateClicks() }
    }

    private fun generateClicks() {
        val task = {
            analyticManager.logEvent(AnalyticManager.TYPE.GENERATE_BATCH_CLICKED)

            val dezgoBodies = promptAdapter.data.flatMapIndexed { index: Int, item: PromptBatch ->
                val prompt = tryOrNull { item.prompt.takeIf { it.isNotEmpty() } } ?: tryOrNull { exploreDao.getAll().random().prompt } ?: listOf("Girl", "Boy").random()
                val negativePrompt = tryOrNull { item.negativePrompt.takeIf { it.isNotEmpty() }?.let { Constraint.Dezgo.DEFAULT_NEGATIVE + ", $it" } ?: Constraint.Dezgo.DEFAULT_NEGATIVE } ?: Constraint.Dezgo.DEFAULT_NEGATIVE

                initDezgoBodyTextsToImages(
                    groupId = index.toLong(),
                    maxChildId = item.numberOfImages.number - 1,
                    prompt = prompt,
                    negativePrompt = negativePrompt,
                    guidance = item.guidance.toString(),
                    steps = item.step.toString(),
                    model = previewCategoryAdapter.category.modelId,
                    sampler = if (item.sampler == Sampler.Random) listOf(Sampler.Ddim, Sampler.Dpm, Sampler.Euler, Sampler.EulerA).random().sampler else item.sampler.sampler,
                    upscale = if (item.isFullHd) "2" else "1",
                    styleId = -1,
                    ratio = item.ratio,
                    seed = null,
                    type = 1
                )
            }

            configApp.dezgoBodiesTextsToImages = dezgoBodies

            activity?.startBatchProcessing()
        }

        activity?.let { activity ->
            when {
                !activity.isNetworkAvailable() -> networkDialog.show(activity) {
                    networkDialog.dismiss()
                }
                configApp.discountCredit > prefs.getCredits().roundToInt() -> activity.startCredit()
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
        categoryAdapter
            .clicks
            .autoDispose(scope())
            .subscribe { categoryAdapter.category = it }

        previewCategoryAdapter
            .clicks
            .autoDispose(scope())
            .subscribe { previewCategoryAdapter.category = it }

        promptAdapter
            .fullHdChanges
            .autoDispose(scope())
            .subscribe { updateUiCredit() }

        promptAdapter
            .numberOfImagesChanges
            .autoDispose(scope())
            .subscribe { pair ->
//                promptAdapter.data.getOrNull(pair.second)?.numberOfImages = pair.first
//                promptAdapter.notifyItemChanged(pair.second)

//                activity?.hideKeyboard()
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
        val creditNumbers = promptAdapter.data.sumOf { it.numberOfImages.number + if (it.isFullHd) 5 else 0 }
        val creditForRatio = 0f
        val creditFor1Image = 10f
        val discount = 0.2f

        configApp.discountCredit = ((creditNumbers * (creditForRatio + creditFor1Image)) - (creditNumbers * discount)).roundToInt()
        val totalCredit = ((creditNumbers * (creditForRatio + creditFor1Image))).roundToInt()

        binding.discountCredit.text = configApp.discountCredit.toString()
        binding.totalCredit.apply {
            text = totalCredit.toString()
            paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            isVisible = configApp.discountCredit != totalCredit
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