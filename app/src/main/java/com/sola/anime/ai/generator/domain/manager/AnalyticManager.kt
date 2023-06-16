package com.sola.anime.ai.generator.domain.manager

interface AnalyticManager {

    enum class TYPE {
        FIRST_SYNC,
        FIRST_SYNC_PURCHASED,
        CLICKED_INVITE,
        CLICKED_RATING,
        CLICKED_SUPPORT,
        ADMOB_CLICKED,
        GENERATE_SUCCESS,
        GENERATE_FAILED,
        SYNC_USER_PREMIUM_FAILED
    }

    fun logEvent(type: TYPE)

}