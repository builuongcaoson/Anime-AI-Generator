package com.sola.anime.ai.generator.feature.art

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.basic.common.base.LsActivity
import com.basic.common.base.LsPageAdapter
import com.basic.common.extension.clicks
import com.sola.anime.ai.generator.common.extension.backTopToBottom
import com.sola.anime.ai.generator.common.extension.startCredit
import com.sola.anime.ai.generator.common.extension.startIap
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.databinding.ActivityArtBinding
import com.sola.anime.ai.generator.domain.manager.AdmobManager
import com.sola.anime.ai.generator.domain.manager.PermissionManager
import com.sola.anime.ai.generator.feature.art.art.ArtFragment
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
class ArtActivity : LsActivity<ActivityArtBinding>(ActivityArtBinding::inflate) {

    companion object {
        const val MODEL_ID_EXTRA = "MODEL_ID_EXTRA"
        const val LORA_GROUP_ID_EXTRA = "LORA_GROUP_ID_EXTRA"
        const val LORA_ID_EXTRA = "LORA_ID_EXTRA"
        const val EXPLORE_ID_EXTRA = "EXPLORE_ID_EXTRA"
    }

    @Inject lateinit var prefs: Preferences
    @Inject lateinit var admobManager: AdmobManager
    @Inject lateinit var permissionManager: PermissionManager

    private val modelId by lazy { intent.getLongExtra(MODEL_ID_EXTRA, -1) }
    private val exploreId by lazy { intent.getLongExtra(EXPLORE_ID_EXTRA, -1) }
    private val loRAGroupId by lazy { intent.getLongExtra(LORA_GROUP_ID_EXTRA, -1) }
    private val loRAId by lazy { intent.getLongExtra(LORA_ID_EXTRA, -1) }

    private val artFragment by lazy { ArtFragment() }
//    private val comingSoonFragment by lazy { ComingSoonFragment() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        when {
            !prefs.isUpgraded.get() -> admobManager.loadReward()
        }

        when {
            !permissionManager.hasNotification() -> {
                lifecycleScope.launch(Dispatchers.Main) {
                    delay(1000L)

                    permissionManager.requestNotification(this@ArtActivity, 1001)
                }
            }
        }

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

        prefs
            .creditsChanges
            .asObservable()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribe {
                binding.credits.text = prefs.getCredits().roundToInt().toString()
            }

        Observable
            .timer(1, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribe {
                binding.viewCredit.animate().alpha(1f).setDuration(250L).start()
                binding.viewPro.animate().alpha(1f).setDuration(250L).start()
            }
    }

    private fun initView() {
        artFragment.modelId = modelId
        artFragment.loRAGroupId = loRAGroupId
        artFragment.loRAId = loRAId
        artFragment.exploreId = exploreId

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