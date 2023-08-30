package com.sola.anime.ai.generator.common.ui.sheet.style

import com.sola.anime.ai.generator.common.base.LsBottomSheet
import com.sola.anime.ai.generator.common.ui.sheet.model.adapter.ModelAdapter
import com.sola.anime.ai.generator.common.ui.sheet.style.adapter.StyleAdapter
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.data.db.query.ModelDao
import com.sola.anime.ai.generator.data.db.query.StyleDao
import com.sola.anime.ai.generator.databinding.SheetModelBinding
import com.sola.anime.ai.generator.domain.model.config.model.Model
import com.sola.anime.ai.generator.domain.model.config.style.Style
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

@AndroidEntryPoint
class SheetStyle: LsBottomSheet<SheetModelBinding>(SheetModelBinding::inflate) {

    @Inject lateinit var styleAdapter: StyleAdapter
    @Inject lateinit var styleDao: StyleDao
    @Inject lateinit var prefs: Preferences

    var style: Style? = null
    var clicks: (Style) -> Unit = {}

    override fun onViewCreated() {
        initView()
        initData()
    }

    override fun onResume() {
        initObservable()
        super.onResume()
    }

    private fun initObservable() {
        styleAdapter
            .clicks
            .autoDispose(scope())
            .subscribe { model -> clicks(model) }
    }

    private fun initData() {
        styleDao.getAllLive().observe(this){ models ->
            styleAdapter.apply {
                this.style = this@SheetStyle.style
                this.data = models
            }
        }
    }

    private fun initView() {
        binding.recyclerView.apply {
            this.adapter = styleAdapter
            this.itemAnimator = null
        }
    }

}