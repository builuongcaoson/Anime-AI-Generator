package com.sola.anime.ai.generator.feature.main.text

import android.view.LayoutInflater
import android.view.ViewGroup
import com.basic.common.base.LsAdapter
import com.basic.common.base.LsViewHolder
import com.sola.anime.ai.generator.R
import javax.inject.Inject

class AspectRatioAdapter @Inject constructor(): LsAdapter<Unit>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LsViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.item_aspect_ratio, parent, false)
        return LsViewHolder(view)
    }

    override fun onBindViewHolder(holder: LsViewHolder, position: Int) {

    }

    override fun getItemCount(): Int {
        return 5
    }

}