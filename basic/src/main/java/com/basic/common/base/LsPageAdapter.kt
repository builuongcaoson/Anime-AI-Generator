package com.basic.common.base

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

class LsPageAdapter(manager: FragmentManager) : FragmentStatePagerAdapter(manager) {

    private val fragments = arrayListOf<Fragment>()
    private val titles = arrayListOf<String>()

    override fun getItem(position: Int): Fragment {
        return this.fragments[position]
    }

    override fun getCount(): Int {
        return this.fragments.size
    }

    fun addFragment(fragment: Fragment, title: String? = null) {
        this.fragments.add(fragment)
        title?.let { this.titles.add(it) }
    }

    fun addFragment(vararg fragments: Fragment){
        this.fragments.addAll(fragments)
    }

    override fun getPageTitle(position: Int): CharSequence {
        if (this.titles.isEmpty()){
            return ""
        }
        return this.titles[position]
    }

}