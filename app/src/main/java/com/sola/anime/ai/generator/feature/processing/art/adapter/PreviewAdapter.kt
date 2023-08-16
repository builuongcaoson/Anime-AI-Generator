package com.sola.anime.ai.generator.feature.processing.art.adapter

import android.content.Context
import coil.load
import coil.transition.CrossfadeTransition
import com.basic.common.base.LsAdapter
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.databinding.ItemPreviewArtProcessingBinding
import com.sola.anime.ai.generator.domain.model.config.process.Process
import javax.inject.Inject

class PreviewAdapter @Inject constructor(
    private val context: Context
) : LsAdapter<Process, ItemPreviewArtProcessingBinding>(ItemPreviewArtProcessingBinding::inflate) {

    var totalCount = 0

    fun insert(){
        totalCount += 1
        notifyItemInserted(totalCount - 1)
    }

    override fun bindItem(
        item: Process,
        binding: ItemPreviewArtProcessingBinding,
        position: Int
    ) {
        val item = getItem(position % data.size) ?: return

        binding.preview.load(item.preview) {
            crossfade(true)
        }

        binding.textTitle.text = item.title
        binding.textArtist.text = item.artist
    }

    override fun getItemCount(): Int {
        return totalCount
    }

}