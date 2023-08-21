package com.sola.anime.ai.generator.feature.detailExplore

import android.os.Bundle
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import coil.load
import com.basic.common.base.LsActivity
import com.basic.common.extension.*
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.common.extension.back
import com.sola.anime.ai.generator.common.extension.startDetailExplore
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.data.db.query.ExploreDao
import com.sola.anime.ai.generator.databinding.ActivityDetailExploreBinding
import com.sola.anime.ai.generator.domain.model.ExplorePreview
import com.sola.anime.ai.generator.domain.model.config.explore.Explore
import com.sola.anime.ai.generator.feature.detailExplore.adapter.ExplorePreviewAdapter
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class DetailExploreActivity : LsActivity<ActivityDetailExploreBinding>(ActivityDetailExploreBinding::inflate) {

    companion object {
        const val EXPLORE_ID_EXTRA = "EXPLORE_ID_EXTRA"
        const val PREVIEW_INDEX_EXTRA = "PREVIEW_INDEX_EXTRA"
    }

    @Inject lateinit var prefs: Preferences
    @Inject lateinit var explorePreviewAdapter: ExplorePreviewAdapter
    @Inject lateinit var exploreDao: ExploreDao

    private val subjectDataExploreChanges: Subject<List<ExplorePreview>> = PublishSubject.create()

    private val exploreId by lazy { intent.getLongExtra(EXPLORE_ID_EXTRA, -1) }
    private val previewIndex by lazy { intent.getIntExtra(PREVIEW_INDEX_EXTRA, 0) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transparent()
        setContentView(binding.root)

        initView()
        initObservable()
        initData()
        listenerView()
    }

    private fun listenerView() {
        binding.back.clicks { onBackPressed() }
        binding.save.clicks {  }
        binding.dislike.clicks {  }
        binding.report.clicks {  }
    }

    private fun initData() {

    }

    private fun initObservable() {
        subjectDataExploreChanges
            .debounce(500, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribe { explores ->
                Timber.e("Data size: ${explores.size}")

                lifecycleScope.launch(Dispatchers.Main) {
                    explorePreviewAdapter.data = explores.shuffled()
                    delay(500)
                    binding.loadingExplore.animate().alpha(0f).setDuration(250).start()
                    binding.recyclerExplore.animate().alpha(1f).setDuration(250).start()
                }
            }

        explorePreviewAdapter
            .clicks
            .autoDispose(scope())
            .subscribe { explorePreview ->
                startDetailExplore(exploreId = exploreId, previewIndex = explorePreview.previewIndex)
                finish()
            }
    }

    private fun initView() {
        when {
            exploreId != -1L -> initExploreView()
            else -> {
                lifecycleScope.launch(Dispatchers.Main) {
                    delay(1000)

                    makeToast("Something wrong, please try again!")
                    finish()
                }
                return
            }
        }

        binding.recyclerExplore.adapter = explorePreviewAdapter
    }

    private fun initExploreView() {
        exploreDao.findById(exploreId)?.let { explore ->
            ConstraintSet().apply {
                this.clone(binding.viewPreview)
                this.setDimensionRatio(binding.preview.id, explore.ratio)
                this.applyTo(binding.viewPreview)
            }

            binding.preview.load(explore.previews.getOrNull(previewIndex)) {
                listener(
                    onSuccess = { _, result ->
                        binding.preview.setImageDrawable(result.drawable)
                        binding.preview.animate().alpha(1f).setDuration(250).start()
                        binding.viewShadow.animate().alpha(1f).setDuration(250).start()
                    }
                )
                crossfade(true)
                error(R.drawable.place_holder_image)
            }

            binding.viewDetail.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                this.bottomMargin = getDimens(com.intuit.sdp.R.dimen._70sdp).toInt()
            }

            binding.prompt.text = explore.prompt
            val favouriteCount = if (explore.isFavourite) explore.favouriteCount + 1 else explore.favouriteCount
            binding.favouriteCount.text = "$favouriteCount Uses"

            initExploreData(explore = explore)
        }
    }

    private fun initExploreData(explore: Explore) {
        lifecycleScope.launch(Dispatchers.Main) {
            delay(500)
            subjectDataExploreChanges.onNext(explore.previews.filterIndexed { index, _ -> index != previewIndex  }.mapIndexed { index, preview -> ExplorePreview(exploreId = explore.id, previewIndex = index, preview = preview, ratio = explore.ratio) })
        }
    }

    @Deprecated("Deprecated in Java", ReplaceWith("finish()"))
    override fun onBackPressed() {
        back()
    }

}