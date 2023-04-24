package com.sola.anime.ai.generator.common.ui.sheet.advanced

import android.annotation.SuppressLint
import android.view.MotionEvent
import androidx.recyclerview.widget.LinearLayoutManager
import com.sola.anime.ai.generator.common.ConfigApp
import com.sola.anime.ai.generator.common.base.LsBottomSheet
import com.sola.anime.ai.generator.databinding.SheetAdvancedBinding
import com.sola.anime.ai.generator.feature.main.art.adapter.AspectRatioAdapter
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AdvancedSheet: LsBottomSheet<SheetAdvancedBinding>(SheetAdvancedBinding::inflate) {

    @Inject lateinit var aspectRatioAdapter: AspectRatioAdapter
    @Inject lateinit var configApp: ConfigApp

    override fun onViewCreated() {
        initView()
        listenerView()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun listenerView() {
        binding.slider.setListener { _, currentValue ->

        }
        binding.slider.setOnTouchListener { view, event ->
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
    }

    override fun onResume() {
        initObservable()
        super.onResume()
    }

    private fun initObservable() {
        aspectRatioAdapter
            .clicks
            .autoDispose(scope())
            .subscribe { configApp.subjectRatioClicks.onNext(it) }

        configApp
            .subjectRatioClicks
            .autoDispose(scope())
            .subscribe { aspectRatioAdapter.ratio = it }
    }

    private fun initView() {
        activity?.let { activity ->
            binding.recyclerViewAspectRatio.apply {
                this.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
                this.adapter = aspectRatioAdapter
            }
        }
    }

}