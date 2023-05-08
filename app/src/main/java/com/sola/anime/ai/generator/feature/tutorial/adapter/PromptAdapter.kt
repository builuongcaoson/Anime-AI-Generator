package com.sola.anime.ai.generator.feature.tutorial.adapter

import android.annotation.SuppressLint
import android.view.ViewGroup
import com.basic.common.base.LsAdapter
import com.basic.common.base.LsViewHolder
import com.basic.common.extension.clicks
import com.basic.common.extension.getDimens
import com.basic.common.extension.resolveAttrColor
import com.sola.anime.ai.generator.databinding.ItemPromptInTutorialBinding
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

class PromptAdapter @Inject constructor() : LsAdapter<String, ItemPromptInTutorialBinding>() {

    val clicks: Subject<Pair<String, Int>> = PublishSubject.create()
    var positionSelected = -1
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            if (field == value){
                return
            }

            field.takeIf { it != -1 }?.let { notifyItemChanged(it) }
            value.takeIf { it != -1 }?.let { notifyItemChanged(it) } ?: run {
                notifyDataSetChanged()
            }

            field = value
        }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LsViewHolder<ItemPromptInTutorialBinding> {
        return LsViewHolder(parent, ItemPromptInTutorialBinding::inflate)
    }

    override fun onBindViewHolder(
        holder: LsViewHolder<ItemPromptInTutorialBinding>,
        position: Int
    ) {
        val item = getItem(position)
        val binding = holder.binding
        val context = binding.root.context

        binding.display.text = item

        binding.viewClicks.setCardBackgroundColor(
            when (positionSelected) {
                position -> context.resolveAttrColor(android.R.attr.colorAccent)
                else -> context.resolveAttrColor(android.R.attr.colorBackground)
            }
        )
        binding.display.setTextColor(
            when (positionSelected) {
                position -> context.resolveAttrColor(com.google.android.material.R.attr.colorOnPrimary)
                else -> context.resolveAttrColor(android.R.attr.textColorPrimary)
            }
        )

        binding.viewClicks.clicks(withAnim = false){ clicks.onNext(item to position) }
    }

}