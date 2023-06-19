package com.sola.anime.ai.generator.feature.result.art.adapter

import androidx.constraintlayout.widget.ConstraintSet
import com.basic.common.base.LsAdapter
import com.basic.common.extension.clicks
import com.basic.common.extension.tryOrNull
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.databinding.ItemPreview2ArtResultBinding
import com.sola.anime.ai.generator.domain.model.history.ChildHistory
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

class PagePreviewAdapter @Inject constructor() : LsAdapter<ChildHistory, ItemPreview2ArtResultBinding>(ItemPreview2ArtResultBinding::inflate) {

    val clicks: Subject<ChildHistory> = PublishSubject.create()

    override fun bindItem(
        item: ChildHistory,
        binding: ItemPreview2ArtResultBinding,
        position: Int
    ) {
        val context = binding.root.context

        val ratio = "${item.width}:${item.height}"

        tryOrNull {
            val set = ConstraintSet()
            set.clone(binding.viewGroup)
            set.setDimensionRatio(binding.cardPreview.id, ratio)
            set.applyTo(binding.viewGroup)
        }

        tryOrNull {
            Glide
                .with(context)
                .load(item.pathPreview)
                .centerCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
//            .placeholder(R.drawable.place_holder_image)
                .error(R.drawable.place_holder_image)
//            .listener(object: RequestListener<Drawable> {
//                override fun onLoadFailed(
//                    e: GlideException?,
//                    model: Any?,
//                    target: Target<Drawable>?,
//                    isFirstResource: Boolean
//                ): Boolean {
//                    binding.cardPreview.cardElevation = 0f
//                    binding.preview.setImageResource(R.drawable.place_holder_image)
//                    binding.preview.animate().alpha(0f).setDuration(100).start()
//                    return false
//                }
//
//                override fun onResourceReady(
//                    resource: Drawable?,
//                    model: Any?,
//                    target: Target<Drawable>?,
//                    dataSource: DataSource?,
//                    isFirstResource: Boolean
//                ): Boolean {
//                    resource?.let {
//                        binding.cardPreview.cardElevation = context.getDimens(com.intuit.sdp.R.dimen._3sdp)
//                        binding.preview.setImageDrawable(resource)
//                        binding.preview.animate().alpha(1f).setDuration(100).start()
//                    } ?: run {
//                        binding.cardPreview.cardElevation = 0f
//                        binding.preview.setImageResource(R.drawable.place_holder_image)
//                        binding.preview.animate().alpha(0f).setDuration(100).start()
//                    }
//                    return false
//                }
//            })
                .into(binding.preview)
        }

        binding.cardPreview.clicks(withAnim = false){ clicks.onNext(item) }
    }

}