package com.sola.anime.ai.generator.feature.main.batch

import android.annotation.SuppressLint
import android.graphics.Paint
import android.os.Build
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.basic.common.base.LsFragment
import com.basic.common.extension.clicks
import com.basic.common.extension.getDimens
import com.basic.common.extension.isNetworkAvailable
import com.basic.common.extension.tryOrNull
import com.sola.anime.ai.generator.common.ConfigApp
import com.sola.anime.ai.generator.common.Constraint
import com.sola.anime.ai.generator.common.extension.*
import com.sola.anime.ai.generator.common.ui.dialog.NetworkDialog
import com.sola.anime.ai.generator.common.ui.sheet.model.SheetModel
import com.sola.anime.ai.generator.common.ui.sheet.style.SheetStyle
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.data.db.query.ExploreDao
import com.sola.anime.ai.generator.data.db.query.ModelDao
import com.sola.anime.ai.generator.data.db.query.StyleDao
import com.sola.anime.ai.generator.databinding.FragmentBatchBinding
import com.sola.anime.ai.generator.domain.manager.AnalyticManager
import com.sola.anime.ai.generator.domain.model.PromptBatch
import com.sola.anime.ai.generator.domain.model.Sampler
import com.sola.anime.ai.generator.feature.main.batch.adapter.PromptAdapter
import com.trello.rxlifecycle2.kotlin.bindToLifecycle
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class BatchFragment : LsFragment<FragmentBatchBinding>(FragmentBatchBinding::inflate) {

    companion object {
        private const val MAX_PROMPT = 5
    }

    @Inject lateinit var promptAdapter: PromptAdapter
    @Inject lateinit var configApp: ConfigApp
    @Inject lateinit var exploreDao: ExploreDao
    @Inject lateinit var prefs: Preferences
    @Inject lateinit var networkDialog: NetworkDialog
    @Inject lateinit var analyticManager: AnalyticManager
    @Inject lateinit var modelDao: ModelDao
    @Inject lateinit var styleDao: StyleDao

    private val sheetModel by lazy { SheetModel() }
    private val sheetStyle by lazy { SheetStyle() }

    private var totalCreditsDeducted = 98f
    private var creditsPerImage = totalCreditsDeducted / 10

    override fun onViewCreated() {
        lifecycleScope.launch(Dispatchers.Main) {
            delay(500L)

            initView()
            initObservable()
            initData()
            listenerView()
        }
    }

    private fun initData() {
        modelDao.getAllDislikeLive().observeAndRemoveWhenNotEmpty(viewLifecycleOwner){ models ->
            val model = models.find { model -> model.modelId == Constraint.Dezgo.DEFAULT_MODEL } ?: models.firstOrNull()
            promptAdapter.data.getOrNull(0)?.model = model
            promptAdapter.notifyItemChanged(0)
        }

        styleDao.getAllLive().observeAndRemoveWhenNotEmpty(viewLifecycleOwner) { styles ->
            val style = styles.find { style -> style.display == "No Style" } ?: styles.firstOrNull()
            promptAdapter.data.getOrNull(0)?.style = style
            promptAdapter.notifyItemChanged(0)
        }

        modelDao.getAllLive().observe(viewLifecycleOwner){ models ->
            val pairModelsFavourite = "Favourite" to models.filter { it.isFavourite }
            val pairModelsOther = "Other" to models.filter { !it.isFavourite && !it.isDislike }
            val pairModelsDislike = "Dislike" to models.filter { it.isDislike }

            sheetModel.pairs = listOf(pairModelsFavourite, pairModelsOther, pairModelsDislike)
        }

        styleDao.getAllLive().observe(viewLifecycleOwner) { styles ->
            sheetStyle.pairs = listOf("" to styles)
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
        binding.viewCredit.clicks(withAnim = true) { activity?.startCredit() }
        binding.viewPro.clicks(withAnim = true) { activity?.startIap() }
        binding.cardGenerate.clicks(withAnim = false) { generateClicks() }
    }

    private fun generateClicks() {
        val task = {
            activity?.let { activity ->
                analyticManager.logEvent(AnalyticManager.TYPE.GENERATE_BATCH_CLICKED)

                configApp.creditsRemaining = prefs.getCredits()
                configApp.dezgoBodiesTextsToImages = promptAdapter.data.flatMapIndexed { index: Int, item: PromptBatch ->
                    val prompt = tryOrNull { item.prompt.takeIf { it.isNotEmpty() } } ?: tryOrNull { exploreDao.getAll().random().prompt } ?: listOf("Girl", "Boy").random()
                    val negative = tryOrNull { item.negativePrompt.takeIf { it.isNotEmpty() }?.let { Constraint.Dezgo.DEFAULT_NEGATIVE + ", $it" } ?: Constraint.Dezgo.DEFAULT_NEGATIVE } ?: Constraint.Dezgo.DEFAULT_NEGATIVE

                    initDezgoBodyTextsToImages(
                        context = activity,
                        prefs = prefs,
                        configApp = configApp,
                        creditsPerImage = creditsPerImage,
                        groupId = index.toLong(),
                        maxChildId = item.numberOfImages.number - 1,
                        prompt = prompt,
                        negative = negative,
                        guidance = item.guidance.toString(),
                        steps = item.step.toString(),
                        model = item.model?.modelId ?: Constraint.Dezgo.DEFAULT_MODEL,
                        sampler = if (item.sampler == Sampler.Random) listOf(Sampler.Ddim, Sampler.Dpm, Sampler.Euler, Sampler.EulerA).random().sampler else item.sampler.sampler,
                        upscale = "2",
                        styleId = item.style?.id ?: -1,
                        ratio = item.ratio,
                        seed = null,
                        loRAs = listOf(),
                        type = 1
                    )
                }
                configApp.dezgoBodiesImagesToImages = emptyList()

                activity.startBatchProcessing(totalCreditsDeducted= totalCreditsDeducted, creditsPerImage = creditsPerImage)
            }
        }

        activity?.let { activity ->
            when {
                !activity.isNetworkAvailable() -> networkDialog.show(activity) {
                    networkDialog.dismiss()
                }
                totalCreditsDeducted > prefs.getCredits().roundToInt() -> activity.startCredit()
                else -> task()
            }
        }
    }

    private fun plusPrompt() {
        promptAdapter.data = ArrayList(promptAdapter.data).apply {
            add(
                PromptBatch().apply {
                    this.model = modelDao.getAll().find { model -> model.modelId == Constraint.Dezgo.DEFAULT_MODEL } ?: modelDao.getAll().firstOrNull()
                    this.style = styleDao.getAll().find { style -> style.display == "No Style" } ?: styleDao.getAll().firstOrNull()
                }
            )
        }
        binding.nestedScrollView.post { binding.nestedScrollView.smoothScrollTo(0, binding.nestedScrollView.getChildAt(0).height) }

        binding.viewPlusPrompt.isVisible = promptAdapter.data.size in 0 .. MAX_PROMPT

        updateUiCredit()
    }

    @SuppressLint("AutoDispose", "CheckResult")
    override fun initObservable() {
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

        promptAdapter
            .modelClicks
            .autoDispose(scope())
            .subscribe { index ->
                sheetModel.model = promptAdapter.data.getOrNull(index)?.model
                sheetModel.clicks = { model ->
                    sheetModel.dismiss()

                    promptAdapter.data.getOrNull(index)?.model = model
                    promptAdapter.notifyItemChanged(index)
                }
                sheetModel.detailsClicks = { model ->
                    lifecycleScope.launch(Dispatchers.Main) {
                        sheetModel.dismiss()

                        delay(250L)

                        activity?.startDetailModelOrLoRA(modelId = model.id)
                    }
                }
                sheetModel.show(this)
            }

        promptAdapter
            .styleClicks
            .autoDispose(scope())
            .subscribe { index ->
                sheetStyle.style = promptAdapter.data.getOrNull(index)?.style
                sheetStyle.clicks = { style ->
                    sheetStyle.dismiss()

                    promptAdapter.data.getOrNull(index)?.style = style
                    promptAdapter.notifyItemChanged(index)
                }
                sheetStyle.show(this)
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

                updateUiCredit()
            }

        prefs
            .isUpgraded
            .asObservable()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribe { isUpgraded ->
                binding.viewPro.isVisible = !isUpgraded
            }

//        Observable
//            .timer(1, TimeUnit.SECONDS)
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribeOn(AndroidSchedulers.mainThread())
//            .autoDispose(scope())
//            .subscribe {
//                binding.viewCredit.animate().alpha(1f).setDuration(250L).start()
//                binding.viewPro.animate().alpha(1f).setDuration(250L).start()
//            }
    }

    private fun updateUiCredit(){
        val totalCredits = promptAdapter.data.sumByDouble { (it.numberOfImages.number * if (it.isFullHd) 15.0 else 10.0) }.toFloat()
        val numberOfImages = promptAdapter.data.sumOf { it.numberOfImages.number }

        totalCreditsDeducted = (totalCredits - (totalCredits * configApp.discountCredits))
        creditsPerImage = totalCreditsDeducted / numberOfImages

        binding.discountCredit.text = totalCreditsDeducted.roundToInt().toString()
        binding.totalCredit.apply {
            text = totalCredits.roundToInt().toString()
            paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            isVisible = totalCreditsDeducted.roundToInt() != totalCredits.roundToInt()
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