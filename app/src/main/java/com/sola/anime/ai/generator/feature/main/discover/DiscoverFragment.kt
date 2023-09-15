package com.sola.anime.ai.generator.feature.main.discover

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import com.basic.common.base.LsFragment
import com.basic.common.extension.clicks
import com.basic.common.extension.getDimens
import com.sola.anime.ai.generator.common.extension.getStatusBarHeight
import com.sola.anime.ai.generator.common.extension.startCredit
import com.sola.anime.ai.generator.common.extension.startPickAvatar
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.databinding.FragmentDiscoverBinding
import com.trello.rxlifecycle2.kotlin.bindToLifecycle
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class DiscoverFragment : LsFragment<FragmentDiscoverBinding>(FragmentDiscoverBinding::inflate) {

    @Inject lateinit var prefs: Preferences

    override fun onViewCreated() {
        lifecycleScope.launch(Dispatchers.Main) {
            delay(750L)

            initView()
            listenerView()
        }
    }

    override fun onResume() {
        initObservable()
        super.onResume()
    }

    @SuppressLint("AutoDispose", "CheckResult")
    private fun initObservable() {
        prefs
            .creditsChanges
            .asObservable()
            .map { prefs.getCredits() }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
            .bindToLifecycle(binding.root)
            .subscribe { credits ->
                binding.credits.text = credits.roundToInt().toString()
            }

        prefs
            .isUpgraded
            .asObservable()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
            .bindToLifecycle(binding.root)
            .subscribe { isUpgraded ->
                binding.viewPro.isVisible = !isUpgraded
            }

        Observable
            .timer(1, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
            .bindToLifecycle(binding.root)
            .subscribe {
                binding.viewCredit.animate().alpha(1f).setDuration(250L).start()
                binding.viewPro.animate().alpha(1f).setDuration(250L).start()
            }
    }

    private fun listenerView() {
        binding.viewAvatar.clicks(withAnim = false){ activity?.startPickAvatar() }
        binding.viewCredit.clicks(withAnim = true) { activity?.startCredit() }
    }

    private fun initView() {

    }

}