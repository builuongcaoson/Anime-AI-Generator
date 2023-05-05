package com.sola.anime.ai.generator.domain.manager

interface AnalyticManager {

    enum class TYPE {
        SYNCING,
        CLICKED_INVITE,
        CLICKED_RATING,
        CLICKED_SUPPORT    }

    fun logEvent(type: TYPE, event: String)

}