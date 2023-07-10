package com.sola.anime.ai.generator.feature.pickAvatar

import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.basic.common.base.LsActivity
import com.basic.common.extension.clicks
import com.sola.anime.ai.generator.common.ConfigApp
import com.sola.anime.ai.generator.common.extension.back
import com.sola.anime.ai.generator.common.extension.startCredit
import com.sola.anime.ai.generator.common.extension.startIap
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.data.db.query.ModelDao
import com.sola.anime.ai.generator.databinding.ActivityModelBinding
import com.sola.anime.ai.generator.databinding.ActivityPickAvatarBinding
import com.sola.anime.ai.generator.feature.model.adapter.PreviewAdapter
import com.sola.anime.ai.generator.feature.pickAvatar.adapter.ObjectAdapter
import com.sola.anime.ai.generator.feature.pickAvatar.adapter.PhotoAdapter
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PickAvatarActivity : LsActivity<ActivityPickAvatarBinding>(ActivityPickAvatarBinding::inflate) {

    @Inject lateinit var objectAdapter: ObjectAdapter
    @Inject lateinit var photoAdapter: PhotoAdapter

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
        binding.viewCredit.clicks(withAnim = true) { startCredit() }
        binding.viewUpload.clicks(withAnim = true) {  }
    }

    private fun initData() {

    }

    private fun initObservable() {
        objectAdapter
            .clicks
            .autoDispose(scope())
            .subscribe {

            }
    }

    private fun initView() {
        binding.recyclerObject.apply {
            this.adapter = objectAdapter.apply {
                this.data = Object.values().toList()
                this.item = Object.values().firstOrNull()
            }
        }
        binding.recyclerView.apply {
            this.adapter = photoAdapter
        }
    }

    @Deprecated("Deprecated in Java", ReplaceWith("finish()"))
    override fun onBackPressed() {
        back()
    }

    enum class Object(val display: String, val prompt: List<String>){
        Woman(
            display = "Woman",
            listOf(
                "((masterpiece))), (((best quality))), ((ultra-detailed)), Beautiful girl",
                "((masterpiece))), (((best quality))), ((ultra-detailed)), Beautiful girl",
            )
        ),
        Man(
            display = "Man",
            listOf(
                "((masterpiece))), (((best quality))), ((ultra-detailed)), Handsome boy",
                "((masterpiece))), (((best quality))), ((ultra-detailed)), Handsome man",
            )
        )
    }

}