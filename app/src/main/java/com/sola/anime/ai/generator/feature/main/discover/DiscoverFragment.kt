package com.sola.anime.ai.generator.feature.main.discover

import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import com.basic.common.base.LsFragment
import com.basic.common.extension.clicks
import com.basic.common.extension.getDimens
import com.sola.anime.ai.generator.common.extension.getStatusBarHeight
import com.sola.anime.ai.generator.common.extension.startCredit
import com.sola.anime.ai.generator.common.extension.startPickAvatar
import com.sola.anime.ai.generator.databinding.FragmentDiscoverBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DiscoverFragment : LsFragment<FragmentDiscoverBinding>(FragmentDiscoverBinding::inflate) {

    override fun onViewCreated() {
        initView()
        listenerView()
    }

    private fun listenerView() {
        binding.viewAvatar.clicks(withAnim = false){ activity?.startPickAvatar() }
        binding.viewCredit.clicks(withAnim = true) { activity?.startCredit() }
    }

    private fun initView() {
        activity?.let { activity ->
            binding.viewTop.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                this.topMargin = when(val statusBarHeight = activity.getStatusBarHeight()) {
                    0 -> activity.getDimens(com.intuit.sdp.R.dimen._30sdp).toInt()
                    else -> statusBarHeight
                }
            }
        }
    }

}