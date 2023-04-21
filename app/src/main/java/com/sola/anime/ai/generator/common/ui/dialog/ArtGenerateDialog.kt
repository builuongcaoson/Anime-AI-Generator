package com.sola.anime.ai.generator.common.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.WindowManager
import com.sola.anime.ai.generator.R
import javax.inject.Inject

class ArtGenerateDialog @Inject constructor() {

    lateinit var dialog: Dialog

    fun show(activity: Activity){
        if (!::dialog.isInitialized){
            dialog = Dialog(activity)
            dialog.setContentView(R.layout.dialog_art_generate)
            dialog.setCancelable(false)

            dialog.window?.apply {
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                val lp = attributes
                lp.width = WindowManager.LayoutParams.WRAP_CONTENT
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT
                attributes = lp
                setGravity(Gravity.BOTTOM)
            }
        }

        dialog.show()
    }

    fun dismiss(){
        if (::dialog.isInitialized && dialog.isShowing){
            dialog.dismiss()
        }
    }

    fun isShowing() = if (::dialog.isInitialized) dialog.isShowing else false
}