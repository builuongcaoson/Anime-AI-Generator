package com.sola.anime.ai.generator.common.ui.sheet.share

import android.graphics.Color
import android.view.View
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import coil.load
import coil.transition.CrossfadeTransition
import com.basic.common.extension.clicks
import com.basic.common.extension.makeToast
import com.basic.common.extension.resolveAttrColor
import com.basic.common.extension.setTint
import com.basic.common.extension.tryOrNull
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.common.base.LsBottomSheet
import com.sola.anime.ai.generator.common.extension.blur
import com.sola.anime.ai.generator.common.extension.startIap
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.databinding.SheetDownloadBinding
import com.sola.anime.ai.generator.databinding.SheetShareBinding
import com.sola.anime.ai.generator.databinding.SheetUpscaleBinding
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class ShareSheet: LsBottomSheet<SheetShareBinding>(SheetShareBinding::inflate) {

    @Inject lateinit var prefs: Preferences

    val shareFrameClicks: Subject<View> by lazy { PublishSubject.create() }
    val shareOriginalClicks: Subject<File> by lazy { PublishSubject.create() }
    private var isFrame: Boolean = true
        set(value) {
            if (!value && !prefs.isUpgraded.get()){
                activity?.startIap()
                return
            }
            field = value
            updateUiFrame()
        }
    var file: File? = null
    var ratio: String = "1:1"
    var style: String = "No Style"

    override fun onViewCreated() {
        initView()
        initData()
        listenerView()
    }

    private fun updateUiFrame() {
        when {
            !isFrame -> activity?.startIap()
            else -> {
                activity?.let { activity ->
                    binding.viewFrame.isVisible = isFrame
                    binding.previewOriginal.isVisible = !isFrame

                    binding.viewTabFrame.setCardBackgroundColor(if (isFrame) activity.resolveAttrColor(android.R.attr.colorAccent) else Color.TRANSPARENT)
                    binding.viewTabOriginal.setCardBackgroundColor(if (!isFrame) activity.resolveAttrColor(android.R.attr.colorAccent) else Color.TRANSPARENT)
                    binding.textFrame.setTextColor(if (isFrame) activity.resolveAttrColor(com.google.android.material.R.attr.colorOnPrimary) else activity.resolveAttrColor(android.R.attr.colorAccent))
                    binding.imagePremiumOriginal.setTint(if (!isFrame) activity.resolveAttrColor(com.google.android.material.R.attr.colorOnPrimary) else activity.resolveAttrColor(android.R.attr.colorAccent))
                    binding.textPremiumOriginal.setTextColor(if (!isFrame) activity.resolveAttrColor(com.google.android.material.R.attr.colorOnPrimary) else activity.resolveAttrColor(android.R.attr.colorAccent))
                }
            }
        }
    }

    private fun listenerView() {
        binding.viewTabFrame.clicks(withAnim = false) { isFrame = true }
        binding.viewTabOriginal.clicks(withAnim = false) { isFrame = false }
        binding.viewShare.clicks(withAnim = false) { shareClicks() }
    }

    private fun shareClicks() {
        when {
            isFrame -> shareFrameClicks.onNext(binding.viewFrame)
            else -> file?.takeIf { file -> file.exists() }?.let { file -> shareOriginalClicks.onNext(file) }
        }
    }

    override fun onResume() {
        initObservable()
        super.onResume()
    }

    private fun initObservable() {

    }

    private fun initData() {

    }

    private fun initView() {
        binding.style.text = style

        ConstraintSet().apply {
            clone(binding.viewRoot)
            setDimensionRatio(binding.previewRatio.id, ratio)
            applyTo(binding.viewRoot)
        }

        tryOrNull { binding.blurView.blur(binding.viewFrame) }

        file?.let {
            binding.previewFrame1.load(file) {
                crossfade(true)
                error(R.drawable.place_holder_image)
            }

            binding.previewFrame2.load(file) {
                crossfade(true)
                error(R.drawable.place_holder_image)
            }

            binding.previewOriginal.load(file) {
                crossfade(true)
                error(R.drawable.place_holder_image)
            }
        }
    }

}