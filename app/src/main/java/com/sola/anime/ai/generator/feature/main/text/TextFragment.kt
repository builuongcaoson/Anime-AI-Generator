package com.sola.anime.ai.generator.feature.main.text

import android.animation.ArgbEvaluator
import android.graphics.Bitmap
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.basic.common.base.LsFragment
import com.basic.common.extension.getDimens
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.common.util.HorizontalMarginItemDecoration
import com.sola.anime.ai.generator.databinding.FragmentTextBinding
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
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
        listenerView()
    }

    override fun onDestroy() {
        binding.viewPager.unregisterOnPageChangeCallback(previewChanges)
        super.onDestroy()
    }

    private val previewChanges = object: ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {

        }
    }

    private fun listenerView() {
        binding.viewPager.registerOnPageChangeCallback(previewChanges)
    }

    private fun initView() {
        activity?.let { activity ->
            binding.viewPager.apply {
                this.adapter = previewAdapter
                this.offscreenPageLimit = 1
                this.run {
                    val nextItemVisiblePx = activity.getDimens(com.intuit.sdp.R.dimen._40sdp)
                    val currentItemHorizontalMarginPx = activity.getDimens(com.intuit.sdp.R.dimen._50sdp)
                    val pageTranslationX = nextItemVisiblePx + currentItemHorizontalMarginPx
                    this.setPageTransformer { page: View, position: Float ->
                        page.translationX = -pageTranslationX * position
                        page.scaleY = 1 - (0.25f * abs(position))
                    }
                }
                this.addItemDecoration(HorizontalMarginItemDecoration(activity.getDimens(com.intuit.sdp.R.dimen._50sdp).toInt()))
                this.postDelayed({ this.setCurrentItem((this.adapter?.itemCount ?: 2) / 2, false)}, 250)
            }

            binding.recyclerViewAspectRatio.apply {
                this.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
                this.adapter = aspectRatioAdapter
            }
        }
    }

}