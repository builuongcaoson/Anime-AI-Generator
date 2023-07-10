package com.sola.anime.ai.generator.feature.pickAvatar.adapter

import android.net.Uri
import com.basic.common.base.LsAdapter
import com.basic.common.extension.clicks
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.databinding.ItemPhotoInAvatarBinding
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

class PhotoAdapter @Inject constructor() : LsAdapter<Uri, ItemPhotoInAvatarBinding>(ItemPhotoInAvatarBinding::inflate) {

    val subjectDeleteChanges: Subject<Uri> = PublishSubject.create()

    var item: Uri? = null
        set(value) {
            if (field == value) {
                return
            }

            if (value == null) {
                val oldIndex = data.indexOf(field)

                field = null

                notifyItemChanged(oldIndex)
                return
            }

            data.indexOf(field).takeIf { it != -1 }?.let { notifyItemChanged(it) }
            data.indexOf(value).takeIf { it != -1 }?.let { notifyItemChanged(it) }

            field = value
        }

    override fun bindItem(item: Uri, binding: ItemPhotoInAvatarBinding, position: Int) {
        Glide.with(binding.root.context)
            .load(item)
            .error(R.drawable.place_holder_image)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(binding.preview)

        binding.closePhoto.clicks { subjectDeleteChanges.onNext(item) }
    }
}
