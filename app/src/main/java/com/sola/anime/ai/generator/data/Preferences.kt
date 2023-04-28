package com.sola.anime.ai.generator.data

import android.content.Context
import androidx.core.content.res.getColorOrThrow
import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.widget.R
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Preferences @Inject constructor(
    private val context: Context,
    private val rxPrefs: RxSharedPreferences
) {

    companion object {
        const val MAX_NUMBER_CREATE_ART = 9999999
        const val MAX_SECOND_GENERATE_ART = 30
    }

    // Config
    val isUpgraded = rxPrefs.getBoolean("isUpgradedPro", true)

    // For App
    val isFirstTime = rxPrefs.getBoolean("isFirstTime", true)
    val isRated = rxPrefs.getBoolean("isRated", false)
    val numberCreateArt = rxPrefs.getInteger("numberCreateArt", 0)
    val isCreateDefaultFolder = rxPrefs.getBoolean("isCreateDefaultFolder", false)


}
