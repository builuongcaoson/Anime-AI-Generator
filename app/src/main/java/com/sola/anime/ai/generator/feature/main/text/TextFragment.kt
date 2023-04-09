package com.sola.anime.ai.generator.feature.main.text

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.basic.common.base.LsFragment
import com.basic.common.extension.getDimens
import com.sola.anime.ai.generator.common.util.HorizontalMarginItemDecoration
import com.sola.anime.ai.generator.databinding.FragmentTextBinding
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.abs

@AndroidEntryPoint
class TextFragment : LsFragment<FragmentTextBinding>() {

    @Inject lateinit var previewAdapter: PreviewAdapter
    @Inject lateinit var aspectRatioAdapter: AspectRatioAdapter

    private val subjectFirstView: Subject<Unit> = BehaviorSubject.createDefault(Unit)

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

    override fun onResume() {
        initObservable()
        super.onResume()
    }

    private fun initObservable() {
        subjectFirstView
            .take(1)
            .debounce(500, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribe {
                binding.viewPager.setCurrentItem((binding.viewPager.adapter?.itemCount ?: 2) / 2, false)
                binding.viewPager.animate().alpha(1f).setDuration(500).start()
            }
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
            }

            binding.recyclerViewAspectRatio.apply {
                this.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
                this.adapter = aspectRatioAdapter
            }
        }
    }

}