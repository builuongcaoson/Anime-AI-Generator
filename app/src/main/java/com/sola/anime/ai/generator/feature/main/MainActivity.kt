package com.sola.anime.ai.generator.feature.main

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View
import com.basic.common.base.LsActivity
import com.basic.common.base.LsPageAdapter
import com.basic.common.extension.getDimens
import com.basic.common.extension.lightStatusBar
import com.basic.common.extension.transparent
import com.jakewharton.rxbinding2.view.clicks
import com.sola.anime.ai.generator.common.ConfigApp
import com.sola.anime.ai.generator.databinding.ActivityMainBinding
import com.sola.anime.ai.generator.databinding.LayoutBottomMainBinding
import com.sola.anime.ai.generator.feature.main.batch.BatchFragment
import com.sola.anime.ai.generator.feature.main.discover.DiscoverFragment
import com.sola.anime.ai.generator.feature.main.mine.MineFragment
import com.sola.anime.ai.generator.feature.main.art.ArtFragment
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : LsActivity() {

    @Inject lateinit var configApp: ConfigApp

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val fragments by lazy { listOf(ArtFragment(), BatchFragment(), DiscoverFragment(), MineFragment()) }
    private val bottomTabs by lazy { binding.initTabBottom() }
    private val subjectTabClicks: Subject<Int> = BehaviorSubject.createDefault(0) // Default tab home
    private var tabIndex = 0 // Default tab home

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transparent()
        lightStatusBar()
        setContentView(binding.root)

        initView()
        initObservable()
    }

    private fun initObservable() {
        bottomTabs.forEachIndexed { index, tab ->
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

        configApp
            .subjectExploreClicks
            .filter { it != -1L }
            .filter { binding.viewPager.currentItem != 0 }
            .autoDispose(scope())
            .subscribe { _ ->
                subjectTabClicks.onNext(0)
            }
    }

    private fun scrollToPage(index: Int) {
        if (tabIndex != index){
            bottomTabs.getOrNull(tabIndex)?.viewHide?.animShowTop()
            bottomTabs.getOrNull(tabIndex)?.viewShow?.animHideBottom()
        }

        bottomTabs.getOrNull(index)?.viewHide?.animHideTop()
        bottomTabs.getOrNull(index)?.viewShow?.animShowBottom()
        bottomTabs.getOrNull(index)?.let { binding.viewBottom.viewDividerTab.animToCenterView(it.viewClicks, it.viewShow.width) }

        binding.viewPager.currentItem = index
        tabIndex = index
    }

    private fun initView() {
        binding.viewPager.apply {
            this.adapter = LsPageAdapter(supportFragmentManager).apply {
                this.addFragment(fragments = fragments.toTypedArray())
            }
            this.offscreenPageLimit = this.adapter?.count ?: 0
        }
    }

    private data class Tab(val viewClicks: View, val viewHide: View, val viewShow: View)
    private fun ActivityMainBinding.initTabBottom() = listOf(viewBottom.initTabHome(), viewBottom.initTabExplore(), viewBottom.initTabGallery(), viewBottom.initTabSetting())
    private fun LayoutBottomMainBinding.initTabHome() = Tab(viewTab1, imageTab1, textTab1)
    private fun LayoutBottomMainBinding.initTabExplore() = Tab(viewTab2, imageTab2, textTab2)
    private fun LayoutBottomMainBinding.initTabGallery() = Tab(viewTab3, imageTab3, textTab3)
    private fun LayoutBottomMainBinding.initTabSetting() = Tab(viewTab4, imageTab4, textTab4)
    private fun View.animHideTop() { animate().translationY(-getDimens(com.intuit.sdp.R.dimen._30sdp)).alpha(0f).setDuration(100).start() }
    private fun View.animShowTop() { animate().translationY(0f).alpha(1f).setDuration(100).start() }
    private fun View.animHideBottom() { animate().translationY(getDimens(com.intuit.sdp.R.dimen._30sdp)).alpha(0f).setDuration(100).start() }
    private fun View.animShowBottom() { animate().translationY(0f).alpha(1f).setDuration(100).start() }
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