package com.sola.anime.ai.generator.feature.result.art

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.basic.common.base.LsActivity
import com.basic.common.extension.clicks
import com.basic.common.extension.isNetworkAvailable
import com.basic.common.extension.makeToast
import com.basic.common.extension.tryOrNull
import com.sola.anime.ai.generator.common.ConfigApp
import com.sola.anime.ai.generator.common.Constraint
import com.sola.anime.ai.generator.common.extension.*
import com.sola.anime.ai.generator.common.ui.dialog.NetworkDialog
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.data.db.query.HistoryDao
import com.sola.anime.ai.generator.data.db.query.StyleDao
import com.sola.anime.ai.generator.databinding.ActivityArtResultBinding
import com.sola.anime.ai.generator.domain.manager.AdmobManager
import com.sola.anime.ai.generator.domain.model.Ratio
import com.sola.anime.ai.generator.domain.model.history.ChildHistory
import com.sola.anime.ai.generator.domain.repo.FileRepository
import com.sola.anime.ai.generator.feature.result.art.adapter.PagePreviewAdapter
import com.sola.anime.ai.generator.feature.result.art.adapter.PreviewAdapter
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class ArtResultActivity : LsActivity<ActivityArtResultBinding>(ActivityArtResultBinding::inflate) {

    companion object {
        const val HISTORY_ID_EXTRA = "HISTORY_ID_EXTRA"
        const val CHILD_HISTORY_INDEX_EXTRA = "CHILD_HISTORY_INDEX_EXTRA"
    }

    @Inject lateinit var previewAdapter: PreviewAdapter
    @Inject lateinit var pagePreviewAdapter: PagePreviewAdapter
    @Inject lateinit var historyDao: HistoryDao
    @Inject lateinit var styleDao: StyleDao
    @Inject lateinit var fileRepo: FileRepository
    @Inject lateinit var configApp: ConfigApp
    @Inject lateinit var admobManager: AdmobManager
    @Inject lateinit var prefs: Preferences
    @Inject lateinit var networkDialog: NetworkDialog

    private val subjectPageChanges: Subject<ChildHistory> = PublishSubject.create()

    private val historyId by lazy { intent.getLongExtra(HISTORY_ID_EXTRA, -1L) }
    private val childHistoryIndex by lazy { intent.getIntExtra(CHILD_HISTORY_INDEX_EXTRA, -1) }
    private var childHistories = arrayListOf<ChildHistory>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        when {
            !prefs.isUpgraded.get() -> admobManager.loadRewardCreateAgain()
        }

        when (historyId) {
            -1L -> {
                finish()
                return
            }
        }

        prefs.numberCreatedArtwork.set(prefs.numberCreatedArtwork.get() + 1)
        prefs.latestTimeCreatedArtwork.set(System.currentTimeMillis())

        initView()
        initObservable()
        initData()
        listenerView()
    }

    private fun listenerView() {
        binding.back.clicks { onBackPressed() }
        binding.viewPro.clicks { startIap() }
        binding.viewPager.registerOnPageChangeCallback(pageChanges)
        binding.cardShare.clicks { tryOrNull { shareClicks() } }
        binding.cardDownload.clicks { tryOrNull { downloadClicks() } }
        binding.cardGenerate.clicks { generateAgainClicks() }
    }

    private fun generateAgainClicks() {
        val history = historyDao.findById(historyId) ?: return

        val task = {
            configApp.dezgoBodiesTextsToImages = initDezgoBodyTextsToImages(
                maxGroupId = 0,
                maxChildId = 0,
                prompt = history.prompt,
                negativePrompt = history.childs.firstOrNull()?.negativePrompt?.takeIf { it.isNotEmpty() } ?: Constraint.Dezgo.DEFAULT_NEGATIVE,
                guidance = history.childs.firstOrNull()?.guidance ?: "7.5",
                steps = if (!prefs.isUpgraded.get()) "75" else "100",
                styleId = history.styleId,
                ratio = Ratio.values().firstOrNull {
                    it.width == (history.childs.firstOrNull()?.width ?: "") && it.height == (history.childs.firstOrNull()?.height ?: "")
                } ?: Ratio.Ratio1x1,
                seed = (0..4294967295).random()
            )

            startArtProcessing()
            finish()
        }

        when {
            !isNetworkAvailable() -> networkDialog.show(this)
            prefs.numberCreatedArtwork.get() >= Preferences.MAX_NUMBER_CREATE_ARTWORK && !prefs.isUpgraded.get() -> {
                startIap()
            }
            !prefs.isUpgraded.get() -> {
                admobManager.showRewardCreateAgain(
                    this,
                    success = {
                        task()
                        admobManager.loadRewardCreateAgain()
                    },
                    failed = {
                        makeToast("Please watch all ads to perform the function!")
                        admobManager.loadRewardCreateAgain()
                    }
                )
            }
            else -> task()
        }
    }

    private fun downloadClicks() {
        previewAdapter.childHistory?.let { childHistory ->
            lifecycleScope.launch {
                fileRepo.downloads(File(childHistory.pathPreview))
                withContext(Dispatchers.Main){
                    makeToast("Download successfully!")
                }
            }
        }
    }

    private fun shareClicks() {
        previewAdapter.childHistory?.let { childHistory ->
            lifecycleScope.launch {
                fileRepo.shares(File(childHistory.pathPreview))
            }
        }
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
                binding.displayStyle.text = styleDao.findById(history.styleId)?.display ?: "No Style"
                binding.textPrompt.text = history.prompt

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
//            .debounce(50, TimeUnit.MILLISECONDS)
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribeOn(AndroidSchedulers.mainThread())
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

        pagePreviewAdapter
            .clicks
            .map { previewAdapter.data.indexOf(it) }
            .filter { it != -1 }
            .autoDispose(scope())
            .subscribe { index ->
                startPreview(historyId = historyId, childHistoryIndex = index)
            }

        prefs
            .isUpgraded
            .asObservable()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribe { isUpgraded ->
                binding.viewPro.isVisible = !isUpgraded
            }
    }

    private fun initView() {
        binding.viewPager.apply {
            this.adapter = pagePreviewAdapter
        }
        binding.recyclerPreview.apply {
            this.layoutManager = LinearLayoutManager(this@ArtResultActivity, LinearLayoutManager.HORIZONTAL, false)
            this.adapter = previewAdapter
        }
        binding.textPrompt.movementMethod = ScrollingMovementMethod()
    }

    @Deprecated("Deprecated in Java", ReplaceWith("finish()"))
    override fun onBackPressed() {
        back()
    }

}