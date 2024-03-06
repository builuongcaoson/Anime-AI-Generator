package com.basic.common.base

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class LsPageAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    private val fragments = arrayListOf<Fragment>()

    fun addFragment(vararg fragments: Fragment){
        this.fragments.addAll(fragments)
    }

    override fun getItemCount() = fragments.size

    override fun createFragment(position: Int) = fragments[position]

}