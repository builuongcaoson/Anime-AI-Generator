package com.sola.anime.ai.generator.feature.main.explore.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.RecyclerView
import com.basic.common.base.LsAdapter
import com.basic.common.base.LsViewHolder
import com.basic.common.extension.clicks
import com.basic.common.extension.getColorCompat
import com.basic.common.extension.setTint
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.common.extension.load
import com.sola.anime.ai.generator.databinding.ItemPreviewExploreBinding
import com.sola.anime.ai.generator.domain.model.config.explore.Explore
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

class ExploreAdapter @Inject constructor(
    private val context: Context
): LsAdapter<Explore, ItemPreviewExploreBinding>(ItemPreviewExploreBinding::inflate) {

    companion object {
        private const val EXPLORE_SIZE = 5
    }

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
    val clicks: Subject<Explore> = PublishSubject.create()
    val favouriteClicks: Subject<Explore> = PublishSubject.create()

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
        ConstraintSet().apply {
            this.clone(binding.viewGroup)
            this.setDimensionRatio(binding.viewClicks.id, item.ratio)
            this.applyTo(binding.viewGroup)
        }

        binding.preview.load(item.previews.firstOrNull(), errorRes = R.drawable.place_holder_image)
        binding.favourite.setTint(context.getColorCompat(if (item.isFavourite) R.color.yellow else R.color.white))
        binding.prompt.text = item.prompt

        binding.favourite.clicks {
            item.isFavourite = !item.isFavourite
            binding.favourite.setTint(context.getColorCompat(if (item.isFavourite) R.color.yellow else R.color.white))

            favouriteClicks.onNext(item)
        }
        binding.viewClicks.clicks(withAnim = false) { clicks.onNext(item) }
    }

}