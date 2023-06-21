package com.sola.anime.ai.generator.feature.main.mine.adapter

import android.graphics.drawable.Drawable
import androidx.constraintlayout.widget.ConstraintSet
import com.basic.common.base.LsAdapter
import com.basic.common.extension.clicks
import com.basic.common.extension.getDimens
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.databinding.ItemHistoryMineBinding
import com.sola.anime.ai.generator.domain.model.Ratio
import com.sola.anime.ai.generator.domain.model.history.History
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

class HistoryAdapter @Inject constructor(): LsAdapter<History, ItemHistoryMineBinding>(ItemHistoryMineBinding::inflate) {

    val clicks: Subject<History> = PublishSubject.create()

    override fun bindItem(item: History, binding: ItemHistoryMineBinding, position: Int) {
        val context = binding.root.context

        val ratio = "${item.childs.lastOrNull()?.width ?: "1"}:${item.childs.lastOrNull()?.height ?: "1"}"

        val set = ConstraintSet()
        set.clone(binding.viewGroup)
        set.setDimensionRatio(binding.cardPreview.id, ratio)
        set.applyTo(binding.viewGroup)

//        val layoutParams = binding.root.layoutParams as StaggeredGridLayoutManager.LayoutParams
//        layoutParams.isFullSpan = ratio == "${Ratio.Ratio16x9.width}:${Ratio.Ratio16x9.height}"

        val lastChild = item.childs.lastOrNull()

        Glide
            .with(context)
            .load(lastChild?.upscalePathPreview ?: lastChild?.pathPreview)
            .placeholder(R.drawable.place_holder_image)
            .error(R.drawable.place_holder_image)
            .listener(object: RequestListener<Drawable>{
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.cardPreview.cardElevation = 0f
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
                        binding.cardPreview.cardElevation = context.getDimens(com.intuit.sdp.R.dimen._3sdp)
                        binding.preview.setImageDrawable(resource)
                        binding.preview.animate().alpha(1f).setDuration(100).start()
                    } ?: run {
                        binding.cardPreview.cardElevation = 0f
                        binding.preview.setImageResource(R.drawable.place_holder_image)
                        binding.preview.animate().alpha(0f).setDuration(100).start()
                    }
                    return false
                }
            })
            .into(binding.preview)

        binding.title.text = item.title
        binding.prompt.text = item.childs.lastOrNull()?.prompt ?: ""

        binding.viewGroup.clicks(withAnim = false) { clicks.onNext(item) }
    }

}