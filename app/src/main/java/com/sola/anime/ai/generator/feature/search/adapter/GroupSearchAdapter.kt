package com.sola.anime.ai.generator.feature.search.adapter

import com.basic.common.base.LsAdapter
import com.sola.anime.ai.generator.common.ui.sheet.explore.adapter.ExploreAdapter
import com.sola.anime.ai.generator.common.ui.sheet.loRA.adapter.LoRAAdapter
import com.sola.anime.ai.generator.common.ui.sheet.model.adapter.ModelAdapter
import com.sola.anime.ai.generator.databinding.ItemGroupSearchBinding
import com.sola.anime.ai.generator.domain.model.LoRAPreview
import com.sola.anime.ai.generator.domain.model.config.explore.Explore
import com.sola.anime.ai.generator.domain.model.config.model.Model
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

sealed class GroupSearch(val display: String) {
    data class Models(val items: List<Model>): GroupSearch(display = "Models")
    data class LoRAs(val items: List<LoRAPreview>): GroupSearch(display = "LoRAs")
    data class Explores(val items: List<Explore>): GroupSearch(display = "Explores")
}

class GroupSearchAdapter @Inject constructor(): LsAdapter<GroupSearch, ItemGroupSearchBinding>(ItemGroupSearchBinding::inflate) {

    val modelClicks: Subject<Model> = PublishSubject.create()
    val detailModelClicks: Subject<Model> = PublishSubject.create()
    val loRAClicks: Subject<LoRAPreview> = PublishSubject.create()
    val detailLoRAClicks: Subject<LoRAPreview> = PublishSubject.create()
    val exploreCLicks: Subject<Explore> = PublishSubject.create()
    val detailExploreClicks: Subject<Explore> = PublishSubject.create()

    override fun bindItem(item: GroupSearch, binding: ItemGroupSearchBinding, position: Int) {
        binding.display.text = item.display

        when {
            item is GroupSearch.Models -> {
                binding.recyclerChildSearch.apply {
                    this.adapter = ModelAdapter().apply {
                        this.clicks = this@GroupSearchAdapter.modelClicks
                        this.detailsClicks = this@GroupSearchAdapter.detailModelClicks
                        this.data = item.items
                    }
                }
            }
            item is GroupSearch.LoRAs -> {
                binding.recyclerChildSearch.apply {
                    this.adapter = LoRAAdapter().apply {
                        this.clicks = this@GroupSearchAdapter.loRAClicks
                        this.detailsClicks = this@GroupSearchAdapter.detailLoRAClicks
                        this.data = item.items
                    }
                }
            }
            item is GroupSearch.Explores -> {
                binding.recyclerChildSearch.apply {
                    this.adapter = ExploreAdapter().apply {
                        this.clicks = this@GroupSearchAdapter.exploreCLicks
                        this.detailsClicks = this@GroupSearchAdapter.detailExploreClicks
                        this.data = item.items
                    }
                }
            }
        }
    }

}