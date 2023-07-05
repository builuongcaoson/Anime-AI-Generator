package com.sola.anime.ai.generator.feature.processing.art.adapter

import android.graphics.drawable.Drawable
import com.basic.common.base.LsAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.databinding.ItemPreviewArtProcessingBinding
import com.sola.anime.ai.generator.domain.model.config.process.Process
import javax.inject.Inject

class PreviewAdapter @Inject constructor() : LsAdapter<Process, ItemPreviewArtProcessingBinding>(ItemPreviewArtProcessingBinding::inflate) {

    var totalCount = 0

    fun insert(){
        totalCount += 1
        notifyItemInserted(totalCount - 1)
    }

    override fun bindItem(
        item: Process,
        binding: ItemPreviewArtProcessingBinding,
        position: Int
    ) {
        val item = getItem(position % data.size) ?: return
        val context = binding.root.context

        Glide
            .with(context)
            .load(item.preview)
            .centerCrop()
            .transition(DrawableTransitionOptions.withCrossFade())
//            .listener(object: RequestListener<Drawable> {
//                override fun onLoadFailed(
//                    e: GlideException?,
//                    model: Any?,
//                    target: Target<Drawable>?,
//                    isFirstResource: Boolean
//                ): Boolean {
////                    binding.cardPreview.cardElevation = 0f
//                    binding.preview.setImageResource(R.drawable.place_holder_image)
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
////                        binding.cardPreview.cardElevation = context.getDimens(com.intuit.sdp.R.dimen._5sdp)
//                        binding.preview.setImageDrawable(resource)
//                    } ?: run {
////                        binding.cardPreview.cardElevation = 0f
//                        binding.preview.setImageResource(R.drawable.place_holder_image)
//                    }
//                    return false
//                }
//
//            })
            .into(binding.preview)

        binding.textTitle.text = item.title
        binding.textArtist.text = item.artist
    }

    override fun getItemCount(): Int {
        return totalCount
    }

}