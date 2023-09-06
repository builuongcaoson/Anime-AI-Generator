package com.sola.anime.ai.generator.common.ui.sheet.style

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.sola.anime.ai.generator.common.base.LsBottomSheet
import com.sola.anime.ai.generator.common.ui.sheet.style.adapter.GroupStyleAdapter
import com.sola.anime.ai.generator.common.ui.sheet.style.adapter.StyleAdapter
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.data.db.query.StyleDao
import com.sola.anime.ai.generator.databinding.SheetStyleBinding
import com.sola.anime.ai.generator.domain.model.config.model.Model
import com.sola.anime.ai.generator.domain.model.config.style.Style
import com.trello.rxlifecycle2.kotlin.bindToLifecycle
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SheetStyle: LsBottomSheet<SheetStyleBinding>(SheetStyleBinding::inflate) {

    @Inject lateinit var groupStyleAdapter: GroupStyleAdapter
    @Inject lateinit var styleDao: StyleDao
    @Inject lateinit var prefs: Preferences

    var pairs: List<Pair<String, List<Style>>> = listOf()
        set(value) {
            if (isAdded && isVisible){
                groupStyleAdapter.apply {
                    this.style = this@SheetStyle.style
                    this.data = value
                }
            }
            field = value
        }
    var style: Style? = null
    var clicks: (Style) -> Unit = {}

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
        groupStyleAdapter
            .clicks
            .bindToLifecycle(binding.root)
            .subscribe { style -> clicks(style) }
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
                        this.adapter = groupStyleAdapter.apply {
                            this.style = this@SheetStyle.style
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