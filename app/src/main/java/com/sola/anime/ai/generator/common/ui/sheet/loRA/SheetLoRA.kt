package com.sola.anime.ai.generator.common.ui.sheet.loRA

import android.annotation.SuppressLint
import com.sola.anime.ai.generator.common.base.LsBottomSheet
import com.sola.anime.ai.generator.common.ui.sheet.loRA.adapter.LoRAAdapter
import com.sola.anime.ai.generator.common.ui.sheet.model.adapter.ModelAdapter
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.data.db.query.LoRAGroupDao
import com.sola.anime.ai.generator.data.db.query.ModelDao
import com.sola.anime.ai.generator.databinding.SheetModelBinding
import com.sola.anime.ai.generator.domain.model.config.lora.LoRA
import com.sola.anime.ai.generator.domain.model.config.model.Model
import com.trello.rxlifecycle2.kotlin.bindToLifecycle
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SheetLoRA: LsBottomSheet<SheetModelBinding>(SheetModelBinding::inflate) {

    @Inject lateinit var loRAAdapter: LoRAAdapter
    @Inject lateinit var loRAGroupDao: LoRAGroupDao
    @Inject lateinit var prefs: Preferences

    var loRA: LoRA? = null
    var clicks: (LoRA) -> Unit = {}

    override fun onViewCreated() {
        initView()
        initObservable()
        initData()
    }

    @SuppressLint("AutoDispose", "CheckResult")
    private fun initObservable() {
        loRAAdapter
            .clicks
            .bindToLifecycle(binding.root)
            .subscribe { model -> clicks(model) }
    }

    private fun initData() {
        loRAGroupDao.getAllLive().observe(this){ loRAGroups ->
            loRAAdapter.apply {
                this.loRA = this@SheetLoRA.loRA
                this.data = loRAGroups.flatMap { it.childs }
            }
        }
    }

    private fun initView() {
        binding.recyclerView.apply {
            this.adapter = loRAAdapter
            this.itemAnimator = null
        }
    }

}