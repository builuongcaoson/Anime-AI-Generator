package com.sola.anime.ai.generator.common.extension

import android.graphics.drawable.Drawable
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import eightbitlab.com.blurview.BlurView

fun BlurView.blur(rootView: ViewGroup, ratioBlur: Float = 20f){
    this.setupWith(rootView, context.getBlurAlgorithm())
        .setFrameClearDrawable(rootView.background)
        .setBlurRadius(ratioBlur)
}

fun ImageView.load(any: Any?, placeholderRes: Int? = null, errorRes: Int? = null, done: (Drawable?) -> Unit = {}){
    val builder = Glide.with(this.context)
        .load(any)
        .transition(DrawableTransitionOptions.withCrossFade())
        .listener(object: RequestListener<Drawable>{
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>,
                isFirstResource: Boolean
            ): Boolean {
                done(null)
                return false
            }

            override fun onResourceReady(
                resource: Drawable,
                model: Any,
                target: Target<Drawable>?,
                dataSource: DataSource,
                isFirstResource: Boolean
            ): Boolean {
                done(resource)
                return false
            }
        })

    placeholderRes?.let {
        builder.placeholder(placeholderRes)
    }

    errorRes?.let {
        builder.error(errorRes)
    }

    builder.into(this)
}