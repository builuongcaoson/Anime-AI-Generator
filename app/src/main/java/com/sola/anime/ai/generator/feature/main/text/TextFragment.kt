package com.sola.anime.ai.generator.feature.main.text

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.basic.common.base.LsFragment
import com.basic.common.extension.getDimens
import com.intuit.sdp.R
import com.sola.anime.ai.generator.common.util.HorizontalMarginItemDecoration
import com.sola.anime.ai.generator.databinding.FragmentTextBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.math.abs

@AndroidEntryPoint
class TextFragment : LsFragment<FragmentTextBinding>() {

    @Inject lateinit var previewAdapter: PreviewAdapter
    @Inject lateinit var aspectRatioAdapter: AspectRatioAdapter

    override fun initViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentTextBinding {
        return FragmentTextBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated() {
        initView()
    }

    private fun initView() {
        activity?.let { activity ->
            binding.viewPager.apply {
                this.adapter = previewAdapter
                this.offscreenPageLimit = 1
                this.run {
                    val nextItemVisiblePx = activity.getDimens(R.dimen._40sdp)
                    val currentItemHorizontalMarginPx = activity.getDimens(R.dimen._50sdp)
                    val pageTranslationX = nextItemVisiblePx + currentItemHorizontalMarginPx
                    this.setPageTransformer { page: View, position: Float ->
                        page.translationX = -pageTranslationX * position
                        page.scaleY = 1 - (0.25f * abs(position))
                    }
                }
                this.addItemDecoration(HorizontalMarginItemDecoration(activity.getDimens(R.dimen._50sdp).toInt()))
                this.postDelayed({ this.setCurrentItem((this.adapter?.itemCount ?: 2) / 2, false)}, 250)
            }

            binding.recyclerViewAspectRatio.apply {
                this.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
                this.adapter = aspectRatioAdapter
            }
        }
    }

}