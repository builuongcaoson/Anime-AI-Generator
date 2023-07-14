package com.sola.anime.ai.generator.feature.processing.batch.adapter

import androidx.core.view.isVisible
import com.basic.common.base.LsAdapter
import com.basic.common.extension.clicks
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.databinding.ItemPreviewBatchProcessingBinding
import com.sola.anime.ai.generator.domain.model.status.DezgoStatusTextToImage
import com.sola.anime.ai.generator.domain.model.status.StatusBodyTextToImage
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

class PreviewAdapter @Inject constructor() : LsAdapter<DezgoStatusTextToImage, ItemPreviewBatchProcessingBinding>(ItemPreviewBatchProcessingBinding::inflate) {

    val downloadClicks: Subject<DezgoStatusTextToImage> = PublishSubject.create()

    override fun bindItem(
        item: DezgoStatusTextToImage,
        binding: ItemPreviewBatchProcessingBinding,
        position: Int
    ) {
//        ConstraintSet().apply {
//            clone(binding.viewRoot)
//            setDimensionRatio(binding.viewPreviewRatio.id, "${item.body.width}:${item.body.height}")
//            applyTo(binding.viewRoot)
//        }

        val status = item.status

        binding.viewDownload.isVisible = status is StatusBodyTextToImage.Success

        when (status) {
            StatusBodyTextToImage.Loading -> {
                binding.viewLoading.isVisible = true
            }
            is StatusBodyTextToImage.Failure -> {
                binding.viewLoading.isVisible = false

                binding.preview.setImageResource(R.drawable.place_holder_image)
            }

            is StatusBodyTextToImage.Success -> {
                binding.viewLoading.isVisible = false

                Glide
                    .with(binding.root)
                    .load(status.file)
                    .centerCrop()
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .error(R.drawable.place_holder_image)
                    .into(binding.preview)

                binding.viewDownload.clicks { downloadClicks.onNext(item) }
            }
        }

    }

}