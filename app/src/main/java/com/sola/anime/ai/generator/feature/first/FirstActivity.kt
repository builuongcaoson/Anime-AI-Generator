package com.sola.anime.ai.generator.feature.first

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.graphics.Path
import android.graphics.RectF
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.util.toRange
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.basic.common.base.LsActivity
import com.basic.common.extension.clicks
import com.basic.common.extension.lightStatusBar
import com.basic.common.extension.transparent
import com.basic.common.extension.tryOrNull
import com.sola.anime.ai.generator.common.ConfigApp
import com.sola.anime.ai.generator.common.Navigator
import com.sola.anime.ai.generator.common.extension.back
import com.sola.anime.ai.generator.common.extension.makeLinks
import com.sola.anime.ai.generator.common.extension.startMain
import com.sola.anime.ai.generator.common.extension.startTutorial
import com.sola.anime.ai.generator.common.util.AutoScrollLayoutManager
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.databinding.ActivityFirstBinding
import com.sola.anime.ai.generator.feature.first.adapter.PreviewAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.sin

@AndroidEntryPoint
class FirstActivity : LsActivity() {

    @Inject lateinit var configApp: ConfigApp
    @Inject lateinit var prefs: Preferences
    @Inject lateinit var previewAdapter1: PreviewAdapter
    @Inject lateinit var previewAdapter2: PreviewAdapter
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
            prefs.isFirstTime.set(false)
            when {
                !prefs.isViewTutorial.get() -> startTutorial()
                else -> startMain()
            }
            finish()
        }
    }

    private fun initData() {
        val dataAfterChunked = configApp.firstPreviews.chunked(configApp.firstPreviews.size / 2)

        previewAdapter1.let { adapter ->
            adapter.data = dataAfterChunked.getOrNull(0) ?: listOf()
            adapter.totalCount = adapter.data.size
            binding.recyclerView1.apply {
                this.post { this.smoothScrollToPosition(adapter.data.size - 1) }
            }
        }

        previewAdapter2.let { adapter ->
            adapter.data = dataAfterChunked.getOrNull(1) ?: listOf()
            adapter.totalCount = adapter.data.size
            binding.recyclerView2.apply {
                this.post { this.smoothScrollToPosition(adapter.data.size - 1) }
            }
        }
    }

    override fun onResume() {
        registerScrollListener()
        super.onResume()
    }

    override fun onDestroy() {
        unregisterScrollListener()
        super.onDestroy()
    }

    private val scrollListener = object: RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            tryOrNull {
                val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return@tryOrNull

                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val pastVisibleItems = layoutManager.findLastVisibleItemPosition()

                tryOrNull {
                    if (pastVisibleItems + visibleItemCount >= totalItemCount - 2) {
                        when (recyclerView) {
                            binding.recyclerView1 -> {
                                tryOrNull { recyclerView.post { previewAdapter1.insert() } }
                            }
                            binding.recyclerView2 -> {
                                tryOrNull { recyclerView.post { previewAdapter2.insert() } }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun registerScrollListener(){
        binding.recyclerView1.addOnScrollListener(scrollListener)
        binding.recyclerView2.addOnScrollListener(scrollListener)
    }

    private fun unregisterScrollListener(){
        binding.recyclerView1.removeOnScrollListener(scrollListener)
        binding.recyclerView2.removeOnScrollListener(scrollListener)
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
        binding.recyclerView1.apply {
            this.layoutManager = AutoScrollLayoutManager(this@FirstActivity).apply {
                this.orientation = LinearLayoutManager.VERTICAL
            }
            this.adapter = previewAdapter1
        }
        binding.recyclerView2.apply {
            this.layoutManager =  AutoScrollLayoutManager(this@FirstActivity).apply {
                this.orientation = LinearLayoutManager.VERTICAL
                this.reverseLayout = true
            }
            this.adapter = previewAdapter2
        }

        binding.viewAnim1.animBubble()
        binding.viewAnim2.animBubble()
    }

    private fun View.animBubble(to: Float = (-40..40).random().toFloat()) {
        val animation = ObjectAnimator.ofFloat(0f, to)
        animation.apply {
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
            duration = 1000
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener {
                val animatedValue = it.animatedValue as Float

                this@animBubble.translationX = animatedValue
                this@animBubble.translationY = animatedValue
                this@animBubble.rotation = animatedValue / 10
            }
        }
        animation.start()
    }

    @Deprecated("Deprecated in Java", ReplaceWith("finish()"))
    override fun onBackPressed() {
        back()
    }

}