package com.sola.anime.ai.generator.feature.tutorial

import android.os.Bundle
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.transition.TransitionManager
import com.basic.common.base.LsActivity
import com.basic.common.extension.clicks
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.common.extension.backTopToBottom
import com.sola.anime.ai.generator.common.extension.startIap
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.databinding.ActivityTutorialBinding
import com.sola.anime.ai.generator.domain.model.config.tutorial.TutorialStep
import com.sola.anime.ai.generator.feature.tutorial.adapter.PromptAdapter
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class TutorialActivity : LsActivity<ActivityTutorialBinding>(ActivityTutorialBinding::inflate) {

    @Inject lateinit var promptAdapter: PromptAdapter
    @Inject lateinit var prefs: Preferences

    private val steps by lazy { TutorialStep.values() }
    private var indexStep1: Int = -1
    private var indexStep2: Int = -1
    private var indexStep3: Int = -1
    private var isStarted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initView()
        initObservable()
        initData()
        listenerView()
    }

    private fun initData() {

    }

    private fun listenerView() {
        binding.viewSkip.clicks {
            if (!isStarted){
                prefs.isViewTutorial.set(true)
                isStarted = true
                startIap(isKill = false)
                finish()
            }
        }
    }

    private fun initObservable() {
        promptAdapter
            .clicks
            .autoDispose(scope())
            .subscribe { pair ->
                when {
                    indexStep1 == -1 -> indexStep1 = pair.second
                    indexStep2 == -1 -> indexStep2 = pair.second
                    indexStep3 == -1 -> indexStep3 = pair.second
                }

                Timber.e("Step1: $indexStep1 --- Step2: $indexStep2 --- Step3: $indexStep3")

                lifecycleScope.launch(Dispatchers.Main) {
                    binding.viewTapLottie.isVisible = false
                    binding.viewClicksLoading.isVisible = true
                    binding.viewLoading.animate().alpha(1f).setDuration(250).start()
                    promptAdapter.positionSelected = pair.second
                    val newPrompt = when {
                        indexStep2 == -1 -> this@TutorialActivity.steps.getOrNull(indexStep1)?.display
                        indexStep3 == -1 -> ", ${this@TutorialActivity.steps.getOrNull(indexStep1)?.childs?.getOrNull(indexStep2)?.display ?: ""}"
                        else -> ", ${this@TutorialActivity.steps.getOrNull(indexStep1)?.childs?.getOrNull(indexStep2)?.childs?.getOrNull(indexStep3)?.display ?: ""}"
                    }
                    newPrompt?.forEach { char ->
                        binding.textPrompt.append(char.toString())
                        delay(100)
                    }
                    when  {
                        indexStep2 == -1 -> {
                            binding.displayStep.text = "Step 2 of 3"
                            binding.titlePrompt.text = "Add some decorations"
                            ConstraintSet().apply {
                                clone(binding.viewGroupStep)
                                constrainPercentWidth(binding.viewProgressStep.id, 0.666f)
                                applyTo(binding.viewGroupStep)
                            }
                            promptAdapter.data = this@TutorialActivity.steps.getOrNull(indexStep1)?.childs?.map { it.display } ?: listOf()
                        }
                        indexStep3 == -1 -> {
                            binding.displayStep.text = "Step 3 of 3"
                            binding.titlePrompt.text = "Add some decorations"
                            ConstraintSet().apply {
                                clone(binding.viewGroupStep)
                                constrainPercentWidth(binding.viewProgressStep.id, 1f)
                                applyTo(binding.viewGroupStep)
                            }
                            promptAdapter.data = this@TutorialActivity.steps.getOrNull(indexStep1)?.childs?.getOrNull(indexStep2)?.childs?.map { it.display } ?: listOf()
                        }
                        else ->{
                            promptAdapter.data = listOf()
                        }
                    }
                    binding.viewClicksLoading.isVisible = false
                    binding.viewLoading.animate().alpha(0f).setDuration(250).start()
                    promptAdapter.positionSelected = -1

                    val preview = when {
                        indexStep2 == -1 -> this@TutorialActivity.steps.getOrNull(indexStep1)?.preview
                        indexStep3 == -1 -> this@TutorialActivity.steps.getOrNull(indexStep1)?.childs?.getOrNull(indexStep2)?.preview
                        else -> this@TutorialActivity.steps.getOrNull(indexStep1)?.childs?.getOrNull(indexStep2)?.childs?.getOrNull(indexStep3)?.preview
                    }
//                    binding.preview.setImageResource(preview ?: R.drawable.preview_tutorial_default)

                    when  {
                        indexStep1 != -1 && indexStep2 != -1 && indexStep3 != -1 -> {
                            TransitionManager.beginDelayedTransition(binding.viewGroup)
                            binding.viewPrompt.isVisible = false
                            binding.displayStep.text = "Done!"
                            delay(2000)
                            if (!isStarted){
                                prefs.isViewTutorial.set(true)
                                isStarted = true
                                startIap(isKill = false)
                                finish()
                            }
                        }
                    }
                }
            }
    }

    private fun initView() {
        binding.recyclerPrompt.apply {
            this.adapter = promptAdapter.apply {
                this.data = this@TutorialActivity.steps.map { it.display }
            }
        }
    }

    @Deprecated("Deprecated in Java", ReplaceWith("finish()"))
    override fun onBackPressed() {
        finish()
    }

}