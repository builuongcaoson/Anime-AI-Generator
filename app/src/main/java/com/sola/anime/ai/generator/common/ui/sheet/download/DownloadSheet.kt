package com.sola.anime.ai.generator.common.ui.sheet.download

import android.graphics.Color
import android.view.View
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import com.basic.common.extension.clicks
import com.basic.common.extension.resolveAttrColor
import com.basic.common.extension.setTint
import com.basic.common.extension.tryOrNull
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.common.base.LsBottomSheet
import com.sola.anime.ai.generator.common.extension.blur
import com.sola.anime.ai.generator.common.extension.load
import com.sola.anime.ai.generator.common.extension.startIap
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.databinding.SheetDownloadBinding
import com.sola.anime.ai.generator.domain.manager.PermissionManager
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class DownloadSheet: LsBottomSheet<SheetDownloadBinding>(SheetDownloadBinding::inflate) {

    companion object {
        const val RESULT_STORAGE_PERMISSION = 1
    }

    @Inject lateinit var prefs: Preferences
    @Inject lateinit var permissionManager: PermissionManager

    val downloadFrameClicks: Subject<View> by lazy { PublishSubject.create() }
    val downloadOriginalClicks: Subject<File> by lazy { PublishSubject.create() }
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

    override fun onViewCreated() {
        initView()
        initData()
        listenerView()
    }

    private fun updateUiFrame() {
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

    private fun listenerView() {
        binding.viewTabFrame.clicks(withAnim = false) { isFrame = true }
        binding.viewTabOriginal.clicks(withAnim = false) { isFrame = false }
        binding.viewDownload.clicks(withAnim = false) {
            when {
                permissionManager.hasStorage() -> downloadClicks()
                else -> permissionManager.requestStorage(this, RESULT_STORAGE_PERMISSION)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when {
            requestCode == RESULT_STORAGE_PERMISSION && permissionManager.hasStorage() -> downloadClicks()
        }
    }

    private fun downloadClicks() {
        when {
            isFrame -> downloadFrameClicks.onNext(binding.viewFrame)
            else -> file?.takeIf { file -> file.exists() }?.let { file -> downloadOriginalClicks.onNext(file) }
        }
    }

    override fun onResume() {
        initObservable()
        super.onResume()
    }

    private fun initObservable() {
        prefs
            .isUpgraded
            .asObservable()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribe { isUpgraded ->
                binding.imagePremiumOriginal.isVisible = !isUpgraded
                binding.iconWatchAd.isVisible = !isUpgraded && !isFrame
                binding.textDescription.isVisible = !isUpgraded && !isFrame
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
            binding.previewFrame1.load(file, errorRes = R.drawable.place_holder_image)
            binding.previewFrame2.load(file, errorRes = R.drawable.place_holder_image)
            binding.previewOriginal.load(file, errorRes = R.drawable.place_holder_image)
        }
    }

}