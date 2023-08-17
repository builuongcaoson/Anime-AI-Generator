package com.sola.anime.ai.generator.feature.detailModelOrLoRA

import android.os.Bundle
import coil.load
import com.basic.common.base.LsActivity
import com.basic.common.extension.clicks
import com.basic.common.extension.getColorCompat
import com.basic.common.extension.lightStatusBar
import com.basic.common.extension.transparent
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.common.extension.back
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.data.db.query.LoRAGroupDao
import com.sola.anime.ai.generator.data.db.query.ModelDao
import com.sola.anime.ai.generator.databinding.ActivityDetailModelOrLoraBinding
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class DetailModelOrLoRAActivity : LsActivity<ActivityDetailModelOrLoraBinding>(ActivityDetailModelOrLoraBinding::inflate) {

    companion object {
        const val MODEL_ID_EXTRA = "MODEL_ID_EXTRA"
        const val LORA_GROUP_ID_EXTRA = "LORA_GROUP_ID_EXTRA"
        const val LORA_ID_EXTRA = "LORA_ID_EXTRA"
    }

    @Inject lateinit var modelDao: ModelDao
    @Inject lateinit var loRAGroupDao: LoRAGroupDao
    @Inject lateinit var prefs: Preferences

    private val modelId by lazy { intent.getLongExtra(MODEL_ID_EXTRA, -1) }
    private val loRAGroupId by lazy { intent.getLongExtra(LORA_GROUP_ID_EXTRA, -1) }
    private val loRAId by lazy { intent.getLongExtra(LORA_ID_EXTRA, -1) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transparent()
        lightStatusBar()
        setContentView(binding.root)

        initView()
        initObservable()
        initData()
        listenerView()
    }

    private fun listenerView() {
        binding.back.clicks { onBackPressed() }
    }

    private fun initData() {

    }

    private fun initObservable() {

    }

    private fun initView() {
        when {
            modelId != -1L -> initModelView()
            loRAGroupId != -1L -> initLoRAView()
        }

        Timber.e("Model id: $modelId")
        Timber.e("LoRA Group id: $loRAGroupId")
        Timber.e("LoRA id: $loRAId")
    }

    private fun initLoRAView() {
        loRAGroupDao.findById(loRAGroupId)?.let { loRAGroup ->
            val loRA = loRAGroup.childs.find { it.id == loRAId } ?: return

            binding.preview.load(loRA.previews.firstOrNull()) {
                listener(
                    onSuccess = { _, result ->
                        binding.preview.setImageDrawable(result.drawable)
                        binding.preview.animate().alpha(1f).setDuration(250).start()
                        binding.viewShadow.animate().alpha(1f).setDuration(250).start()
                    }
                )
                crossfade(true)
                error(R.drawable.place_holder_image)
            }

            binding.viewTry.setCardBackgroundColor(getColorCompat(R.color.red))
            binding.display.text = loRA.display
            val favouriteCount = if (prefs.getFavouriteCountLoRAId(loRAId = loRA.id)) loRA.favouriteCount + 1 else loRA.favouriteCount
            binding.favouriteCount.text = "$favouriteCount Uses"
        }
    }

    private fun initModelView() {
        modelDao.findById(modelId)?.let { model ->
            binding.preview.load(model.preview) {
                listener(
                    onSuccess = { _, result ->
                        binding.preview.setImageDrawable(result.drawable)
                        binding.preview.animate().alpha(1f).setDuration(250).start()
                        binding.viewShadow.animate().alpha(1f).setDuration(250).start()
                    }
                )
                crossfade(true)
                error(R.drawable.place_holder_image)
            }

            binding.viewTry.setCardBackgroundColor(getColorCompat(R.color.blue))
            binding.display.text = model.display
            val favouriteCount = if (prefs.getFavouriteCountModelId(modelId = model.id)) model.favouriteCount + 1 else model.favouriteCount
            binding.favouriteCount.text = "$favouriteCount Uses"
        }
    }

    @Deprecated("Deprecated in Java", ReplaceWith("finish()"))
    override fun onBackPressed() {
        back()
    }

}