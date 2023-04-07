package com.basic.common.base

import androidx.lifecycle.LifecycleOwner

interface LsView<in State> : LifecycleOwner {

    fun render(state: State)

}