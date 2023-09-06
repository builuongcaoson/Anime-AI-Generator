package com.sola.anime.ai.generator.common.ui.sheet.model.adapter

import android.view.View
import androidx.core.view.isVisible
import com.basic.common.base.LsAdapter
import com.basic.common.extension.clicks
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.common.extension.load
import com.sola.anime.ai.generator.databinding.ItemGroupModelBinding
import com.sola.anime.ai.generator.databinding.ItemModelBinding
import com.sola.anime.ai.generator.domain.model.config.model.Model
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

class GroupModelAdapter @Inject constructor(): LsAdapter<Pair<String, List<Model>>, ItemGroupModelBinding>(ItemGroupModelBinding::inflate) {

    val clicks: Subject<Model> = PublishSubject.create()
    val detailsClicks: Subject<Model> = PublishSubject.create()
    var model: Model? = null
        set(value) {
            if (field == value){
                return
            }

            if (value == null){
                val oldIndex = data.indexOfFirst { it.second.any { model -> model == field } }

                field = null

                oldIndex.takeIf { it != -1 }?.let { notifyItemChanged(oldIndex) }
                return
            }

            data.indexOfFirst { it.second.any { model -> model == field } }.takeIf { it != -1 }?.let { notifyItemChanged(it) }
            data.indexOfFirst { it.second.any { model -> model == field } }.takeIf { it != -1 }?.let { notifyItemChanged(it) }

            field = value
        }

    override fun bindItem(
        item: Pair<String, List<Model>>,
        binding: ItemGroupModelBinding,
        position: Int
    ) {
        binding.display.text = item.first
        binding.recyclerModel.apply {
            this.adapter = ModelAdapter().apply {
                this.clicks = this@GroupModelAdapter.clicks
                this.detailsClicks = this@GroupModelAdapter.detailsClicks
                this.model = this@GroupModelAdapter.model
                this.data = item.second
                this.emptyView = binding.viewEmpty
            }
        }
    }

}

class ModelAdapter: LsAdapter<Model, ItemModelBinding>(ItemModelBinding::inflate){

    var clicks: Subject<Model> = PublishSubject.create()
    var detailsClicks: Subject<Model> = PublishSubject.create()
    var model: Model? = null
        set(value) {
            if (field == value){
                return
            }

            if (value == null){
                val oldIndex = data.indexOf(field)

                field = null

                oldIndex.takeIf { it != -1 }?.let { notifyItemChanged(oldIndex) }
                return
            }

            data.indexOf(field).takeIf { it != -1 }?.let { notifyItemChanged(it) }
            data.indexOf(value).takeIf { it != -1 }?.let { notifyItemChanged(it) }

            field = value
        }

    override fun bindItem(item: Model, binding: ItemModelBinding, position: Int) {
        binding.preview.load(item.preview, errorRes = R.drawable.place_holder_image) { drawable ->
            binding.viewShadow.isVisible = drawable != null
        }
        binding.display.text = item.display
        binding.viewSelected.isVisible = item == model

        binding.viewPreview.clicks(withAnim = false) { clicks.onNext(item) }
        binding.viewDetails.clicks { detailsClicks.onNext(item) }
    }

}