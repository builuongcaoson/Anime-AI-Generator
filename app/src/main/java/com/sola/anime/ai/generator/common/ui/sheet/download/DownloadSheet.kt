package com.sola.anime.ai.generator.common.ui.sheet.download

import android.graphics.Color
import android.view.View
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import com.basic.common.extension.clicks
import com.basic.common.extension.makeToast
import com.basic.common.extension.resolveAttrColor
import com.basic.common.extension.setTint
import com.basic.common.extension.tryOrNull
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.common.base.LsBottomSheet
import com.sola.anime.ai.generator.common.extension.blur
import com.sola.anime.ai.generator.common.extension.startIap
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.databinding.SheetDownloadBinding
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
class DownloadSheet: LsBottomSheet<SheetDownloadBinding>(SheetDownloadBinding::inflate) {

    @Inject lateinit var prefs: Preferences

    private val subjectFrameChanges: Subject<Boolean> by lazy { BehaviorSubject.createDefault(true) }
    val downloadFrameClicks: Subject<View> by lazy { PublishSubject.create() }
    val downloadOriginalClicks: Subject<File> by lazy { PublishSubject.create() }
    var file: File? = null
    var ratio: String = "1:1"

    override fun onViewCreated() {
        initView()
        initData()
        listenerView()
    }

    private fun listenerView() {
        binding.viewTabFrame.clicks(withAnim = false) { downloadFrameClicks.onNext(binding.viewFrame) }
        binding.viewTabOriginal.clicks(withAnim = false) { file?.takeIf { file -> file.exists() }?.let { file -> downloadOriginalClicks.onNext(file) } }
    }

    override fun onResume() {
        initObservable()
        super.onResume()
    }

    private fun initObservable() {
        subjectFrameChanges
            .autoDispose(scope())
            .subscribe { isFrame ->
                when {
                    !isFrame && prefs.numberDownloadedOriginal.get() >= Preferences.MAX_NUMBER_DOWNLOAD_ORIGINAL -> activity?.startIap()
                    else -> {
                        activity?.let { activity ->
                            binding.viewFrame.isVisible = isFrame
                            binding.previewOriginal.isVisible = !isFrame

                            binding.viewTabFrame.setCardBackgroundColor(if (isFrame) activity.resolveAttrColor(android.R.attr.colorAccent) else Color.TRANSPARENT)
                            binding.viewTabOriginal.setCardBackgroundColor(if (!isFrame) activity.resolveAttrColor(android.R.attr.colorAccent) else Color.TRANSPARENT)
                            binding.textFrame.setTextColor(if (isFrame) activity.resolveAttrColor(com.google.android.material.R.attr.colorOnPrimary) else activity.resolveAttrColor(android.R.attr.colorAccent))
                            binding.imagePremiumOriginal.setTint(if (!isFrame) activity.resolveAttrColor(com.google.android.material.R.attr.colorOnPrimary) else activity.resolveAttrColor(android.R.attr.colorAccent))
                            binding.textPremiumOriginal.setTextColor(if (!isFrame) activity.resolveAttrColor(com.google.android.material.R.attr.colorOnPrimary) else activity.resolveAttrColor(android.R.attr.colorAccent))

                            binding.iconWatchAd.isVisible = !prefs.isUpgraded.get() && !isFrame
                            binding.textDescription.isVisible = !prefs.isUpgraded.get() && !isFrame
                        }
                    }
                }
            }

        prefs
            .isUpgraded
            .asObservable()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribe { isUpgraded ->
                binding.imagePremiumOriginal.isVisible = !isUpgraded
                binding.iconWatchAd.isVisible = !isUpgraded && !subjectFrameChanges.blockingFirst()
                binding.textDescription.isVisible = !isUpgraded && !subjectFrameChanges.blockingFirst()
            }
    }

    private fun initData() {

    }

    private fun initView() {
        ConstraintSet().apply {
            clone(binding.viewRoot)
            setDimensionRatio(binding.previewRatio.id, ratio)
            applyTo(binding.viewRoot)
        }

        tryOrNull { binding.blurView.blur(binding.viewFrame) }

        file?.let {
            Glide.with(this)
                .load(file)
                .transition(DrawableTransitionOptions.withCrossFade())
                .error(R.drawable.place_holder_image)
                .into(binding.previewFrame1)

            Glide.with(this)
                .load(file)
                .transition(DrawableTransitionOptions.withCrossFade())
                .error(R.drawable.place_holder_image)
                .into(binding.previewFrame2)

            Glide.with(this)
                .load(file)
                .transition(DrawableTransitionOptions.withCrossFade())
                .error(R.drawable.place_holder_image)
                .into(binding.previewOriginal)
        }
    }

}