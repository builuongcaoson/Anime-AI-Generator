package com.sola.anime.ai.generator.feature.processing.art

import android.os.Bundle
import com.basic.common.base.LsActivity
import com.basic.common.extension.lightStatusBar
import com.basic.common.extension.makeToast
import com.basic.common.extension.transparent
import com.basic.common.extension.tryOrNull
import com.sola.anime.ai.generator.common.ConfigApp
import com.sola.anime.ai.generator.common.Navigator
import com.sola.anime.ai.generator.common.extension.setCurrentItem
import com.sola.anime.ai.generator.common.extension.startArtResult
import com.sola.anime.ai.generator.common.ui.ArtGenerateDialog
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.data.db.query.ArtProcessDao
import com.sola.anime.ai.generator.databinding.ActivityArtProcessingBinding
import com.sola.anime.ai.generator.feature.processing.art.adapter.PreviewAdapter
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposables
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class ArtProcessingActivity : LsActivity() {

    @Inject lateinit var previewAdapter: PreviewAdapter
    @Inject lateinit var configApp: ConfigApp
    @Inject lateinit var artGenerateDialog: ArtGenerateDialog
    @Inject lateinit var artProcessDao: ArtProcessDao

    private val binding by lazy { ActivityArtProcessingBinding.inflate(layoutInflater) }
    private var timeInterval = Disposables.empty()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transparent()
        lightStatusBar()
        setContentView(binding.root)

        initView()
        initObservable()
        initData()
    }

    override fun onResume() {
        startInterval()
        super.onResume()
    }

    override fun onStop() {
        stopInterval()
        super.onStop()
    }

    private fun startInterval(){
        timeInterval.dispose()
        timeInterval = Observable
            .interval(2, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribe { millisecond ->
                Timber.e("Milliseconds: $millisecond")
                when {
                    millisecond >= Preferences.MAX_SECOND_GENERATE_ART -> {
                        makeToast("An error occurred. Please try again!")
                        artGenerateDialog.dismiss()
                        finish()
                    }
                    millisecond >= 2 -> {
                        startArtResult()
                        finish()
                    }
                    else -> {
                        tryOrNull { binding.viewPager.post { previewAdapter.insert() } }
                        tryOrNull { binding.viewPager.setCurrentItem(item = binding.viewPager.currentItem + 1) }
                    }
                }
            }
    }

    private fun stopInterval(){
        timeInterval.dispose()
    }

    private fun initData() {
        artGenerateDialog.show(this)

        artProcessDao.getAllLive().observe(this){ artProcesses ->
            previewAdapter.apply {
                this.data = artProcesses
                this.totalCount = artProcesses.size
            }
        }
    }

    private fun initObservable() {

    }

    private fun initView() {
        binding.viewPager.apply {
            this.isUserInputEnabled = false
            this.adapter = previewAdapter
        }
    }

}