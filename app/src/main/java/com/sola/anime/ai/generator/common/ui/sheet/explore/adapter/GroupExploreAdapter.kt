package com.sola.anime.ai.generator.common.ui.sheet.explore.adapter

import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import com.basic.common.base.LsAdapter
import com.basic.common.extension.clicks
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.common.extension.load
import com.sola.anime.ai.generator.databinding.ItemGroupExploreBinding
import com.sola.anime.ai.generator.databinding.ItemExploreBinding
import com.sola.anime.ai.generator.domain.model.config.explore.Explore
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

class GroupExploreAdapter @Inject constructor(): LsAdapter<Pair<String, List<Explore>>, ItemGroupExploreBinding>(ItemGroupExploreBinding::inflate) {

    val clicks: Subject<Explore> = PublishSubject.create()
    val detailsClicks: Subject<Explore> = PublishSubject.create()

    override fun bindItem(
        item: Pair<String, List<Explore>>,
        binding: ItemGroupExploreBinding,
        position: Int
    ) {
        binding.display.text = item.first
        binding.recyclerExplore.apply {
            this.adapter = ExploreAdapter().apply {
                this.clicks = this@GroupExploreAdapter.clicks
                this.detailsClicks = this@GroupExploreAdapter.detailsClicks
                this.data = item.second
                this.emptyView = binding.viewEmpty
            }
        }
    }

}

class ExploreAdapter: LsAdapter<Explore, ItemExploreBinding>(ItemExploreBinding::inflate){

    var clicks: Subject<Explore> = PublishSubject.create()
    var detailsClicks: Subject<Explore> = PublishSubject.create()
    var isShowedDetailView = true

    override fun bindItem(item: Explore, binding: ItemExploreBinding, position: Int) {
        ConstraintSet().apply {
            this.clone(binding.viewGroup)
            this.setDimensionRatio(binding.viewClicks.id, item.ratio)
            this.applyTo(binding.viewGroup)
        }

        binding.preview.load(item.previews.firstOrNull(), errorRes = R.drawable.place_holder_image)
        binding.prompt.text = item.prompt
        binding.viewDetails.isVisible = isShowedDetailView

        binding.viewClicks.clicks(withAnim = false) { clicks.onNext(item) }
        binding.viewDetails.clicks { detailsClicks.onNext(item) }
    }

}