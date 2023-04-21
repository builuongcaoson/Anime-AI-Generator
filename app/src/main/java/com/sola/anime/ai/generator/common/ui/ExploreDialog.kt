package com.sola.anime.ai.generator.common.ui

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.method.ScrollingMovementMethod
import android.view.WindowManager
import androidx.constraintlayout.widget.ConstraintSet
import com.basic.common.extension.clicks
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
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

        dialog.show()
    }

    private fun initView(activity: Activity, explore: Explore) {
        val set = ConstraintSet()
        set.clone(binding.viewPreview)
        set.setDimensionRatio(binding.viewPreview.id, explore.ratio)
        set.applyTo(binding.viewPreview)

        binding.prompt.apply {
            text = explore.prompt
            movementMethod = ScrollingMovementMethod()
        }

        Glide
            .with(activity)
            .asBitmap()
            .load(explore.preview)
            .placeholder(R.drawable.place_holder_image)
            .error(R.drawable.place_holder_image)
            .transition(BitmapTransitionOptions.withCrossFade())
            .into(binding.preview)
    }

    fun dismiss() {
        if (::dialog.isInitialized && dialog.isShowing) {
            dialog.dismiss()
        }
    }

    fun isShowing() = if (::dialog.isInitialized) dialog.isShowing else false
}