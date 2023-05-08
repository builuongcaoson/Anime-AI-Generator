package com.sola.anime.ai.generator.feature.first.adapter

import android.graphics.Bitmap
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import com.basic.common.base.LsAdapter
import com.basic.common.base.LsViewHolder
import com.basic.common.extension.getDimens
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.databinding.ItemPreviewIapBinding
import com.sola.anime.ai.generator.databinding.ItemPreviewInFirstBinding
import com.sola.anime.ai.generator.domain.model.config.first.FirstPreview
import javax.inject.Inject

class PreviewAdapter @Inject constructor(): LsAdapter<FirstPreview, ItemPreviewInFirstBinding>() {

    var totalCount = 0

    fun insert(){
        totalCount += 1
        notifyItemInserted(totalCount - 1)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LsViewHolder<ItemPreviewInFirstBinding> {
        return LsViewHolder(parent, ItemPreviewInFirstBinding::inflate)
    }

    override fun onBindViewHolder(holder: LsViewHolder<ItemPreviewInFirstBinding>, position: Int) {
        val item = getItem(position % data.size)
        val binding = holder.binding
        val context = binding.root.context

        val set = ConstraintSet()
        set.clone(binding.viewGroup)
        set.setDimensionRatio(binding.viewPreview.id, item.ratio)
        set.applyTo(binding.viewGroup)

        binding.viewPreview.cardElevation = context.getDimens(com.intuit.sdp.R.dimen._2sdp)
        binding.preview.setImageResource(item.preview)
        binding.preview.animate().alpha(1f).setDuration(100).start()

//        Glide.with(context)
//            .asBitmap()
//            .load(item.preview)
//            .error(R.drawable.place_holder_image)
//            .placeholder(R.drawable.place_holder_image)
//            .transition(BitmapTransitionOptions.withCrossFade())
//            .listener(object: RequestListener<Bitmap>{
//                override fun onLoadFailed(
//                    e: GlideException?,
//                    model: Any?,
//                    target: Target<Bitmap>?,
//                    isFirstResource: Boolean
//                ): Boolean {
//                    binding.viewPreview.cardElevation = 0f
//                    binding.preview.setImageResource(R.drawable.place_holder_image)
//                    return false
//                }
//
//                override fun onResourceReady(
//                    resource: Bitmap?,
//                    model: Any?,
//                    target: Target<Bitmap>?,
//                    dataSource: DataSource?,
//                    isFirstResource: Boolean
//                ): Boolean {
//                    resource?.let { bitmap ->
//                        binding.viewPreview.cardElevation = context.getDimens(com.intuit.sdp.R.dimen._2sdp)
//                        binding.preview.setImageBitmap(bitmap)
//                        binding.preview.animate().alpha(1f).setDuration(100).start()
//                    } ?: run {
//                        binding.viewPreview.cardElevation = 0f
//                        binding.preview.setImageResource(R.drawable.place_holder_image)
//                        binding.preview.animate().alpha(0f).setDuration(100).start()
//                    }
//                    return false
//                }
//
//            })
//            .into(binding.preview)
    }

    override fun getItemCount(): Int {
        return totalCount
    }

}