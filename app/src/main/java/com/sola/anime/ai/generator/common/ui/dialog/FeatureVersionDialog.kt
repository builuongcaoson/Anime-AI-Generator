package com.sola.anime.ai.generator.common.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.WindowManager
import com.basic.common.extension.clicks
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.common.Navigator
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.databinding.DialogFeatureBinding
import com.sola.anime.ai.generator.databinding.DialogFeatureVersionBinding
import com.sola.anime.ai.generator.databinding.ItemFeatureVersionBinding
import javax.inject.Inject

class FeatureVersionDialog @Inject constructor(
    private val navigator: Navigator,
    private val prefs: Preferences
) {

    private lateinit var binding: DialogFeatureVersionBinding
    private lateinit var dialog: Dialog

    fun show(activity: Activity, version: Long, feature: String) {
        if (!::dialog.isInitialized) {
            binding = DialogFeatureVersionBinding.inflate(activity.layoutInflater)
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

        binding.textVersion.text = "V${version}"

        feature.split("***").forEach {
            val view = LayoutInflater.from(activity).inflate(R.layout.item_feature_version, binding.viewFeatures, false)
            val bindingFeatureVersion = ItemFeatureVersionBinding.bind(view)
            bindingFeatureVersion.feature.text = it
            binding.viewFeatures.addView(view)
        }


        binding.later.clicks {
            prefs.isShowFeatureDialog(version).set(true)

            dismiss()
        }
        binding.buyNow.clicks {
            prefs.isShowFeatureDialog(version).set(true)

            navigator.showRating()

            dismiss()
        }

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