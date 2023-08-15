package com.sola.anime.ai.generator.feature.main.art.adapter

import com.basic.common.base.LsAdapter
import com.basic.common.extension.clicks
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.sola.anime.ai.generator.databinding.ItemPreviewArt2Binding
import com.sola.anime.ai.generator.domain.model.config.explore.Explore
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

class PreviewAdapter @Inject constructor(): LsAdapter<Explore, ItemPreviewArt2Binding>(ItemPreviewArt2Binding::inflate) {

    val clicks: Subject<Explore> = PublishSubject.create()

    override fun bindItem(item: Explore, binding: ItemPreviewArt2Binding, position: Int) {
        val context = binding.root.context

        Glide
            .with(context)
            .load(item.previews.firstOrNull())
            .centerCrop()
            .transition(DrawableTransitionOptions.withCrossFade())
//            .listener(object: RequestListener<Drawable>{
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

        binding.cardPreview.clicks(withAnim = false) { clicks.onNext(item) }
    }

}