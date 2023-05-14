package com.sola.anime.ai.generator.domain.manager

interface AnalyticManager {

    enum class TYPE {
        SYNCING,
        CLICKED_INVITE,
        CLICKED_RATING,
        CLICKED_SUPPORT,
        ADMOB_CLICKED
    }

    fun logEvent(type: TYPE, event: String)

}