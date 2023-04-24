package com.sola.anime.ai.generator.feature.main.art

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.basic.common.base.LsFragment
import com.basic.common.extension.clicks
import com.basic.common.extension.getDimens
import com.basic.common.extension.hideKeyboard
import com.basic.common.extension.showKeyboard
import com.bumptech.glide.Glide
import com.jakewharton.rxbinding2.widget.textChanges
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.common.ConfigApp
import com.sola.anime.ai.generator.common.Navigator
import com.sola.anime.ai.generator.common.extension.*
import com.sola.anime.ai.generator.common.ui.sheet.advanced.AdvancedSheet
import com.sola.anime.ai.generator.common.ui.sheet.history.HistorySheet
import com.sola.anime.ai.generator.common.util.HorizontalMarginItemDecoration
import com.sola.anime.ai.generator.data.db.query.ExploreDao
import com.sola.anime.ai.generator.data.db.query.StyleDao
import com.sola.anime.ai.generator.databinding.FragmentArtBinding
import com.sola.anime.ai.generator.domain.model.config.explore.Explore
import com.sola.anime.ai.generator.domain.model.config.style.Style
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
    @Inject lateinit var styleDao: StyleDao
    @Inject lateinit var exploreDao: ExploreDao

    private val subjectFirstView: Subject<Boolean> = BehaviorSubject.createDefault(true)

    private val historySheet by lazy { HistorySheet() }
    private val advancedSheet by lazy { AdvancedSheet() }

    override fun onViewCreated() {
        initView()
        initData()
        listenerView()
    }

    private fun initData() {
        exploreDao.getAllLive().observe(viewLifecycleOwner){ explores ->
            previewAdapter.data = explores
            binding.viewPager.offscreenPageLimit = if (explores.size >= 5) 3 else 1
        }
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
            .subscribe { configApp.subjectRatioClicks.onNext(it) }

        configApp
            .subjectRatioClicks
            .autoDispose(scope())
            .subscribe { aspectRatioAdapter.ratio = it }

        configApp
            .subjectStyleClicks
            .autoDispose(scope())
            .subscribe { id ->
                updateUiStyle(styleDao.findById(id))
            }

        configApp
            .subjectExploreClicks
            .autoDispose(scope())
            .subscribe { id ->
                updateUiExplore(exploreDao.findById(id))
            }

        binding
            .editPrompt
            .textChanges()
            .autoDispose(scope())
            .subscribe { prompt ->
                binding.viewClear.isVisible = !prompt.isNullOrEmpty()
                binding.history.isVisible = prompt.isNullOrEmpty()

                binding.count.text = "${prompt.length}/1000"
            }
    }

    private fun updateUiExplore(explore: Explore?) {
        binding.editPrompt.setText(explore?.prompt ?: "")
    }

    private fun updateUiStyle(style: Style?){
        binding.viewNoStyle.isVisible = style == null
        binding.cardStyle.isVisible = style != null
        binding.viewMoreStyle.isVisible = style != null
        binding.viewHadStyle.isVisible = style != null

        binding.displayStyle.text = when (style) {
            null -> "Pick a style"
            else -> style.display
        }

        style?.let {
            activity?.let { activity ->
                Glide
                    .with(activity)
                    .asBitmap()
                    .load(style.preview)
                    .thumbnail(0.7f)
                    .placeholder(R.drawable.place_holder_image)
                    .error(R.drawable.place_holder_image)
                    .into(binding.previewStyle)
            }
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

    @SuppressLint("ClickableViewAccessibility")
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
        binding.viewStyle.clicks{ activity?.startStyle() }
        binding.clear.clicks { binding.editPrompt.setText("") }
        binding.history.clicks { historySheet.show(this) }
        binding.editPrompt.setOnTouchListener { view, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    view.parent.requestDisallowInterceptTouchEvent(true)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    view.parent.requestDisallowInterceptTouchEvent(false)
                }
            }
            false
        }
        binding.viewAdvancedSetting.clicks { advancedSheet.show(this) }
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

            binding.editPrompt.disableEnter()
        }
    }

}