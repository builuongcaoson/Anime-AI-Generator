package com.sola.anime.ai.generator.domain.manager

interface AnalyticManager {

    enum class TYPE {
        CLICKED_INVITE,
        CLICKED_RATING,
        CLICKED_SUPPORT,
        ADMOB_CLICKED,
        GENERATE_PROCESSING,
        GENERATE_PROCESSING_BATCH,
        GENERATE_PROCESSING_AVATAR,
        GENERATE_SUCCESS,
        GENERATE_FAILED,
        GENERATE_FAILED_BATCH,
        GENERATE_FAILED_AVATAR,
        GENERATE_CLICKED,
        GENERATE_CREDITS_CLICKED,
        GENERATE_BATCH_CLICKED,
        GENERATE_AVATAR_CLICKED,
        UPSCALE_CLICKED,
        UPSCALE_SUCCESS,
        UPSCALE_FAILED,
        DOWNLOAD_CLICKED,
        DOWNLOAD_ORIGINAL_CLICKED,
        SHARE_CLICKED,
        SHARE_ORIGINAL_CLICKED,
        PURCHASE_SUCCESS,
        PURCHASE_CANCEL,
        PURCHASE_SUCCESS_CREDITS,
        PURCHASE_CANCEL_CREDITS,
        REPORT_MODEL,
        REPORT_LORA,
        REPORT_EXPLORE,
        RESTORE_CLICKED,
        RESTORE_SUCCESS
    }

    fun logEvent(type: TYPE)

}