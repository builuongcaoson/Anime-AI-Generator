package com.basic.common.base

import android.annotation.SuppressLint
import android.provider.CallLog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.basic.common.extension.tryOrNull
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject

abstract class LsAdapter<T, VB: ViewBinding>(
    val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> VB,
    private val isNotifyDataChanged: Boolean = true
) : RecyclerView.Adapter<LsViewHolder<VB>>() {

    var data: List<T> = listOf()
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            if (field === value) return
            field = value
            emptyView?.isVisible = data.isEmpty()
            if (isNotifyDataChanged) notifyDataSetChanged()
        }
    var emptyView: View? = null
        set(value) {
            field = value
            value?.isVisible = data.isEmpty()
        }

    abstract fun bindItem(item: T, binding: VB, position: Int)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LsViewHolder<VB> {
        return LsViewHolder(bindingInflater(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: LsViewHolder<VB>, position: Int) {
        val item = getItem(position) ?: return
        bindItem(item, holder.binding, position)
    }

    fun getItem(position: Int): T? {
        return data.getOrNull(position)
    }

    override fun getItemCount(): Int {
        return data.size
    }

}