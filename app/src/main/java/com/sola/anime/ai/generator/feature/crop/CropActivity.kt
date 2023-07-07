package com.sola.anime.ai.generator.feature.crop

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.basic.common.base.LsActivity
import com.basic.common.extension.clicks
import com.basic.common.extension.makeToast
import com.sola.anime.ai.generator.common.ConfigApp
import com.sola.anime.ai.generator.common.extension.back
import com.sola.anime.ai.generator.common.extension.startIap
import com.sola.anime.ai.generator.databinding.ActivityCropBinding
import com.sola.anime.ai.generator.feature.crop.adapter.AspectRatioAdapter
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import com.yalantis.ucrop.callback.BitmapCropCallback
import com.yalantis.ucrop.view.TransformImageView
import com.yalantis.ucrop.view.widget.HorizontalProgressWheelView
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.io.File
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class CropActivity : LsActivity<ActivityCropBinding>(ActivityCropBinding::inflate) {

    companion object {
        private const val ANIMATION_DURATION = 250L
        private const val COMPRESS_QUALITY = 100
    }

    @Inject lateinit var configApp: ConfigApp
    @Inject lateinit var aspectRatioAdapter: AspectRatioAdapter

    private val cropImageView by lazy { binding.cropView.cropImageView }
    private val uri by lazy { intent.data }
    private var isLoadedCrop = false
    private var isFlip = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        if (uri == null) {
            makeToast("Not found photo, please check again!")
            back()
            return
        }

        initView(uri!!)
        initObservable()
        initData()
        listenerView()
    }

    private fun listenerView() {
        binding.back.clicks { onBackPressed() }
        binding.rotate.clicks {
            binding.rotate.binding.imageIcon.animRotate90(ANIMATION_DURATION)

            cropImageView.rotate90()
            cropImageView.setImageToWrapCropBounds()
        }
        binding.mirror.clicks {
            isFlip = !isFlip

            binding.mirror.binding.imageIcon.animFlipY(ANIMATION_DURATION)

            cropImageView.flip()
            cropImageView.setImageToWrapCropBounds()
        }
        binding.crop.clicks {
            binding.viewAspectRatioCrop.isVisible = true
            binding.viewClicksAspectRatioCrop.isVisible = true
        }
        binding.rotateWheelView.setScrollingListener(object: HorizontalProgressWheelView.ScrollingListener {
            override fun onScrollStart() {
                cropImageView.cancelAllAnimations()
            }
            override fun onScroll(delta: Float, totalDistance: Float) {
                cropImageView.postRotate(Math.toRadians(delta.toDouble()).toFloat())
            }
            override fun onScrollEnd() {
                cropImageView.setImageToWrapCropBounds()
            }
        })
        binding.viewClicksAspectRatioCrop.clicks(withAnim = false) {
            binding.viewAspectRatioCrop.isVisible = false
            binding.viewClicksAspectRatioCrop.isVisible = false
        }
        binding.tick.clicks { tickClicks() }
    }

    private fun tickClicks() {
        binding.viewLoading.isVisible = true

        binding
            .groupCropView
            .animate()
            .alpha(0f)
            .setDuration(500)
            .start()

        binding.cropView
            .animate()
            .alpha(0f)
            .setDuration(500)
            .withEndAction {
                if (isFlip){
                    cropImageView.rotate180()
                }

                cropImageView
                    .cropAndSaveImage(
                        Bitmap.CompressFormat.PNG,
                        COMPRESS_QUALITY,
                        object: BitmapCropCallback {
                            override fun onBitmapCropped(
                                resultUri: Uri,
                                offsetX: Int,
                                offsetY: Int,
                                imageWidth: Int,
                                imageHeight: Int
                            ) {
                                binding.viewLoading.isVisible = false

                                val intent = Intent().apply {
                                    this.data = resultUri
                                    this.putExtra("ratioDisplay", aspectRatioAdapter.aspectRatioSelect.display)
                                }
                                setResult(Activity.RESULT_OK, intent)
                                back()
                            }

                            override fun onCropFailure(t: Throwable) {
                                binding.viewLoading.isVisible = false

                                setResult(Activity.RESULT_CANCELED)
                                makeToast("Crop failed, please try again or report it to us!")
                                back()
                            }
                        })
            }
            .start()
    }

    private fun initData() {

    }

    private fun initObservable() {
        aspectRatioAdapter
            .clicks
            .autoDispose(scope())
            .subscribe {
                aspectRatioAdapter.aspectRatioSelect = it

                cropImageView.targetAspectRatio = it.aspectRatio
                cropImageView.setImageToWrapCropBounds()
            }
    }

    private fun initView(uri: Uri) {
        cropImageView.apply {
            setTransformImageListener(transformImageListener)
            setImageUri(uri, Uri.fromFile(File(filesDir, "${System.currentTimeMillis()}.png")))
            isRotateEnabled = true
            targetAspectRatio = AspectRatioAdapter.AspectRatio.OneToOne.aspectRatio
        }

        binding.recyclerAspectRadioCrop.apply {
            layoutManager = LinearLayoutManager(this@CropActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = aspectRatioAdapter
        }
    }

    private val transformImageListener = object: TransformImageView.TransformImageListener {
        override fun onLoadComplete() {
            binding
                .cropView
                .animate()
                .alpha(1f)
                .setDuration(500)
                .setStartDelay(500)
                .withEndAction {
                    isLoadedCrop = true
                }
                .start()
        }
        override fun onLoadFailure(e: java.lang.Exception) {
            Timber.e("onLoadFailure: $e")
            markFailed()
        }
        override fun onRotate(currentAngle: Float) {
            if (!isLoadedCrop) return

            binding.displayRotate.text = String.format(Locale.getDefault(), "%.1f°", currentAngle)

            binding.tick.isVisible = true
        }
        override fun onScale(currentScale: Float) {
            if (!isLoadedCrop) return

            binding.tick.isVisible = true
        }
    }

    private fun View.animFlipY(duration: Long) {
        animate()
            .rotationY(rotationY - 180f)
            .setDuration(duration)
            .start()
    }

    private fun View.animRotate90(duration: Long) {
        animate()
            .rotation(rotation - 180f)
            .setDuration(duration)
            .start()
    }

    private fun markFailed() {
        setResult(Activity.RESULT_CANCELED)
        makeToast("An error occurred, please try again or report it to us!")
        back()
    }

    @Deprecated("Deprecated in Java", ReplaceWith("finish()"))
    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED)
        back()
    }

}