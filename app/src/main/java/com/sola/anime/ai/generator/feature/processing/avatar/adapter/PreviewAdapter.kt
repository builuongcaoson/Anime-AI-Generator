package com.sola.anime.ai.generator.feature.processing.avatar.adapter

import androidx.core.view.isVisible
import com.basic.common.base.LsAdapter
import com.basic.common.extension.clicks
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.databinding.ItemPreviewAvatarProcessingBinding
import com.sola.anime.ai.generator.domain.model.status.DezgoStatusImageToImage
import com.sola.anime.ai.generator.domain.model.status.DezgoStatusTextToImage
import com.sola.anime.ai.generator.domain.model.status.StatusBodyImageToImage
import com.sola.anime.ai.generator.domain.model.status.StatusBodyTextToImage
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

class PreviewAdapter @Inject constructor() : LsAdapter<DezgoStatusImageToImage, ItemPreviewAvatarProcessingBinding>(ItemPreviewAvatarProcessingBinding::inflate) {

    val downloadClicks: Subject<DezgoStatusImageToImage> = PublishSubject.create()

    override fun bindItem(
        item: DezgoStatusImageToImage,
        binding: ItemPreviewAvatarProcessingBinding,
        position: Int
    ) {
        val status = item.status

        binding.viewDownload.isVisible = status is StatusBodyImageToImage.Success

        when (status) {
            StatusBodyImageToImage.Loading -> {
                binding.viewLoading.isVisible = true
            }
            is StatusBodyImageToImage.Failure -> {
                binding.viewLoading.isVisible = false

                binding.preview.setImageResource(R.drawable.place_holder_image)
            }

            is StatusBodyImageToImage.Success -> {
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