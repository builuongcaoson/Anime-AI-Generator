package com.sola.anime.ai.generator.common.extension

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer

fun <T, S> LiveData<T?>.combineWith(other: LiveData<S?>): LiveData<Pair<T?, S?>> =
    MediatorLiveData<Pair<T?, S?>>().apply {
        addSource(this@combineWith) { value = Pair(it, other.value) }
        addSource(other) { value = Pair(this@combineWith.value, it) }
    }

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
