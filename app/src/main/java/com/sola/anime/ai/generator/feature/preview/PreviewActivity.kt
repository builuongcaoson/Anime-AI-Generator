package com.sola.anime.ai.generator.feature.preview

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.basic.common.base.LsActivity
import com.basic.common.extension.clicks
import com.sola.anime.ai.generator.common.extension.back
import com.sola.anime.ai.generator.data.db.query.HistoryDao
import com.sola.anime.ai.generator.databinding.ActivityPreviewBinding
import com.sola.anime.ai.generator.domain.model.history.ChildHistory
import com.sola.anime.ai.generator.feature.preview.adapter.PagePreviewAdapter
import com.sola.anime.ai.generator.feature.result.art.ArtResultActivity
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
class PreviewActivity : LsActivity() {

    @Inject lateinit var previewAdapter: PreviewAdapter
    @Inject lateinit var pagePreviewAdapter: PagePreviewAdapter
    @Inject lateinit var historyDao: HistoryDao

    private val subjectPageChanges: Subject<ChildHistory> = PublishSubject.create()

    private val binding by lazy { ActivityPreviewBinding.inflate(layoutInflater) }
    private val historyId by lazy { intent.getLongExtra(ArtResultActivity.HISTORY_ID_EXTRA, -1L) }
    private val childHistoryIndex by lazy { intent.getIntExtra(ArtResultActivity.CHILD_HISTORY_INDEX_EXTRA, -1) }
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
                }
                pagePreviewAdapter.apply {
                    this.data = history.childs
                }
                childHistoryIndex.takeIf { it != -1 }?.let {
                    binding.viewPager.post {
                        binding.viewPager.setCurrentItem(childHistoryIndex, false)
                    }
                } ?: run {
                    binding.viewPager.post {
                        binding.viewPager.setCurrentItem(history.childs.lastIndex, false)
                    }
                }
            }
        }
    }

    private fun initObservable() {
        subjectPageChanges
            .debounce(100, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribe {
                previewAdapter.childHistory = it
            }

        previewAdapter
            .clicks
            .map { previewAdapter.data.indexOf(it) }
            .filter { it != -1 }
            .autoDispose(scope())
            .subscribe { index ->
                binding.viewPager.post {
                    binding.viewPager.currentItem = index
                }
            }
    }

    private fun initView() {
        binding.viewPager.apply {
            this.adapter = pagePreviewAdapter
        }
        binding.recyclerPreview.apply {
            this.layoutManager = LinearLayoutManager(this@PreviewActivity, LinearLayoutManager.HORIZONTAL, false)
            this.adapter = previewAdapter
        }
    }

    @Deprecated("Deprecated in Java", ReplaceWith("finish()"))
    override fun onBackPressed() {
        back()
    }

}