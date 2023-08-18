package com.sola.anime.ai.generator.feature.detailModelOrLoRA

import android.os.Bundle
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import coil.load
import com.basic.common.base.LsActivity
import com.basic.common.extension.*
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.common.extension.back
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.data.db.query.ExploreDao
import com.sola.anime.ai.generator.data.db.query.LoRAGroupDao
import com.sola.anime.ai.generator.data.db.query.ModelDao
import com.sola.anime.ai.generator.databinding.ActivityDetailModelOrLoraBinding
import com.sola.anime.ai.generator.domain.model.ExploreOrLoRA
import com.sola.anime.ai.generator.domain.model.config.lora.LoRA
import com.sola.anime.ai.generator.domain.model.config.lora.LoRAGroup
import com.sola.anime.ai.generator.domain.model.config.model.Model
import com.sola.anime.ai.generator.feature.detailModelOrLoRA.adapter.ExploreOrLoRAAdapter
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class DetailModelOrLoRAActivity : LsActivity<ActivityDetailModelOrLoraBinding>(ActivityDetailModelOrLoraBinding::inflate) {

    companion object {
        const val MODEL_ID_EXTRA = "MODEL_ID_EXTRA"
        const val LORA_GROUP_ID_EXTRA = "LORA_GROUP_ID_EXTRA"
        const val LORA_ID_EXTRA = "LORA_ID_EXTRA"
    }

    @Inject lateinit var modelDao: ModelDao
    @Inject lateinit var loRAGroupDao: LoRAGroupDao
    @Inject lateinit var prefs: Preferences
    @Inject lateinit var exploreOrLoRAAdapter: ExploreOrLoRAAdapter
    @Inject lateinit var exploreDao: ExploreDao

    private val subjectDataExploreOrLoRAChanges: Subject<List<ExploreOrLoRA>> = PublishSubject.create()

    private val modelId by lazy { intent.getLongExtra(MODEL_ID_EXTRA, -1) }
    private val loRAGroupId by lazy { intent.getLongExtra(LORA_GROUP_ID_EXTRA, -1) }
    private val loRAId by lazy { intent.getLongExtra(LORA_ID_EXTRA, -1) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transparent()
        setContentView(binding.root)

        initView()
        initObservable()
        initData()
        listenerView()
    }

    private fun listenerView() {
        binding.back.clicks { onBackPressed() }
        binding.save.clicks {  }
        binding.dislike.clicks {  }
        binding.report.clicks {  }
    }

    private fun initData() {

    }

    private fun initObservable() {
        subjectDataExploreOrLoRAChanges
            .debounce(500, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribe { dataExploreOrLoRA ->
                Timber.e("Data size: ${dataExploreOrLoRA.size}")

                lifecycleScope.launch(Dispatchers.Main) {
                    exploreOrLoRAAdapter.data = dataExploreOrLoRA.shuffled()
                    delay(500)
                    binding.loadingExploreOrLoRA.animate().alpha(0f).setDuration(250).start()
                    binding.recyclerExploreOrLoRA.animate().alpha(1f).setDuration(250).start()
                }
            }
    }

    private fun initView() {
        when {
            modelId != -1L -> initModelView()
            loRAGroupId != -1L -> initLoRAView()
            else -> {
                lifecycleScope.launch(Dispatchers.Main) {
                    delay(1000)

                    makeToast("Something wrong, please try again!")
                    finish()
                }
                return
            }
        }

        binding.recyclerExploreOrLoRA.adapter = exploreOrLoRAAdapter
    }

    private fun initLoRAView() {
        loRAGroupDao.findById(loRAGroupId)?.let { loRAGroup ->
            val loRA = loRAGroup.childs.find { it.id == loRAId } ?: return

            ConstraintSet().apply {
                this.clone(binding.viewPreview)
                this.setDimensionRatio(binding.preview.id, "3:4")
                this.applyTo(binding.viewPreview)
            }

            binding.preview.load(loRA.previews.firstOrNull()) {
                listener(
                    onSuccess = { _, result ->
                        binding.preview.setImageDrawable(result.drawable)
                        binding.preview.animate().alpha(1f).setDuration(250).start()
                        binding.viewShadow.animate().alpha(1f).setDuration(250).start()
                    }
                )
                crossfade(true)
                error(R.drawable.place_holder_image)
            }

            binding.viewDetail.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                this.bottomMargin = getDimens(com.intuit.sdp.R.dimen._70sdp).toInt()
            }

            binding.modelOrLoRA.text = "LoRA"
            binding.use.text = "Use this LoRA"
            binding.note.text = "Artwork made by this LoRA"
            binding.imgModelOrLoRA.setImageResource(R.drawable.star_of_david)
            binding.viewModelOrLoRA.setCardBackgroundColor(getColorCompat(R.color.red))
            binding.viewUse.setCardBackgroundColor(getColorCompat(R.color.red))
            binding.display.text = loRA.display
            val favouriteCount = if (prefs.getFavouriteCountLoRAId(loRAId = loRA.id)) loRA.favouriteCount + 1 else loRA.favouriteCount
            binding.favouriteCount.text = "$favouriteCount Uses"

            initLoRAData(loRAGroup = loRAGroup, loRA = loRA)
        }
    }

    private fun initLoRAData(loRAGroup: LoRAGroup, loRA: LoRA) {
        lifecycleScope.launch(Dispatchers.Main) {
            delay(500)
            subjectDataExploreOrLoRAChanges.onNext(loRAGroup.childs.filter { it.id != loRA.id }.map { loRA -> ExploreOrLoRA(loRA = loRA, ratio = listOf("1:1", "2:3", "3:4").random()) })
        }
    }

    private fun initModelView() {
        modelDao.findById(modelId)?.let { model ->
            ConstraintSet().apply {
                this.clone(binding.viewPreview)
                this.setDimensionRatio(binding.preview.id, "1:1")
                this.applyTo(binding.viewPreview)
            }

            binding.preview.load(model.preview) {
                listener(
                    onSuccess = { _, result ->
                        binding.preview.setImageDrawable(result.drawable)
                        binding.preview.animate().alpha(1f).setDuration(250).start()
                        binding.viewShadow.animate().alpha(1f).setDuration(250).start()
                    }
                )
                crossfade(true)
                error(R.drawable.place_holder_image)
            }

            binding.viewDetail.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                this.bottomMargin = getDimens(com.intuit.sdp.R.dimen._30sdp).toInt()
            }

            binding.modelOrLoRA.text = "Model"
            binding.use.text = "Use this Model"
            binding.note.text = "Artwork made by this Model"
            binding.imgModelOrLoRA.setImageResource(R.drawable.user_robot)
            binding.viewModelOrLoRA.setCardBackgroundColor(getColorCompat(R.color.blue))
            binding.viewUse.setCardBackgroundColor(getColorCompat(R.color.blue))
            binding.display.text = model.display
            val favouriteCount = if (prefs.getFavouriteCountModelId(modelId = model.id)) model.favouriteCount + 1 else model.favouriteCount
            binding.favouriteCount.text = "$favouriteCount Uses"

            initExploreData(model)
        }
    }

    private fun initExploreData(model: Model) {
        exploreDao.getAllLive().observe(this) { explores ->
            subjectDataExploreOrLoRAChanges.onNext(explores.filter { explore -> explore.modelIds.contains(model.id) }.map { explore -> ExploreOrLoRA(explore = explore, ratio = explore.ratio) })
        }
    }

    @Deprecated("Deprecated in Java", ReplaceWith("finish()"))
    override fun onBackPressed() {
        back()
    }

}