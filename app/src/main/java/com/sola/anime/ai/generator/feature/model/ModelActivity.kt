package com.sola.anime.ai.generator.feature.model

import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.basic.common.base.LsActivity
import com.basic.common.extension.clicks
import com.sola.anime.ai.generator.common.ConfigApp
import com.sola.anime.ai.generator.common.extension.back
import com.sola.anime.ai.generator.common.extension.startIap
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.data.db.query.ModelDao
import com.sola.anime.ai.generator.databinding.ActivityModelBinding
import com.sola.anime.ai.generator.feature.model.adapter.PreviewAdapter
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ModelActivity : LsActivity<ActivityModelBinding>(ActivityModelBinding::inflate) {

    @Inject lateinit var previewAdapter: PreviewAdapter
    @Inject lateinit var modelDao: ModelDao
    @Inject lateinit var configApp: ConfigApp
    @Inject lateinit var prefs: Preferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initView()
        initObservable()
        initData()
        listenerView()
    }

    private fun listenerView() {
        binding.back.clicks { onBackPressed() }
        binding.recyclerView.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val alpha = dy.toFloat() / binding.viewShadow.height.toFloat()

                binding.viewShadow.alpha = alpha
            }
        })
    }

    private fun initData() {
        modelDao.getAllLive().observe(this){
            previewAdapter.data = it

            previewAdapter.model = configApp.modelChoice
        }
    }

    private fun initObservable() {
        previewAdapter
            .clicks
            .autoDispose(scope())
            .subscribe { model ->
                when {
                    model.premium && !prefs.isUpgraded.get() -> startIap()
                    else -> {
                        configApp.modelChoice = model

                        back()
                    }
                }
            }
    }

    private fun initView() {
        binding.recyclerView.apply {
            this.layoutManager = GridLayoutManager(this@ModelActivity, 3, GridLayoutManager.VERTICAL, false)
            this.adapter = previewAdapter
        }
    }

    @Deprecated("Deprecated in Java", ReplaceWith("finish()"))
    override fun onBackPressed() {
        back()
    }

}