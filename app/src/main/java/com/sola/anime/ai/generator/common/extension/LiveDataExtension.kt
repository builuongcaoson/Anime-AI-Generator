package com.sola.anime.ai.generator.common.extension

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData

fun <T, S> LiveData<T?>.combineWith(other: LiveData<S?>): LiveData<Pair<T?, S?>> =
    MediatorLiveData<Pair<T?, S?>>().apply {
        addSource(this@combineWith) { value = Pair(it, other.value) }
        addSource(other) { value = Pair(this@combineWith.value, it) }
    }
