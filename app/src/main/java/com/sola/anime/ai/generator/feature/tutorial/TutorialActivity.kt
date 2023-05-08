package com.sola.anime.ai.generator.feature.tutorial

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.basic.common.base.LsActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.sola.anime.ai.generator.common.extension.backTopToBottom
import com.sola.anime.ai.generator.databinding.ActivityTutorialBinding
import com.sola.anime.ai.generator.domain.model.config.tutorial.TutorialStep
import com.sola.anime.ai.generator.feature.tutorial.adapter.PromptAdapter
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TutorialActivity : LsActivity() {

    @Inject lateinit var promptAdapter: PromptAdapter

    private val binding by lazy { ActivityTutorialBinding.inflate(layoutInflater) }
    private val steps by lazy { TutorialStep.values() }
    private var indexStep: Int = 0

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

    }

    private fun initObservable() {
        promptAdapter
            .clicks
            .autoDispose(scope())
            .subscribe { pair ->
                indexStep = when (indexStep) {
                    0 -> 1
                    1 -> 2
                    else -> 3
                }

                lifecycleScope.launch(Dispatchers.Main) {
                    promptAdapter.positionSelected = pair.second
                    delay(2000)
                    promptAdapter.positionSelected = -1
                    promptAdapter.data = when (indexStep) {
                        0 -> this@TutorialActivity.steps.map { it.display }
                        else -> this@TutorialActivity.steps.getOrNull(pair.second)?.childs?.map { it.display } ?: listOf()
                    }
                    Glide
                        .with(this@TutorialActivity)
                        .asBitmap()
                        .load(this@TutorialActivity.steps.getOrNull(pair.second)?.preview)
                        .transition(BitmapTransitionOptions.withCrossFade())
                        .into(binding.preview)
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
        backTopToBottom()
    }

}