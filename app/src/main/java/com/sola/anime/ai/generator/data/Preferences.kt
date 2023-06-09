package com.sola.anime.ai.generator.data

import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.sola.anime.ai.generator.common.Constraint
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Preferences @Inject constructor(
    private val rxPrefs: RxSharedPreferences
) {

    companion object {
        const val MAX_NUMBER_CREATE_ARTWORK = 5L
        const val MAX_NUMBER_CREATE_ARTWORK_IN_A_DAY = 15L
        const val MAX_SECOND_GENERATE_ART = 30
    }

    // Config
    val isUpgraded = rxPrefs.getBoolean("isUpgraded", false)
    val timeExpiredPremium = rxPrefs.getLong("timeExpiredPremium", -1) // Milliseconds
    val isSyncUserPurchased = rxPrefs.getBoolean("isSyncUserPurchased_v2", false)
    val isSyncUserPurchasedFailed = rxPrefs.getBoolean("isSyncUserPurchasedFailed", true)
    val userPremium = rxPrefs.getString("userPremium", "")

    // For App
    val isEnableNsfw = rxPrefs.getBoolean("isEnableNsfw", false)
    val isShowedWaringPremiumDialog = rxPrefs.getBoolean("isShowedWaringPremiumDialog", false)
    val isFirstTime = rxPrefs.getBoolean("isFirstTime", true)
    val isViewTutorial = rxPrefs.getBoolean("isViewTutorial", false)
    val isRated = rxPrefs.getBoolean("isRated", false)
    val isCreateDefaultFolder = rxPrefs.getBoolean("isCreateDefaultFolder", false)
    val isSyncedData = rxPrefs.getBoolean("isSyncedData_${Constraint.Info.DATA_VERSION}", false)
    val numberCreatedArtworkInDayFailed = rxPrefs.getLong("numberCreatedArtworkInDayFailed", 1)
    val numberCreatedArtwork = rxPrefs.getLong("numberCreatedArtwork", 0)
    val totalNumberCreatedArtwork = rxPrefs.getLong("totalNumberCreatedArtwork", 0)
    val latestTimeCreatedArtwork = rxPrefs.getLong("latestTimeCreatedArt", -1)
    val creditsTest = rxPrefs.getFloat("creditsTest", -1f)
}
