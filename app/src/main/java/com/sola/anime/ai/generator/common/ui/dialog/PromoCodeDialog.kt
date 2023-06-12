package com.sola.anime.ai.generator.common.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.WindowManager
import com.basic.common.extension.clicks
import com.basic.common.extension.makeToast
import com.sola.anime.ai.generator.databinding.DialogPromoCodeBinding
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

class PromoCodeDialog @Inject constructor() {

    private lateinit var binding: DialogPromoCodeBinding
    private lateinit var dialog: Dialog

    val confirmClicks: Subject<String> = PublishSubject.create()

    fun show(activity: Activity){
        if (!::dialog.isInitialized){
            binding = DialogPromoCodeBinding.inflate(activity.layoutInflater)
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

            binding.later.clicks { dismiss() }
            binding.confirm.clicks {
                val promoCode = binding.editPromoCode.text?.toString()?.trim() ?: ""
                when {
                    promoCode.isEmpty() -> activity.makeToast("Promo code cannot be empty!")
                    else -> confirmClicks.onNext(promoCode)
                }
            }
        }

        if (isShowing()){
            return
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