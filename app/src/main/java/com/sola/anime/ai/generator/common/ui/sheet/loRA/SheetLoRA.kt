package com.sola.anime.ai.generator.common.ui.sheet.loRA

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.sola.anime.ai.generator.common.base.LsBottomSheet
import com.sola.anime.ai.generator.common.ui.sheet.loRA.adapter.GroupLoRAAdapter
import com.sola.anime.ai.generator.common.ui.sheet.loRA.adapter.LoRAAdapter
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.data.db.query.LoRAGroupDao
import com.sola.anime.ai.generator.databinding.SheetLoraBinding
import com.sola.anime.ai.generator.domain.model.LoRAPreview
import com.sola.anime.ai.generator.domain.model.config.lora.LoRA
import com.sola.anime.ai.generator.domain.model.config.model.Model
import com.trello.rxlifecycle2.kotlin.bindToLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SheetLoRA: LsBottomSheet<SheetLoraBinding>(SheetLoraBinding::inflate) {

    @Inject lateinit var groupLoRAAdapter: GroupLoRAAdapter
    @Inject lateinit var loRAGroupDao: LoRAGroupDao
    @Inject lateinit var prefs: Preferences

    var pairs: List<Pair<String, List<LoRAPreview>>> = listOf()
        set(value) {
            if (isAdded && isVisible){
                groupLoRAAdapter.apply {
                    this.loRA = this@SheetLoRA.loRA
                    this.data = value
                }
            }
            field = value
        }
    var loRA: LoRA? = null
    var clicks: (LoRA) -> Unit = {}
    var detailsClicks: (LoRAPreview) -> Unit = {}

    override fun onViewCreated() {
        initView()
        initObservable()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme)
        dialog.setOnShowListener {
            val bottomSheetDialog = it as BottomSheetDialog
            val parentLayout = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            parentLayout?.let {
                val behaviour = BottomSheetBehavior.from(parentLayout)
                setupFullHeight(parentLayout)
                behaviour.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
        return dialog
    }

    private fun setupFullHeight(bottomSheet: View) {
        val layoutParams = bottomSheet.layoutParams
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
        bottomSheet.layoutParams = layoutParams
    }

    @SuppressLint("AutoDispose", "CheckResult")
    private fun initObservable() {
        groupLoRAAdapter
            .clicks
            .bindToLifecycle(binding.root)
            .subscribe { loRAPreview -> clicks(loRAPreview.loRA) }

        groupLoRAAdapter
            .detailsClicks
            .bindToLifecycle(binding.root)
            .subscribe { loRAPreview -> detailsClicks(loRAPreview) }
    }

    private fun initView() {
        lifecycleScope.launch(Dispatchers.Main) {
            delay(250L)
            binding.viewLoading
                .animate()
                .alpha(0f)
                .setDuration(250L)
                .withEndAction {
                    binding.recyclerView.apply {
                        this.adapter = groupLoRAAdapter.apply {
                            this.loRA = this@SheetLoRA.loRA
                            this.data = pairs
                        }
                        this.itemAnimator = null
                    }
                    binding.recyclerView.animate().alpha(1f).setDuration(250L).start()
                }
                .start()
        }
    }

}