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

fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
    observe(lifecycleOwner, object : Observer<T> {
        override fun onChanged(value: T) {
            observer.onChanged(value)
            removeObserver(this)
        }
    })
}
