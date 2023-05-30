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
import com.sola.anime.ai.generator.databinding.DialogFeatureBinding
import com.sola.anime.ai.generator.domain.model.config.explore.Explore
import io.reactivex.subjects.Subject
import javax.inject.Inject

class FeatureDialog @Inject constructor() {

    private lateinit var binding: DialogFeatureBinding
    private lateinit var dialog: Dialog

    fun show(activity: Activity, buyNow: () -> Unit = {}) {
        if (!::dialog.isInitialized) {
            binding = DialogFeatureBinding.inflate(activity.layoutInflater)
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

        binding.later.clicks { dismiss() }
        binding.buyNow.clicks { buyNow() }

        if (isShowing()){
            return
        }

        dialog.show()
    }

    fun dismiss() {
        if (::dialog.isInitialized && dialog.isShowing) {
            dialog.dismiss()
        }
    }

    fun isShowing() = if (::dialog.isInitialized) dialog.isShowing else false
}