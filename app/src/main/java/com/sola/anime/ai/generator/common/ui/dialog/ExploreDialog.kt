package com.sola.anime.ai.generator.common.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.method.ScrollingMovementMethod
import android.view.WindowManager
import androidx.constraintlayout.widget.ConstraintSet
import com.basic.common.extension.clicks
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.common.extension.copyToClipboard
import com.sola.anime.ai.generator.common.extension.load
import com.sola.anime.ai.generator.databinding.DialogExploreBinding
import com.sola.anime.ai.generator.domain.model.config.explore.Explore
import io.reactivex.subjects.Subject
import javax.inject.Inject

class ExploreDialog @Inject constructor() {

    private lateinit var binding: DialogExploreBinding
    private lateinit var dialog: Dialog

    fun show(activity: Activity, explore: Explore, useClicks: Subject<Explore>, detailClicks: Subject<Explore>) {
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

        initView(explore)

        binding.root.clicks(withAnim = false) { dismiss() }
        binding.close.clicks { dismiss() }
        binding.viewCopy.clicks { explore.prompt.copyToClipboard(activity) }
        binding.viewUse.clicks { useClicks.onNext(explore) }
        binding.viewDetails.clicks { detailClicks.onNext(explore) }

        if (isShowing()){
            return
        }

        dialog.show()
    }

    private fun initView(explore: Explore) {
        val set = ConstraintSet()
        set.clone(binding.viewPreview)
        set.setDimensionRatio(binding.viewRatioPreview.id, explore.ratio)
        set.applyTo(binding.viewPreview)

        binding.prompt.apply {
            text = explore.prompt
            movementMethod = ScrollingMovementMethod()
        }

        binding.viewPreview.post {
            if (isShowing()){
                binding.preview.animate().alpha(0f).setDuration(100).setStartDelay(0).start()
                binding.preview.load(explore.previews.firstOrNull()) { drawable ->
                    drawable?.let {
                        binding.preview.setImageDrawable(drawable)
                        binding.preview.animate().alpha(1f).setDuration(250).setStartDelay(250).start()
                    } ?: run {
                        binding.preview.setImageResource(R.drawable.place_holder_image)
                        binding.preview.animate().alpha(1f).setDuration(250).setStartDelay(250).start()
                    }
                }
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