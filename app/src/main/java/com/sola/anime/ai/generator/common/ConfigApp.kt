package com.sola.anime.ai.generator.common

import android.content.Context
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConfigApp @Inject constructor(
    private val context: Context
) {

    var subjectStyleClicks: Subject<Int> = BehaviorSubject.createDefault(-1) // Default No Style
    var subjectExploreClicks: Subject<Int> = BehaviorSubject.createDefault(-1) // Default No Explore

}