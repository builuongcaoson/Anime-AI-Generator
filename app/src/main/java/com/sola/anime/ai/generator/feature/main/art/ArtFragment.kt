package com.sola.anime.ai.generator.feature.main.art

import android.os.Build
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.basic.common.base.LsFragment
import com.basic.common.extension.clicks
import com.basic.common.extension.getDimens
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.common.ConfigApp
import com.sola.anime.ai.generator.common.Navigator
import com.sola.anime.ai.generator.common.extension.startArtProcessing
import com.sola.anime.ai.generator.common.extension.startExplore
import com.sola.anime.ai.generator.common.extension.startIap
import com.sola.anime.ai.generator.common.util.HorizontalMarginItemDecoration
import com.sola.anime.ai.generator.databinding.FragmentArtBinding
import com.sola.anime.ai.generator.feature.main.art.adapter.AspectRatioAdapter
import com.sola.anime.ai.generator.feature.main.art.adapter.PreviewAdapter
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
class ArtFragment : LsFragment<FragmentArtBinding>(FragmentArtBinding::inflate) {

    @Inject lateinit var previewAdapter: PreviewAdapter
    @Inject lateinit var aspectRatioAdapter: AspectRatioAdapter
    @Inject lateinit var configApp: ConfigApp

    private val subjectFirstView: Subject<Boolean> = BehaviorSubject.createDefault(true)

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
            .filter { it }
            .debounce(250, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribe {
                subjectFirstView.onNext(false)

                binding.viewPager.setCurrentItem((binding.viewPager.adapter?.itemCount ?: 2) / 2, false)
                binding.viewPager.animate().alpha(1f).setDuration(500).start()
            }

        aspectRatioAdapter
            .clicks
            .autoDispose(scope())
            .subscribe { aspectRatioAdapter.ratio = it }
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            binding.nestedScrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
                val alpha = scrollY.toFloat() / binding.viewShadow.height.toFloat()
                val alphaBottom = 1 - scrollY.toFloat() / binding.cardGenerate.height.toFloat()

                binding.viewShadow.alpha = alpha
                binding.viewShadowBottom.alpha = alphaBottom
            }
        }
        binding.viewPro.clicks { activity?.startIap() }
        binding.cardGenerate.clicks { activity?.startArtProcessing() }
        binding.viewExplore.clicks(withAnim = false){ activity?.startExplore() }
    }

    private fun initView() {
        activity?.let { activity ->
            binding.viewPager.apply {
                this.adapter = previewAdapter.apply {
                    this.data = configApp.previewsInRes.shuffled()
                }
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