package com.sola.anime.ai.generator.feature.first

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.lifecycle.lifecycleScope
import com.basic.common.base.LsActivity
import com.basic.common.extension.clicks
import com.basic.common.extension.lightStatusBar
import com.basic.common.extension.transparent
import com.sola.anime.ai.generator.common.ConfigApp
import com.sola.anime.ai.generator.common.Navigator
import com.sola.anime.ai.generator.common.extension.back
import com.sola.anime.ai.generator.common.extension.makeLinks
import com.sola.anime.ai.generator.common.extension.startMain
import com.sola.anime.ai.generator.common.extension.startTutorial
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.databinding.ActivityFirstBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FirstActivity : LsActivity() {

    @Inject lateinit var configApp: ConfigApp
    @Inject lateinit var prefs: Preferences
    @Inject lateinit var navigator: Navigator

    private val binding by lazy { ActivityFirstBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transparent()
        lightStatusBar()
        setContentView(binding.root)

        initView()
        initObservable()
        initData()
        listenerView()
    }

    private fun listenerView() {
        binding.cardStart.clicks(withAnim = true){
            lifecycleScope.launch {
                delay(100)

                prefs.isFirstTime.set(false)
                when {
                    !prefs.isViewTutorial.get() -> startTutorial()
                    else -> startMain()
                }
                finish()
            }
        }
    }

    private fun initData() {

    }

    private fun initObservable() {

    }

    private fun initView() {
        binding.textPrivacy.makeLinks(
            "Privacy Policy" to View.OnClickListener {
                navigator.showPrivacy()
            },
            "Terms of Use" to View.OnClickListener {
                navigator.showTerms()
            }
        )

        listOf(
            binding.viewAnim1,
            binding.viewAnim2,
            binding.viewAnim3,
            binding.viewAnim4,
            binding.viewAnim5,
            binding.viewAnim6,
            binding.viewAnim7,
            binding.viewAnim8,
            binding.viewAnim9
        ).forEachIndexed { index, view ->
            view.animScale(index)
        }
    }

    private fun View.animScale(index: Int) {
        val animation = ObjectAnimator.ofFloat(0.95f, 1.05f)
        animation.apply {
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
            duration = 1000
            interpolator = AccelerateDecelerateInterpolator()
            startDelay = if (index % 2 == 0) 250 else 0
            addUpdateListener {
                val animatedValue = it.animatedValue as Float

                this@animScale.scaleX = animatedValue
                this@animScale.scaleY = animatedValue
            }
        }
        animation.start()
    }

    @Deprecated("Deprecated in Java", ReplaceWith("finish()"))
    override fun onBackPressed() {
        back()
    }

}