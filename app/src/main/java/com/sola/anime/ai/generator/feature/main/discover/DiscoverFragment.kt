package com.sola.anime.ai.generator.feature.main.discover

import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import com.basic.common.base.LsFragment
import com.basic.common.extension.clicks
import com.basic.common.extension.getDimens
import com.sola.anime.ai.generator.common.extension.getStatusBarHeight
import com.sola.anime.ai.generator.common.extension.startCredit
import com.sola.anime.ai.generator.common.extension.startPickAvatar
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.databinding.FragmentDiscoverBinding
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class DiscoverFragment : LsFragment<FragmentDiscoverBinding>(FragmentDiscoverBinding::inflate) {

    @Inject lateinit var prefs: Preferences

    override fun onViewCreated() {
        initView()
        listenerView()
    }

    override fun onResume() {
        initObservable()
        super.onResume()
    }

    private fun initObservable() {
        prefs
            .creditsChanges
            .asObservable()
            .map { prefs.getCredits() }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribe { credits ->
                binding.credits.text = credits.roundToInt().toString()
            }
    }

    private fun listenerView() {
        binding.viewAvatar.clicks(withAnim = false){ activity?.startPickAvatar() }
        binding.viewCredit.clicks(withAnim = true) { activity?.startCredit() }
    }

    private fun initView() {
//        activity?.let { activity ->
//            binding.viewTop.updateLayoutParams<ViewGroup.MarginLayoutParams> {
//                this.topMargin = when(val statusBarHeight = activity.getStatusBarHeight()) {
//                    0 -> activity.getDimens(com.intuit.sdp.R.dimen._30sdp).toInt()
//                    else -> statusBarHeight
//                }
//            }
//        }
    }

}