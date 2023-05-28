package com.sola.anime.ai.generator.common.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.text.method.ScrollingMovementMethod
import android.view.WindowManager
import androidx.constraintlayout.widget.ConstraintSet
import com.basic.common.extension.clicks
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.common.extension.copyToClipboard
import com.sola.anime.ai.generator.databinding.DialogExploreBinding
import com.sola.anime.ai.generator.domain.model.config.explore.Explore
import io.reactivex.subjects.Subject
import javax.inject.Inject

class ExploreDialog @Inject constructor() {

    private lateinit var binding: DialogExploreBinding
    private lateinit var dialog: Dialog

    fun show(activity: Activity, explore: Explore, useClicks: Subject<Explore>) {
        if (!::dialog.isInitialized) {
            binding = DialogExploreBinding.inflate(activity.layoutInflater)
            dialog = Dialog(activity)
            dialog.setContentView(binding.root)
            dialog.setCancelable(true)

            dialog.window?.apply {
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                val lp = attributes
                lp.width = WindowManager.LayoutParams.MATCH_PARENT
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT
                attributes = lp
            }
        }

        initView(activity, explore)

        binding.close.clicks { dismiss() }
        binding.viewCopy.clicks { explore.prompt.copyToClipboard(activity) }
        binding.viewUse.clicks { useClicks.onNext(explore) }

        if (isShowing()){
            return
        }

        dialog.show()
    }

    private fun initView(activity: Activity, explore: Explore) {
        val set = ConstraintSet()
        set.clone(binding.viewPreview)
        set.setDimensionRatio(binding.viewRatioPreview.id, explore.ratio)
        set.applyTo(binding.viewPreview)

        binding.prompt.apply {
            text = explore.prompt
            movementMethod = ScrollingMovementMethod()
        }

        binding.preview.animate().alpha(0f).setDuration(100).start()
        binding.viewPreview.post {
            if (isShowing()){
                Glide
                    .with(activity)
                    .load(explore.preview)
                    .placeholder(R.drawable.place_holder_image)
                    .error(R.drawable.place_holder_image)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .listener(object: RequestListener<Drawable>{
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
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
                                binding.preview.setImageDrawable(resource)
                                binding.preview.animate().alpha(1f).setDuration(100).start()
                            } ?: run {
                                binding.preview.setImageResource(R.drawable.place_holder_image)
                                binding.preview.animate().alpha(0f).setDuration(100).start()
                            }
                            return false
                        }
                    })
                    .into(binding.preview)
            }
        }
    }

    fun dismiss() {
        if (::dialog.isInitialized && dialog.isShowing) {
            dialog.dismiss()
        }
    }

    fun isShowing() = if (::dialog.isInitialized) dialog.isShowing else false
}