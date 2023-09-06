package com.sola.anime.ai.generator.feature.art

import android.os.Bundle
import androidx.core.view.isVisible
import com.basic.common.base.LsActivity
import com.basic.common.base.LsPageAdapter
import com.basic.common.extension.clicks
import com.sola.anime.ai.generator.common.extension.back
import com.sola.anime.ai.generator.common.extension.backTopToBottom
import com.sola.anime.ai.generator.common.extension.startCredit
import com.sola.anime.ai.generator.common.extension.startIap
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.databinding.ActivityArtBinding
import com.sola.anime.ai.generator.feature.art.art.ArtFragment
import com.sola.anime.ai.generator.feature.art.comingsoon.ComingSoonFragment
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

@AndroidEntryPoint
class ArtActivity : LsActivity<ActivityArtBinding>(ActivityArtBinding::inflate) {

    companion object {
        const val EXPLORE_ID_EXTRA = "EXPLORE_ID_EXTRA"
    }

    @Inject lateinit var prefs: Preferences

    private val exploreId by lazy { intent.getLongExtra(EXPLORE_ID_EXTRA, -1) }
    private val artFragment by lazy { ArtFragment() }
//    private val comingSoonFragment by lazy { ComingSoonFragment() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initView()
        initObservable()
        initData()
        listenerView()
    }

    private fun listenerView() {
        binding.back.clicks { onBackPressed() }
        binding.viewPro.clicks(withAnim = false) { startIap() }
        binding.viewCredit.clicks { startCredit() }
    }

    private fun initData() {

    }

    private fun initObservable() {
        prefs
            .isUpgraded
            .asObservable()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribe { isUpgraded ->
                binding.viewPro.isVisible = !isUpgraded
            }
    }

    private fun initView() {
        binding.viewPager.apply {
            this.adapter = LsPageAdapter(supportFragmentManager).apply {
                this.addFragment(fragment = artFragment, title = "Generate Image")
//                this.addFragment(fragment = comingSoonFragment, title = "Coming Soon")
            }
            this.offscreenPageLimit = this.adapter?.count ?: 1
        }
        binding.tabLayout.apply {
            this.setupWithViewPager(binding.viewPager)
        }
    }

    @Deprecated("Deprecated in Java", ReplaceWith("finish()"))
    override fun onBackPressed() {
        backTopToBottom()
    }

}