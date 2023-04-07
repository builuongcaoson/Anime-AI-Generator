package com.basic.data

import android.content.Context
import com.f2prateek.rx.preferences2.RxSharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesConfig @Inject constructor(
    private val context: Context,
    private val rxPrefs: RxSharedPreferences
) {

    companion object {
        const val NIGHT_MODE = 0
        const val LIGHT_MODE = 1
        const val AUTO_MODE = 2

        const val TEXT_SIZE_SMALL = 0
        const val TEXT_SIZE_NORMAL = 1
        const val TEXT_SIZE_LARGE = 2
        const val TEXT_SIZE_LARGER = 3
    }

    // Config
    val mode = rxPrefs.getInteger("mode", AUTO_MODE)
    val language = rxPrefs.getString("language", "en")
    val systemFont = rxPrefs.getBoolean("systemFont", false)
    val textSize = rxPrefs.getInteger("textSize", TEXT_SIZE_NORMAL)
    val theme = rxPrefs.getInteger("theme", 0xFF141A18.toInt())


}
