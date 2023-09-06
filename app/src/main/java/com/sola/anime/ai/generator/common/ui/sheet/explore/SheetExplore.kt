package com.sola.anime.ai.generator.common.ui.sheet.explore

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.sola.anime.ai.generator.common.base.LsBottomSheet
import com.sola.anime.ai.generator.common.ui.sheet.explore.adapter.GroupExploreAdapter
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.data.db.query.ExploreDao
import com.sola.anime.ai.generator.databinding.SheetExploreBinding
import com.sola.anime.ai.generator.domain.model.config.explore.Explore
import com.trello.rxlifecycle2.kotlin.bindToLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SheetExplore: LsBottomSheet<SheetExploreBinding>(SheetExploreBinding::inflate) {

    @Inject lateinit var groupExploreAdapter: GroupExploreAdapter
    @Inject lateinit var exploreDao: ExploreDao
    @Inject lateinit var prefs: Preferences

    var pairs: List<Pair<String, List<Explore>>> = listOf()
        set(value) {
            if (isAdded && isVisible){
                groupExploreAdapter.apply {
                    this.data = value
                }
            }
            field = value
        }
    var clicks: (Explore) -> Unit = {}
    var detailsClicks: (Explore) -> Unit = {}

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
        groupExploreAdapter
            .clicks
            .bindToLifecycle(binding.root)
            .subscribe { model -> clicks(model) }

        groupExploreAdapter
            .detailsClicks
            .bindToLifecycle(binding.root)
            .subscribe { model -> detailsClicks(model) }
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
                        this.adapter = groupExploreAdapter.apply {
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