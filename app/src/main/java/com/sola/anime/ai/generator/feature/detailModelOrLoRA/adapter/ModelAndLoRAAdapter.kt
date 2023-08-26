package com.sola.anime.ai.generator.feature.detailModelOrLoRA.adapter

import android.content.Context
import androidx.constraintlayout.widget.ConstraintSet
import coil.load
import com.basic.common.base.LsAdapter
import com.basic.common.extension.clicks
import com.basic.common.extension.getColorCompat
import com.basic.common.extension.setTint
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.databinding.ItemModelOrLoraInDetailBinding
import com.sola.anime.ai.generator.domain.model.ExploreOrLoRA
import com.sola.anime.ai.generator.domain.model.ModelOrLoRA
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

class ModelAndLoRAAdapter @Inject constructor(
    private val context: Context
): LsAdapter<ModelOrLoRA, ItemModelOrLoraInDetailBinding>(ItemModelOrLoraInDetailBinding::inflate) {

    val clicks: Subject<ModelOrLoRA> = PublishSubject.create()
    val favouriteClicks: Subject<ModelOrLoRA> = PublishSubject.create()

    override fun bindItem(item: ModelOrLoRA, binding: ItemModelOrLoraInDetailBinding, position: Int) {
//        ConstraintSet().apply {
//            this.clone(binding.viewGroup)
//            this.setDimensionRatio(binding.viewClicks.id, item.ratio)
//            this.applyTo(binding.viewGroup)
//        }

        val preview = when {
            item.model != null -> item.model.preview
            item.loRA != null -> item.loRA.previews.firstOrNull()
            else -> null
        }
        binding.preview.load(preview) {
            crossfade(true)
            error(R.drawable.place_holder_image)
        }

        binding.viewDescription.setCardBackgroundColor(binding.root.context.getColorCompat(if (item.model != null) R.color.blue else if (item.loRA != null) R.color.red else com.widget.R.color.tools_theme))
        binding.description.text = if (item.model != null) "Model" else if (item.loRA != null) "LoRA" else ""
        binding.display.text = item.display
        binding.favouriteCount.text = "${if (item.isFavourite) (item.favouriteCount + 1) else item.favouriteCount} Uses"
        binding.favourite.setTint(context.getColorCompat(if (item.isFavourite) R.color.red else R.color.white))

        binding.favourite.clicks {
            item.model?.isFavourite = !(item.model?.isFavourite ?: false)
            item.loRA?.isFavourite = !(item.loRA?.isFavourite ?: false)
            item.isFavourite = !item.isFavourite
            binding.favourite.setTint(context.getColorCompat(if (item.isFavourite) R.color.red else R.color.white))

            favouriteClicks.onNext(item)
        }
        binding.viewClicks.clicks(withAnim = false) { clicks.onNext(item) }
    }

}