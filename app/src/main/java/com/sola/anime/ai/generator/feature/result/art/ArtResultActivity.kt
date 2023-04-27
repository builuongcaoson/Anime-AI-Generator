package com.sola.anime.ai.generator.feature.result.art

import android.graphics.Bitmap
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.basic.common.base.LsActivity
import com.basic.common.extension.clicks
import com.basic.common.extension.getDimens
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.common.extension.back
import com.sola.anime.ai.generator.common.extension.startIap
import com.sola.anime.ai.generator.data.db.query.HistoryDao
import com.sola.anime.ai.generator.databinding.ActivityArtResultBinding
import com.sola.anime.ai.generator.domain.model.history.ChildHistory
import com.sola.anime.ai.generator.feature.result.art.adapter.Preview2Adapter
import com.sola.anime.ai.generator.feature.result.art.adapter.PreviewAdapter
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class ArtResultActivity : LsActivity() {

    companion object {
        const val HISTORY_ID_EXTRA = "HISTORY_ID_EXTRA"
        const val CHILD_HISTORY_ID_EXTRA = "CHILD_HISTORY_ID_EXTRA"
    }

    @Inject lateinit var previewAdapter: PreviewAdapter
    @Inject lateinit var preview2Adapter: Preview2Adapter
    @Inject lateinit var historyDao: HistoryDao

    private val subjectPageChanges: Subject<ChildHistory> = PublishSubject.create()

    private val binding by lazy { ActivityArtResultBinding.inflate(layoutInflater) }
    private val historyId by lazy { intent.getLongExtra(HISTORY_ID_EXTRA, -1L) }
    private val childHistoryId by lazy { intent.getLongExtra(CHILD_HISTORY_ID_EXTRA, -1L) }
    private var childHistories = arrayListOf<ChildHistory>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        when (historyId) {
            -1L -> {
                finish()
                return
            }
        }

        initView()
        initObservable()
        initData()
        listenerView()
    }

    private fun listenerView() {
        binding.back.clicks { onBackPressed() }
        binding.viewPro.clicks { startIap() }
        binding.viewPager.registerOnPageChangeCallback(pageChanges)
    }

    override fun onDestroy() {
        binding.viewPager.unregisterOnPageChangeCallback(pageChanges)
        super.onDestroy()
    }

    private val pageChanges = object: ViewPager2.OnPageChangeCallback() {
        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
            childHistories.getOrNull(position)?.let { childHistory -> subjectPageChanges.onNext(childHistory) }
        }
    }

    private fun initData() {
        historyDao.getWithIdLive(id = historyId).observe(this){ history ->
            history?.let {
                childHistories = ArrayList(history.childs)

                previewAdapter.apply {
                    this.data = history.childs
                    this.childHistory = history.childs.lastOrNull()
                }
                preview2Adapter.data = history.childs
            }
        }
    }

    private fun initObservable() {
        subjectPageChanges
            .debounce(500, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribe {
                previewAdapter.childHistory = it
            }
    }

    private fun initView() {
        binding.viewPager.apply {
            this.adapter = preview2Adapter
        }
        binding.recyclerPreview.apply {
            this.layoutManager = LinearLayoutManager(this@ArtResultActivity, LinearLayoutManager.HORIZONTAL, false)
            this.adapter = previewAdapter
        }
    }

    @Deprecated("Deprecated in Java", ReplaceWith("finish()"))
    override fun onBackPressed() {
        back()
    }

}