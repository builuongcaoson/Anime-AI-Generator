package com.sola.anime.ai.generator.feature.credit

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.viewpager2.widget.ViewPager2
import com.basic.common.base.LsActivity
import com.basic.common.base.LsAdapter
import com.basic.common.extension.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.common.ConfigApp
import com.sola.anime.ai.generator.common.Constraint
import com.sola.anime.ai.generator.common.extension.backTopToBottom
import com.sola.anime.ai.generator.common.extension.startIap
import com.sola.anime.ai.generator.common.extension.startMain
import com.sola.anime.ai.generator.common.ui.dialog.FeatureDialog
import com.sola.anime.ai.generator.common.ui.dialog.NetworkDialog
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.data.db.query.IAPDao
import com.sola.anime.ai.generator.databinding.ActivityCreditBinding
import com.sola.anime.ai.generator.databinding.ItemPreviewCreditBinding
import com.sola.anime.ai.generator.domain.repo.ServerApiRepository
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class CreditActivity : LsActivity<ActivityCreditBinding>(ActivityCreditBinding::inflate) {

    companion object {
        const val IS_KILL_EXTRA = "IS_KILL_EXTRA"
    }

    @Inject lateinit var prefs: Preferences
    @Inject lateinit var featureDialog: FeatureDialog
    @Inject lateinit var networkDialog: NetworkDialog
    @Inject lateinit var serverApiRepo: ServerApiRepository
    @Inject lateinit var previewAdapter: PreviewAdapter

    private val isKill by lazy { intent.getBooleanExtra(IS_KILL_EXTRA, true) }
    private val subjectSkuChoice: Subject<String> = BehaviorSubject.createDefault(Constraint.Iap.SKU_LIFE_TIME)

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
        binding.back.clicks { onBackPressed() }
        binding.viewPremium.clicks { startIap() }
    }

    override fun onResume() {
        binding.viewPager.registerOnPageChangeCallback(pageChanges)
        initPreviewView()
        super.onResume()
    }

    private fun initPreviewView() {
        Observable
            .interval(2, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribe {
                when {
                    binding.viewPager.currentItem == previewAdapter.data.lastIndex -> binding.viewPager.currentItem = 0
                    else -> binding.viewPager.currentItem = binding.viewPager.currentItem + 1
                }
            }
    }

    override fun onPause() {
        binding.viewPager.unregisterOnPageChangeCallback(pageChanges)
        super.onPause()
    }

    private val pageChanges = object: ViewPager2.OnPageChangeCallback() {
        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
            binding.pageIndicatorView.setSelected(position)
        }
    }

    private fun initView() {
        binding.viewPager.apply {
            this.adapter = previewAdapter
            this.offscreenPageLimit = previewAdapter.data.size
            this.isUserInputEnabled = false
        }
        binding.pageIndicatorView.apply {
            this.count = previewAdapter.data.size
        }
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

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (!isKill){
            startMain()
            finish()
        } else {
            backTopToBottom()
        }
    }

    class PreviewAdapter @Inject constructor(): LsAdapter<Int, ItemPreviewCreditBinding>(ItemPreviewCreditBinding::inflate){

        init {
            data = listOf(
                R.drawable.preview_credit_1,
                R.drawable.preview_credit_2,
                R.drawable.preview_credit_3,
                R.drawable.preview_credit_4,
                R.drawable.preview_credit_5
            )
        }

        override fun bindItem(item: Int, binding: ItemPreviewCreditBinding, position: Int) {
            Glide.with(binding.root)
                .load(item)
                .centerCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(binding.preview)
        }

    }

}