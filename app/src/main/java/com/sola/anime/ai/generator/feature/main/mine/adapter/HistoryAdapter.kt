package com.sola.anime.ai.generator.feature.main.mine.adapter

import android.graphics.Bitmap
import android.view.ViewGroup
import com.basic.common.base.LsAdapter
import com.basic.common.base.LsViewHolder
import com.basic.common.extension.getDimens
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.databinding.ItemHistoryMineBinding
import com.sola.anime.ai.generator.domain.model.history.History
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

class HistoryAdapter @Inject constructor(): LsAdapter<History, ItemHistoryMineBinding>() {

    val clicks: Subject<History> = PublishSubject.create()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LsViewHolder<ItemHistoryMineBinding> {
        return LsViewHolder(parent, ItemHistoryMineBinding::inflate)
    }

    override fun onBindViewHolder(holder: LsViewHolder<ItemHistoryMineBinding>, position: Int) {
        val item = getItem(position)
        val binding = holder.binding
        val context = binding.root.context

        Glide
            .with(context)
            .asBitmap()
            .load(item.childs.lastOrNull()?.pathPreview)
            .placeholder(R.drawable.place_holder_image)
            .error(R.drawable.place_holder_image)
            .listener(object: RequestListener<Bitmap>{
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.cardPreview.cardElevation = 0f
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
                        binding.cardPreview.cardElevation = context.getDimens(com.intuit.sdp.R.dimen._3sdp)
                        binding.preview.setImageBitmap(bitmap)
                    } ?: run {
                        binding.cardPreview.cardElevation = 0f
                        binding.preview.setImageResource(R.drawable.place_holder_image)
                    }
                    return false
                }
            })
            .preload()

        binding.prompt.text = item.childs.lastOrNull()?.prompt ?: ""
    }

}