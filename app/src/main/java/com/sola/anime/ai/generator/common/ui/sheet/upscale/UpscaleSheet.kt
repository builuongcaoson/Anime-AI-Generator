package com.sola.anime.ai.generator.common.ui.sheet.upscale

import androidx.core.view.isVisible
import com.basic.common.extension.clicks
import com.sola.anime.ai.generator.common.base.LsBottomSheet
import com.sola.anime.ai.generator.common.extension.startIap
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.databinding.SheetUpscaleBinding
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

@AndroidEntryPoint
class UpscaleSheet: LsBottomSheet<SheetUpscaleBinding>(SheetUpscaleBinding::inflate) {

    @Inject lateinit var prefs: Preferences

    val upscaleClicks: Subject<Unit> = PublishSubject.create()

    override fun onViewCreated() {
        initView()
        initData()
        listenerView()
    }

    private fun listenerView() {
        binding.viewUpscale.clicks(withAnim = false){ upscaleClicks.onNext(Unit) }
        binding.viewPremium.clicks(withAnim = false){ activity?.startIap() }
    }

    override fun onResume() {
        initObservable()
        super.onResume()
    }

    private fun initObservable() {
        prefs
            .isUpgraded
            .asObservable()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribe { isUpgraded ->
                binding.iconWatchAd.isVisible = !isUpgraded
                binding.textDescription.isVisible = !isUpgraded
                binding.viewPremium.isVisible = !isUpgraded
            }
    }

    private fun initData() {

    }

    private fun initView() {

    }

}