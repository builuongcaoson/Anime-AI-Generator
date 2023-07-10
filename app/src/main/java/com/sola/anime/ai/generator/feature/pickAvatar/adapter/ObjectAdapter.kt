package com.sola.anime.ai.generator.feature.pickAvatar.adapter

import com.basic.common.base.LsAdapter
import com.basic.common.extension.clicks
import com.basic.common.extension.resolveAttrColor
import com.sola.anime.ai.generator.databinding.ItemObjectInAvatarBinding
import com.sola.anime.ai.generator.feature.pickAvatar.PickAvatarActivity
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

class ObjectAdapter @Inject constructor(): LsAdapter<PickAvatarActivity.Object, ItemObjectInAvatarBinding>(ItemObjectInAvatarBinding::inflate) {

    val clicks: Subject<PickAvatarActivity.Object> = PublishSubject.create()
    var item: PickAvatarActivity.Object? = null
        set(value) {
            if (field == value){
                return
            }

            val oldIndex = data.indexOf(field)
            val newIndex = data.indexOf(value)

            notifyItemChanged(oldIndex)
            notifyItemChanged(newIndex)

            field = value
        }

    override fun bindItem(item: PickAvatarActivity.Object, binding: ItemObjectInAvatarBinding, position: Int) {
        val context = binding.root.context

        binding.display.text = item.display

        when (this.item) {
            item -> {
                binding.display.setTextColor(context.resolveAttrColor(com.google.android.material.R.attr.colorOnPrimary))
                binding.viewClicks.setCardBackgroundColor(context.resolveAttrColor(android.R.attr.colorAccent))
            }
            else -> {
                binding.display.setTextColor(context.resolveAttrColor(android.R.attr.textColorPrimary))
                binding.viewClicks.setCardBackgroundColor(context.resolveAttrColor(android.R.attr.colorBackground))
            }
        }

        binding.viewClicks.clicks(withAnim = false) { clicks.onNext(item) }
    }

}