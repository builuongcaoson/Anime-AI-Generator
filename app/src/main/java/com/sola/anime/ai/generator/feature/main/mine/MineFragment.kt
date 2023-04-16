package com.sola.anime.ai.generator.feature.main.mine

import android.animation.ValueAnimator
import android.view.View
import com.basic.common.base.LsFragment
import com.basic.common.base.LsPageAdapter
import com.basic.common.extension.resolveAttrColor
import com.basic.common.widget.LsTextView
import com.jakewharton.rxbinding2.view.clicks
import com.sola.anime.ai.generator.databinding.FragmentMineBinding
import com.sola.anime.ai.generator.databinding.LayoutTopMineBinding
import com.sola.anime.ai.generator.feature.main.mine.feature.ArtFragment
import com.sola.anime.ai.generator.feature.main.mine.feature.AvatarFragment
import com.sola.anime.ai.generator.feature.main.mine.feature.BatchFragment
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class MineFragment : LsFragment<FragmentMineBinding>(FragmentMineBinding::inflate) {

    private val fragments by lazy { listOf(ArtFragment(), BatchFragment(), AvatarFragment()) }
    private val topTabs by lazy { binding.initTabTop() }
    private val subjectTabClicks: Subject<Int> = BehaviorSubject.createDefault(0) // Default tab home
    private var tabIndex = 0 // Default tab home

    override fun onViewCreated() {
        initView()
    }

    private fun initView() {
        binding.viewPager.apply {
            this.adapter = LsPageAdapter(childFragmentManager).apply {
                this.addFragment(fragments = fragments.toTypedArray())
            }
            this.offscreenPageLimit = this.adapter?.count ?: 0
        }
    }

    override fun onResume() {
        initObservable()
        super.onResume()
    }

    private fun initObservable() {
        topTabs.forEachIndexed { index, tab ->
            tab
                .viewClicks
                .clicks()
                .autoDispose(scope())
                .subscribe { subjectTabClicks.onNext(index) }
        }

        subjectTabClicks
            .take(1)
            .delay(250, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribe { index ->
                scrollToPage(index)
            }

        subjectTabClicks
            .skip(1)
            .distinctUntilChanged()
            .autoDispose(scope())
            .subscribe { index ->
                scrollToPage(index)
            }
    }

    private fun scrollToPage(index: Int) {
        if (tabIndex != index){
            activity?.let { activity ->
//                topTabs.getOrNull(tabIndex)?.textTitle?.setTextFont(FONT_REGULAR)
                topTabs.getOrNull(tabIndex)?.textTitle?.setTextColor(activity.resolveAttrColor(android.R.attr.textColorPrimary))
            }
        }

        activity?.let { activity ->
//            topTabs.getOrNull(index)?.textTitle?.setTextFont(FONT_SEMI)
            topTabs.getOrNull(index)?.textTitle?.setTextColor(activity.resolveAttrColor(android.R.attr.colorAccent))
        }
        topTabs.getOrNull(index)?.let { binding.viewTabTop.viewDividerTab.animToCenterView(it.viewClicks, it.textTitle.width) }

        binding.viewPager.currentItem = index
        tabIndex = index
    }

    private data class Tab(val viewClicks: View, val textTitle: LsTextView)
    private fun FragmentMineBinding.initTabTop() = listOf(viewTabTop.initTabArt(), viewTabTop.initTabBatch(), viewTabTop.initTabAvatar())
    private fun LayoutTopMineBinding.initTabArt() = Tab(viewTab1, textTab1)
    private fun LayoutTopMineBinding.initTabBatch() = Tab(viewTab2, textTab2)
    private fun LayoutTopMineBinding.initTabAvatar() = Tab(viewTab3, textTab3)
    private fun View.animToCenterView(centerView: View, newWidth: Int) {
        val newLayoutParams = this.layoutParams
        ValueAnimator.ofInt(width, newWidth).apply {
            this.duration = 100
            this.addUpdateListener {
                val value = it.animatedValue as Int
                newLayoutParams.width = value
                this@animToCenterView.layoutParams = newLayoutParams
            }
            this.start()
        }

        val endX = centerView.x + centerView.width / 2 - newWidth / 2
        animate().translationX(endX).setDuration(100).start()
    }

}