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
import com.sola.anime.ai.generator.common.extension.getStatusBarHeight
import com.sola.anime.ai.generator.databinding.FragmentBatchBinding
import com.sola.anime.ai.generator.domain.model.PromptBatch
import com.sola.anime.ai.generator.feature.main.batch.adapter.CategoryAdapter
import com.sola.anime.ai.generator.feature.main.batch.adapter.PreviewCategoryAdapter
import com.sola.anime.ai.generator.feature.main.batch.adapter.PromptAdapter
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class BatchFragment : LsFragment<FragmentBatchBinding>(FragmentBatchBinding::inflate) {

    @Inject lateinit var categoryAdapter: CategoryAdapter
    @Inject lateinit var previewCategoryAdapter: PreviewCategoryAdapter
    @Inject lateinit var promptAdapter: PromptAdapter

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
    }

    private fun plusPrompt() {
        promptAdapter.data = ArrayList(promptAdapter.data).apply {
            add(PromptBatch())
        }
        binding.nestedScrollView.post { binding.nestedScrollView.smoothScrollTo(0, binding.nestedScrollView.getChildAt(0).height) }

        binding.viewPlusPrompt.isVisible = promptAdapter.data.size in 0 .. 10
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
            .numberOfImagesChanges
            .autoDispose(scope())
            .subscribe { pair ->
                promptAdapter.data.getOrNull(pair.second)?.numberOfImages = pair.first
                promptAdapter.notifyItemChanged(pair.second)

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

                binding.viewPlusPrompt.isVisible = promptAdapter.data.size in 0 .. 10

                updateUiCredit()
            }
    }

    private fun updateUiCredit(){
        val creditNumbers = promptAdapter.data.sumOf { it.numberOfImages.number }
        val creditForRatio = 5f
        val creditFor1Image = 15f
        val discount = 0.1f

        val discountCredit = (creditNumbers * (creditForRatio + creditFor1Image)) - (creditNumbers * discount)
        val totalCredit = (creditNumbers * (creditForRatio + creditFor1Image))

        binding.discountCredit.text = discountCredit.toInt().toString()
        binding.totalCredit.apply {
            text = totalCredit.toInt().toString()
            paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            isVisible = discountCredit.toInt() != totalCredit.toInt()
        }
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