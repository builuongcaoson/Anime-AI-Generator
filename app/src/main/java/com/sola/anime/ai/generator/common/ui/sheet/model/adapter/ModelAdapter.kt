package com.sola.anime.ai.generator.common.ui.sheet.model.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.basic.common.extension.clicks
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.common.extension.load
import com.sola.anime.ai.generator.databinding.ItemPreviewModelBinding
import com.sola.anime.ai.generator.domain.model.config.model.Model
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

class ModelAdapter @Inject constructor(): RecyclerView.Adapter<ModelAdapter.ViewHolder>() {

    companion object {
        private const val HEADER_TYPE = 0
        private const val ITEM_TYPE = 1
    }

    var data = listOf<ModelOrHeader>()

    val clicks: Subject<Model> = PublishSubject.create()
    var model: Model? = null
        set(value) {
            if (field == value){
                return
            }

            if (value == null){
                val oldIndex = data.indexOfFirst { it.model == field }

                field = null

                oldIndex.takeIf { it != -1 }?.let { notifyItemChanged(oldIndex) }
                return
            }

            data.indexOfFirst { it.model == field }.takeIf { it != -1 }?.let { notifyItemChanged(it) }
            data.indexOfFirst { it.model == value }.takeIf { it != -1 }?.let { notifyItemChanged(it) }

            field = value
        }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = when (viewType) {
            HEADER_TYPE -> inflater.inflate(R.layout.item_text_header, parent, false)
            else -> inflater.inflate(R.layout.item_preview_model, parent, false)
        }
        return ViewHolder(view)
    }

    override fun getItemViewType(position: Int): Int {
        return if (data.getOrNull(position)?.display == null) HEADER_TYPE else ITEM_TYPE
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            HEADER_TYPE -> {

            }
            ITEM_TYPE -> {
                val item = data.getOrNull(position)?.model ?: return
                val binding = ItemPreviewModelBinding.bind(holder.itemView)

                binding.preview.load(item.preview, errorRes = R.drawable.place_holder_image)
                binding.display.text = item.display
                binding.viewSelected.isVisible = item.id == model?.id
                binding.viewDescription.visibility = if (item.description.isNotEmpty()) View.VISIBLE else View.INVISIBLE
                binding.description.text = item.description

                binding.viewPreview.clicks { clicks.onNext(item) }
            }
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

}

class ModelOrHeader(val display: String ?= null, val model: Model? = null)