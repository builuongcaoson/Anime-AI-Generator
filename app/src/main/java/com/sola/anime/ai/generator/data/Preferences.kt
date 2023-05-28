package com.sola.anime.ai.generator.data

import android.content.Context
import androidx.core.content.res.getColorOrThrow
import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.sola.anime.ai.generator.common.Constraint
import com.widget.R
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Preferences @Inject constructor(
    private val context: Context,
    private val rxPrefs: RxSharedPreferences
) {

    companion object {
        const val MAX_NUMBER_CREATE_ARTWORK = 3L
//        const val MAX_NUMBER_CREATE_ARTWORK_IN_A_DAY = 50L
        const val MAX_SECOND_GENERATE_ART = 30
    }

    // Config
    val isUpgraded = rxPrefs.getBoolean("isUpgraded", false)
    val timeExpiredIap = rxPrefs.getLong("timeExpiredIap", -1)

    // For App
    val isFirstTime = rxPrefs.getBoolean("isFirstTime", true)
    val isViewTutorial = rxPrefs.getBoolean("isViewTutorial", false)
    val isRated = rxPrefs.getBoolean("isRated", false)
    val isCreateDefaultFolder = rxPrefs.getBoolean("isCreateDefaultFolder", false)
    val numberCreatedArtwork = rxPrefs.getLong("numberCreatedArtwork", 0)
    val isSyncedData = rxPrefs.getBoolean("isSyncedData_${Constraint.Info.DATA_VERSION}", false)

}
