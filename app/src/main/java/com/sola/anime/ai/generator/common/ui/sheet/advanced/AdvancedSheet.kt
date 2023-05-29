package com.sola.anime.ai.generator.common.ui.sheet.advanced

import android.annotation.SuppressLint
import android.view.MotionEvent
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.basic.common.extension.clicks
import com.jakewharton.rxbinding2.widget.textChanges
import com.sola.anime.ai.generator.common.ConfigApp
import com.sola.anime.ai.generator.common.base.LsBottomSheet
import com.sola.anime.ai.generator.common.extension.startIap
import com.sola.anime.ai.generator.data.Preferences
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
    @Inject lateinit var prefs: Preferences

    var negative: String = ""
    var guidance: Float = 7.5f

    override fun onViewCreated() {
        initView()
        listenerView()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun listenerView() {
        binding.slider.setListener { _, currentValue ->
            guidance = currentValue
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
        binding.clear.clicks { binding.editNegative.setText("") }
        binding.viewPinNegative.clicks {  }
        binding.viewPinRatio.clicks {  }
//        binding.viewPinCFG.clicks {  }
//        binding.viewPinSeed.clicks {  }
    }

    override fun onResume() {
        initObservable()
        super.onResume()
    }

    private fun initObservable() {
        aspectRatioAdapter
            .clicks
            .autoDispose(scope())
            .subscribe {
                when {
                    !prefs.isUpgraded.get() -> activity?.startIap()
                    else -> configApp.subjectRatioClicks.onNext(it)
                }
            }

        configApp
            .subjectRatioClicks
            .autoDispose(scope())
            .subscribe {
                aspectRatioAdapter.ratio = it
            }

        binding
            .editNegative
            .textChanges()
            .autoDispose(scope())
            .subscribe { negative ->
                this.negative = negative.toString()

                binding.viewClear.isVisible = !negative.isNullOrEmpty()

                binding.count.text = "${negative.length}/1000"
            }
    }

    private fun initView() {
        activity?.let { activity ->
            binding.recyclerViewAspectRatio.apply {
                this.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
                this.adapter = aspectRatioAdapter
            }
            binding.editNegative.setText(negative)
            binding.slider.currentValue = guidance
        }
    }

}