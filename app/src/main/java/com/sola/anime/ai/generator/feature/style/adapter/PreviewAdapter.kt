package com.sola.anime.ai.generator.feature.style.adapter

import android.graphics.Bitmap
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import com.basic.common.base.LsAdapter
import com.basic.common.base.LsViewHolder
import com.basic.common.extension.clicks
import com.basic.common.extension.getDimens
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.databinding.ItemPreviewStyleBinding
import com.sola.anime.ai.generator.domain.model.PreviewIap
import com.sola.anime.ai.generator.domain.model.Ratio
import com.sola.anime.ai.generator.domain.model.config.style.Style
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

class PreviewAdapter @Inject constructor(): LsAdapter<Style, ItemPreviewStyleBinding>() {

    val clicks: Subject<Style> = PublishSubject.create()
    var style: Style? = null
        set(value) {
            if (field == value){
                return
            }

            if (value == null){
                val oldIndex = data.indexOf(field)

                field = null

                notifyItemChanged(oldIndex)
                return
            }

            data.indexOf(field).takeIf { it != -1 }?.let { notifyItemChanged(it) }
            data.indexOf(value).takeIf { it != -1 }?.let { notifyItemChanged(it) }

            field = value
        }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LsViewHolder<ItemPreviewStyleBinding> {
        return LsViewHolder(parent, ItemPreviewStyleBinding::inflate)
    }

    override fun onBindViewHolder(holder: LsViewHolder<ItemPreviewStyleBinding>, position: Int) {
        val item = getItem(position % data.size)
        val binding = holder.binding
        val context = binding.root.context

        binding.display.text = item.display
        binding.viewSelected.isVisible = item == style

        Glide.with(context)
            .asBitmap()
            .load(item.preview)
            .error(R.drawable.place_holder_image)
            .placeholder(R.drawable.place_holder_image)
            .listener(object: RequestListener<Bitmap> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.viewPreview.cardElevation = 0f
                    binding.viewShadow.isVisible = false
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
                    resource?.let { bitmap ->
                        binding.viewShadow.isVisible = true
                        binding.viewPreview.cardElevation = context.getDimens(com.intuit.sdp.R.dimen._2sdp)
                        binding.preview.setImageBitmap(bitmap)
                        binding.preview.animate().alpha(1f).setDuration(250).start()
                    } ?: run {
                        binding.viewPreview.cardElevation = 0f
                        binding.viewShadow.isVisible = false
                        binding.preview.setImageResource(R.drawable.place_holder_image)
                    }
                    return false
                }
            })
            .preload()

        binding.viewPreview.clicks { clicks.onNext(item) }
    }

}