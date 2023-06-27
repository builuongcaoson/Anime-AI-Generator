package com.sola.anime.ai.generator.feature.main.art.adapter

import android.graphics.drawable.Drawable
import androidx.constraintlayout.widget.ConstraintSet
import com.basic.common.base.LsAdapter
import com.basic.common.extension.clicks
import com.basic.common.extension.getDimens
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.databinding.ItemPreviewExploreBinding
import com.sola.anime.ai.generator.domain.model.config.explore.Explore
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

class PreviewExploreAdapter @Inject constructor(): LsAdapter<Explore, ItemPreviewExploreBinding>(ItemPreviewExploreBinding::inflate) {

    val clicks: Subject<Explore> = PublishSubject.create()

    override fun bindItem(item: Explore, binding: ItemPreviewExploreBinding, position: Int) {
        val context = binding.root.context

        val set = ConstraintSet()
        set.clone(binding.viewGroup)
        set.setDimensionRatio(binding.viewPreview.id, item.ratio)
        set.applyTo(binding.viewGroup)

//        val layoutParams = binding.root.layoutParams as StaggeredGridLayoutManager.LayoutParams
//        layoutParams.isFullSpan = item.ratio == Ratio.Ratio16x9.ratio

        binding.prompt.text = item.prompt

        Glide
            .with(context)
            .load(item.preview)
            .error(R.drawable.place_holder_image)
            .placeholder(R.drawable.place_holder_image)
            .transition(DrawableTransitionOptions.withCrossFade())
            .listener(object: RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.viewPreview.cardElevation = 0f
                    binding.preview.setImageResource(R.drawable.place_holder_image)
                    binding.preview.animate().alpha(0f).setDuration(100).start()
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    resource?.let {
                        binding.viewPreview.cardElevation = context.getDimens(com.intuit.sdp.R.dimen._2sdp)
                        binding.preview.setImageDrawable(resource)
                        binding.preview.animate().alpha(1f).setDuration(100).start()
                    } ?: run {
                        binding.viewPreview.cardElevation = 0f
                        binding.preview.setImageResource(R.drawable.place_holder_image)
                        binding.preview.animate().alpha(0f).setDuration(100).start()
                    }
                    return false
                }

            })
            .into(binding.preview)

        binding.viewPreview.clicks { clicks.onNext(item) }
    }

}