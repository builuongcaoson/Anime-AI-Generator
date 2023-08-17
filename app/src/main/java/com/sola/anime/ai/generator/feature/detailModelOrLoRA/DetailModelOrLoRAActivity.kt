package com.sola.anime.ai.generator.feature.detailModelOrLoRA

import android.os.Bundle
import coil.load
import com.basic.common.base.LsActivity
import com.basic.common.extension.transparent
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.common.extension.back
import com.sola.anime.ai.generator.data.db.query.LoRAGroupDao
import com.sola.anime.ai.generator.data.db.query.ModelDao
import com.sola.anime.ai.generator.databinding.ActivityDetailModelOrLoraBinding
import com.sola.anime.ai.generator.domain.model.config.model.Model
import dagger.hilt.android.AndroidEntryPoint
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

    private val modelId by lazy { intent.getLongExtra(MODEL_ID_EXTRA, -1) }
    private val loRAGroupId by lazy { intent.getLongExtra(LORA_GROUP_ID_EXTRA, -1) }
    private val loRAId by lazy { intent.getLongExtra(LORA_ID_EXTRA, -1) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transparent()
        setContentView(binding.root)

        initView()
        initObservable()
        initData()
        listenerView()
    }

    private fun listenerView() {

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
    }

    private fun initLoRAView() {
        loRAGroupDao.findById(loRAGroupId)?.let { loRAGroup ->
            binding.preview.load(loRAGroup.childs.find { it.id == loRAId }) {
                crossfade(true)
                error(R.drawable.place_holder_image)
            }
        }
    }

    private fun initModelView() {
        modelDao.findById(modelId)?.let { model ->
            binding.preview.load(model.preview) {
                crossfade(true)
                error(R.drawable.place_holder_image)
            }
        }
    }

    @Deprecated("Deprecated in Java", ReplaceWith("finish()"))
    override fun onBackPressed() {
        back()
    }

}