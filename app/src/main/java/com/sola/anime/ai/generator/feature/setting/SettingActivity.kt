package com.sola.anime.ai.generator.feature.setting

import android.os.Bundle
import androidx.core.view.isVisible
import com.basic.common.base.LsActivity
import com.basic.common.extension.clicks
import com.sola.anime.ai.generator.common.Navigator
import com.sola.anime.ai.generator.common.extension.back
import com.sola.anime.ai.generator.common.extension.startIap
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.databinding.ActivitySettingBinding
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

@AndroidEntryPoint
class SettingActivity : LsActivity<ActivitySettingBinding>(ActivitySettingBinding::inflate) {

    @Inject lateinit var navigator: Navigator
    @Inject lateinit var prefs: Preferences

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
        binding.viewGetPremium.clicks(withAnim = false) { startIap() }
        binding.viewSupport.clicks(withAnim = false) { navigator.showSupport() }
        binding.viewShare.clicks(withAnim = false) { navigator.showInvite() }
        binding.viewRate.clicks(withAnim = false) { navigator.showRating() }
        binding.viewPrivacy.clicks(withAnim = false) { navigator.showPrivacy() }
        binding.viewTerms.clicks(withAnim = false) { navigator.showTerms() }
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
                binding.viewPremium.isVisible = !isUpgraded
            }
    }

    private fun initView() {

    }

    @Deprecated("Deprecated in Java", ReplaceWith("finish()"))
    override fun onBackPressed() {
        back()
    }

}