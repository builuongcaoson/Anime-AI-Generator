package com.sola.anime.ai.generator.common.ui.sheet.model

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.sola.anime.ai.generator.common.base.LsBottomSheet
import com.sola.anime.ai.generator.common.ui.sheet.model.adapter.GroupModelAdapter
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.data.db.query.ModelDao
import com.sola.anime.ai.generator.databinding.SheetModelBinding
import com.sola.anime.ai.generator.domain.model.config.model.Model
import com.trello.rxlifecycle2.kotlin.bindToLifecycle
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class SheetModel: LsBottomSheet<SheetModelBinding>(SheetModelBinding::inflate) {

    @Inject lateinit var groupModelAdapter: GroupModelAdapter
    @Inject lateinit var modelDao: ModelDao
    @Inject lateinit var prefs: Preferences

    var pairs: List<Pair<String, List<Model>>> = listOf()
        set(value) {
            if (isAdded && isVisible){
                groupModelAdapter.apply {
                    this.model = this@SheetModel.model
                    this.data = value
                }
            }
            field = value
        }
    var model: Model? = null
    var clicks: (Model) -> Unit = {}

    override fun onViewCreated() {
        initView()
        initObservable()
        initData()
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
        groupModelAdapter
            .clicks
            .bindToLifecycle(binding.root)
            .subscribe { model -> clicks(model) }
    }

    private fun initData() {
//        modelDao.getAllLive().observe(this){ models ->
//            val pairModelsFavourite = "Favourite" to models.filter { it.isFavourite }
//            val pairModelsOther = "Other" to models.filter { !it.isFavourite && !it.isDislike }
//            val pairModelsDislike = "Dislike" to models.filter { it.isDislike }
//
//            groupModelAdapter.apply {
//                this.model = this@SheetModel.model
//                this.data = listOf(pairModelsFavourite, pairModelsOther, pairModelsDislike)
//            }
//        }
    }

    private fun initView() {
        lifecycleScope.launch(Dispatchers.Main) {
            delay(250L)
            binding.recyclerView.apply {
                this.adapter = groupModelAdapter.apply {
                    this.model = this@SheetModel.model
                    this.data = pairs
                }
                this.itemAnimator = null
            }
            delay(250L)
            binding.recyclerView.animate().alpha(1f).setDuration(250L).start()
        }
    }

}