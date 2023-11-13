package com.sola.anime.ai.generator.feature.loading

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.basic.common.base.LsActivity
import com.sola.anime.ai.generator.common.App
import com.sola.anime.ai.generator.common.extension.back
import com.sola.anime.ai.generator.databinding.ActivityLoadingBinding
import com.sola.anime.ai.generator.domain.manager.AdmobManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LoadingActivity : LsActivity<ActivityLoadingBinding>(ActivityLoadingBinding::inflate) {

    @Inject lateinit var admobManager: AdmobManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initView()
        initObservable()
        initData()
        listenerView()
    }

    private fun listenerView() {

    }

    private fun initData() {
        admobManager.loadAndShowFullItem(this) {
            lifecycleScope.launch(Dispatchers.Main) {
                delay(250L)
                App.app.actionAfterFullItem()
                delay(250L)
                back()
            }
        }
    }

    private fun initObservable() {

    }

    private fun initView() {

    }

    @Deprecated("Deprecated in Java", ReplaceWith("finish()"))
    override fun onBackPressed() {

    }

}