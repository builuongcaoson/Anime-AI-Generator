package com.sola.anime.ai.generator.feature.processing.art.adapter

import android.graphics.Bitmap
import android.view.ViewGroup
import com.basic.common.base.LsAdapter
import com.basic.common.base.LsViewHolder
import com.basic.common.extension.getDimens
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.databinding.ItemPreviewArtProcessingBinding
import com.sola.anime.ai.generator.domain.model.config.artprocess.ArtProcess
import javax.inject.Inject

class PreviewAdapter @Inject constructor() : LsAdapter<ArtProcess, ItemPreviewArtProcessingBinding>() {

    var totalCount = 0

    fun insert(){
        totalCount += 1
        notifyItemInserted(totalCount - 1)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LsViewHolder<ItemPreviewArtProcessingBinding> {
        return LsViewHolder(parent, ItemPreviewArtProcessingBinding::inflate)
    }

    override fun onBindViewHolder(
        holder: LsViewHolder<ItemPreviewArtProcessingBinding>,
        position: Int
    ) {
        val item = getItem(position % data.size)
        val binding = holder.binding
        val context = binding.root.context

        Glide
            .with(context)
            .asBitmap()
            .load(item.preview)
            .listener(object: RequestListener<Bitmap> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    isFirstResource: Boolean
                ): Boolean {
//                    binding.cardPreview.cardElevation = 0f
                    binding.preview.setImageResource(R.drawable.place_holder_image)
                    return false
                }

                override fun onResourceReady(
                    resource: Bitmap?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    resource?.let {
//                        binding.cardPreview.cardElevation = context.getDimens(com.intuit.sdp.R.dimen._5sdp)
                        binding.preview.setImageBitmap(it)
                    } ?: run {
//                        binding.cardPreview.cardElevation = 0f
                        binding.preview.setImageResource(R.drawable.place_holder_image)
                    }
                    return false
                }

            })
            .preload()

        binding.textTitle.text = item.title
        binding.textArtist.text = item.artist
    }

    override fun getItemCount(): Int {
        return totalCount
    }

}