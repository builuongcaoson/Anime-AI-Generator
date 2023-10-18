package com.sola.anime.ai.generator.common.ui.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup
import android.view.Window
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.common.Navigator
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.databinding.DialogRatingBinding
import javax.inject.Inject

class RatingDialog @Inject constructor(
    private val prefs: Preferences,
    private val navigator: Navigator
) {

    private lateinit var binding: DialogRatingBinding
    private var dialog: Dialog? = null

    @SuppressLint("ClickableViewAccessibility")
    fun show(activity: Activity){
        if (dialog == null){
            binding = DialogRatingBinding.inflate(activity.layoutInflater)
            dialog = Dialog(activity)
            dialog?.apply {
                requestWindowFeature(Window.FEATURE_NO_TITLE)
                window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                window?.setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                setCancelable(true)
                setContentView(binding.root)
            }
        }

        if (isShowing()){
            return
        }

        binding.ratingBar.setOnTouchListener { _, _ ->
            when {
                binding.ratingBar.rating <= 1 -> binding.preview.setImageResource(R.drawable.star_1)
                binding.ratingBar.rating <= 2 -> binding.preview.setImageResource(R.drawable.star_2)
                binding.ratingBar.rating <= 3 -> binding.preview.setImageResource(R.drawable.star_3)
                binding.ratingBar.rating <= 4 -> binding.preview.setImageResource(R.drawable.star_4)
                else -> binding.preview.setImageResource(R.drawable.star_5)
            }
            false
        }

        binding.cancel.setOnClickListener {
            dismiss()
        }
        binding.submit.setOnClickListener {
            when {
                binding.ratingBar.rating < 4 -> navigator.showSupport()
                else -> navigator.showRating()
            }

            prefs.isRatedApp.set(true)
            dismiss()
        }

        dialog?.show()
    }

    fun isShowing() = if (dialog != null) dialog!!.isShowing else false

    fun dismiss(){
        if (dialog?.isShowing == true){
            dialog?.dismiss()
        }
    }

}