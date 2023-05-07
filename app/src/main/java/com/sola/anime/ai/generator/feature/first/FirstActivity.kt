package com.sola.anime.ai.generator.feature.first

import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.basic.common.base.LsActivity
import com.basic.common.extension.clicks
import com.sola.anime.ai.generator.common.ConfigApp
import com.sola.anime.ai.generator.common.extension.back
import com.sola.anime.ai.generator.data.db.query.StyleDao
import com.sola.anime.ai.generator.databinding.ActivityFirstBinding
import com.sola.anime.ai.generator.databinding.ActivityStyleBinding
import com.sola.anime.ai.generator.feature.style.adapter.PreviewAdapter
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class FirstActivity : LsActivity() {

    private val binding by lazy { ActivityFirstBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

    }

    @Deprecated("Deprecated in Java", ReplaceWith("finish()"))
    override fun onBackPressed() {
        back()
    }

}