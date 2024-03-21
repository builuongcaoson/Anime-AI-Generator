package com.sola.anime.ai.generator.feature.openad

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.basic.common.base.LsActivity
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.SingletonOpenManager
import com.sola.anime.ai.generator.common.extension.backTopToBottom
import com.sola.anime.ai.generator.databinding.ActivityOpenAdBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class OpenAdActivity : LsActivity<ActivityOpenAdBinding>(ActivityOpenAdBinding::inflate) {

    @Inject lateinit var singletonOpenManager: SingletonOpenManager

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

    }

    private fun initObservable() {

    }

    private fun initView() {
        lifecycleScope.launch {
            delay(1000L)
            singletonOpenManager.showAdIfAvailable(this@OpenAdActivity, getString(R.string.key_open_splash)) {
                backTopToBottom()
            }
        }
    }

    @Deprecated("Deprecated in Java", ReplaceWith("finish()"))
    override fun onBackPressed() {

    }

}