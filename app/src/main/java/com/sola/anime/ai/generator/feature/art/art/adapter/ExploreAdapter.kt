package com.sola.anime.ai.generator.feature.art.art.adapter

import android.graphics.drawable.Drawable
import androidx.constraintlayout.widget.ConstraintSet
import com.basic.common.base.LsAdapter
import com.basic.common.extension.clicks
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.common.extension.load
import com.sola.anime.ai.generator.databinding.ItemPreviewExploreBinding
import com.sola.anime.ai.generator.domain.model.config.explore.Explore
import com.sola.anime.ai.generator.feature.main.explore.adapter.ExploreAdapter
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

class ExploreAdapter @Inject constructor(): LsAdapter<Explore, ItemPreviewExploreBinding>(ItemPreviewExploreBinding::inflate) {

    companion object {
        private const val EXPLORE_SIZE = 5
    }

    val clicks: Subject<Explore> = PublishSubject.create()

    private var isLastPage = false
    var explores = listOf<Explore>()
        set(value) {
            val exploresDisplay = when (itemCount) {
                0 -> value.takeIf { it.size > EXPLORE_SIZE }?.subList(0, EXPLORE_SIZE) ?: listOf()
                else -> value.takeIf { it.size > itemCount }?.subList(0, itemCount) ?: listOf()
            }
            data = exploresDisplay

            field = value
        }
    val hashmapDrawable = hashMapOf<Int, Drawable?>()

    fun loadMore(){
        val startIndex = itemCount

        var endIndex = startIndex + EXPLORE_SIZE - 1
        if (endIndex >= explores.size) {
            endIndex = explores.size
        }

        if (!isLastPage){
            data = ArrayList(data).apply {
                this.addAll(explores.subList(startIndex, endIndex))
            }
        }
        isLastPage = endIndex == explores.size
    }

    override fun bindItem(item: Explore, binding: ItemPreviewExploreBinding, position: Int) {
        val set = ConstraintSet()
        set.clone(binding.viewGroup)
        set.setDimensionRatio(binding.viewClicks.id, item.ratio)
        set.applyTo(binding.viewGroup)

        hashmapDrawable[position]?.let { drawable ->
            binding.preview.setImageDrawable(drawable)
        } ?: run {
            binding.preview.load(item.previews.firstOrNull(), errorRes = R.drawable.place_holder_image) { drawable ->
                hashmapDrawable[position] = drawable
            }
        }
        binding.prompt.text = item.prompt

        binding.viewClicks.clicks(withAnim = false) { clicks.onNext(item) }
    }

}