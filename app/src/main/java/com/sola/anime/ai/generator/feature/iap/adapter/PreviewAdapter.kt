package com.sola.anime.ai.generator.feature.iap.adapter

import android.graphics.drawable.Drawable
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import com.basic.common.base.LsAdapter
import com.basic.common.base.LsViewHolder
import com.basic.common.extension.getDimens
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.databinding.ItemPreviewIapBinding
import com.sola.anime.ai.generator.domain.model.PreviewIap
import javax.inject.Inject

class PreviewAdapter @Inject constructor(): LsAdapter<PreviewIap, ItemPreviewIapBinding>() {

    var totalCount = 0

    fun insert(){
        totalCount += 1
        notifyItemInserted(totalCount - 1)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LsViewHolder<ItemPreviewIapBinding> {
        return LsViewHolder(parent, ItemPreviewIapBinding::inflate)
    }

    override fun onBindViewHolder(holder: LsViewHolder<ItemPreviewIapBinding>, position: Int) {
        val item = getItem(position % data.size)
        val binding = holder.binding
        val context = binding.root.context

        val set = ConstraintSet()
        set.clone(binding.viewGroup)
        set.setDimensionRatio(binding.viewPreview.id, item.ratio)
        set.applyTo(binding.viewGroup)

        Glide.with(context)
            .load(item.preview)
            .error(R.drawable.place_holder_image)
            .placeholder(R.drawable.place_holder_image)
            .listener(object: RequestListener<Drawable>{
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.preview.setImageResource(R.drawable.place_holder_image)
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
                        binding.preview.setImageDrawable(it)
                    } ?: run {
                        binding.preview.setImageResource(R.drawable.place_holder_image)
                    }
                    return false
                }

            })
            .into(binding.preview)
    }

    override fun getItemCount(): Int {
        return totalCount
    }

}