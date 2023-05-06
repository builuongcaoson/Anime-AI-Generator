package com.sola.anime.ai.generator.feature.preview.adapter

import android.graphics.Bitmap
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import com.basic.common.base.LsAdapter
import com.basic.common.base.LsViewHolder
import com.basic.common.extension.clicks
import com.basic.common.extension.getDimens
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.databinding.ItemPreviewInPreviewBinding
import com.sola.anime.ai.generator.domain.model.history.ChildHistory
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

class PagePreviewAdapter @Inject constructor() : LsAdapter<ChildHistory, ItemPreviewInPreviewBinding>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LsViewHolder<ItemPreviewInPreviewBinding> {
        return LsViewHolder(parent, ItemPreviewInPreviewBinding::inflate)
    }

    override fun onBindViewHolder(
        holder: LsViewHolder<ItemPreviewInPreviewBinding>,
        position: Int
    ) {
        val item = getItem(position)
        val binding = holder.binding
        val context = binding.root.context

        val ratio = "${item.width}:${item.height}"

        val set = ConstraintSet()
        set.clone(binding.viewGroup)
        set.setDimensionRatio(binding.cardPreview.id, ratio)
        set.applyTo(binding.viewGroup)

        Glide
            .with(context)
            .asBitmap()
            .load(item.pathPreview)
            .placeholder(R.drawable.place_holder_image)
            .error(R.drawable.place_holder_image)
            .listener(object: RequestListener<Bitmap> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.cardPreview.cardElevation = 0f
                    binding.preview.setImageResource(R.drawable.place_holder_image)
                    binding.preview.animate().alpha(0f).setDuration(100).start()
                    return false
                }

                override fun onResourceReady(
                    resource: Bitmap?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    resource?.let { bitmap ->
                        binding.cardPreview.cardElevation = context.getDimens(com.intuit.sdp.R.dimen._3sdp)
                        binding.preview.setImageBitmap(bitmap)
                        binding.preview.animate().alpha(1f).setDuration(100).start()
                    } ?: run {
                        binding.cardPreview.cardElevation = 0f
                        binding.preview.setImageResource(R.drawable.place_holder_image)
                        binding.preview.animate().alpha(0f).setDuration(100).start()
                    }
                    return false
                }
            })
            .preload()
    }

}