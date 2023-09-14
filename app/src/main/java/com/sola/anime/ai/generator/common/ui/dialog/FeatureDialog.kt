package com.sola.anime.ai.generator.common.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.WindowManager
import com.basic.common.extension.clicks
import com.sola.anime.ai.generator.common.ConfigApp
import com.sola.anime.ai.generator.databinding.DialogFeatureBinding
import javax.inject.Inject

class FeatureDialog @Inject constructor(
    private val configApp: ConfigApp
) {

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

        binding.numberCreatedArtworkPremium.text = "Create ${configApp.maxNumberGeneratePremium} high-quality artworks every day"

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