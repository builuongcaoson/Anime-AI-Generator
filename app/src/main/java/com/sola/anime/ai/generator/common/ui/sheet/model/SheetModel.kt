package com.sola.anime.ai.generator.common.ui.sheet.model

import com.sola.anime.ai.generator.common.base.LsBottomSheet
import com.sola.anime.ai.generator.common.ui.sheet.model.adapter.ModelAdapter
import com.sola.anime.ai.generator.common.ui.sheet.model.adapter.ModelOrHeader
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.data.db.query.ModelDao
import com.sola.anime.ai.generator.databinding.SheetModelBinding
import com.sola.anime.ai.generator.domain.model.config.model.Model
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

@AndroidEntryPoint
class SheetModel: LsBottomSheet<SheetModelBinding>(SheetModelBinding::inflate) {

    @Inject lateinit var modelAdapter: ModelAdapter
    @Inject lateinit var modelDao: ModelDao
    @Inject lateinit var prefs: Preferences

    var model: Model? = null
    var clicks: (Model) -> Unit = {}

    override fun onViewCreated() {
        initView()
        initData()
    }

    override fun onResume() {
        initObservable()
        super.onResume()
    }

    private fun initObservable() {
        modelAdapter
            .clicks
            .autoDispose(scope())
            .subscribe { model -> clicks(model) }
    }

    private fun initData() {
        modelDao.getAllLive().observe(this){ models ->
            val modelsFavourite = models.filter { it.isFavourite }
            val modelsOther = models.filter { !it.isFavourite && !it.isDislike }
            val modelsDislike = models.filter { it.isDislike }

            modelAdapter.apply {
                this.model = this@SheetModel.model
                this.data = ArrayList(modelsFavourite).mapIndexed { index, model ->  }
            }
        }
    }

    private fun initView() {
        binding.recyclerView.apply {
            this.adapter = modelAdapter
            this.itemAnimator = null
        }
    }

}