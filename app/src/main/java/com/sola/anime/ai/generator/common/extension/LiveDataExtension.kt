package com.sola.anime.ai.generator.common.extension

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

fun <T> LiveData<List<T>>.observeAndRemoveWhenNotEmpty(lifecycleOwner: LifecycleOwner, observer: Observer<List<T>>) {
    observe(lifecycleOwner, object : Observer<List<T>> {
        override fun onChanged(value: List<T>) {
            if (value.isNotEmpty()){
                observer.onChanged(value)
                removeObserver(this)
            }
        }
    })
}

fun <T> LiveData<T>.observeAndRemoveWhenNotNull(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
    observe(lifecycleOwner, object : Observer<T?> {
        override fun onChanged(value: T?) {
            if (value != null){
                observer.onChanged(value)
                removeObserver(this)
            }
        }
    })
}