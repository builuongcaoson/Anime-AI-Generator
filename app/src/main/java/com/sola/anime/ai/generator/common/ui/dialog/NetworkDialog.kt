package com.sola.anime.ai.generator.common.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.WindowManager
import com.basic.common.extension.clicks
import com.sola.anime.ai.generator.databinding.DialogNetworkBinding

import javax.inject.Inject

class NetworkDialog @Inject constructor() {

    private lateinit var binding: DialogNetworkBinding
    private lateinit var dialog: Dialog

    fun show(activity: Activity, tryClicks: () -> Unit = {}) {
        if (!::dialog.isInitialized) {
            binding = DialogNetworkBinding.inflate(activity.layoutInflater)
            dialog = Dialog(activity)
            dialog.setContentView(binding.root)
            dialog.setCancelable(false)

            dialog.window?.apply {
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                val lp = attributes
                lp.width = WindowManager.LayoutParams.MATCH_PARENT
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT
                attributes = lp
            }
        }

        binding.viewTryAgain.clicks { tryClicks() }

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