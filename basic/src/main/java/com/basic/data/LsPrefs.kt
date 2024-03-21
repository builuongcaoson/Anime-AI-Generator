package com.basic.data

import android.content.Context
import android.content.res.Configuration
import com.f2prateek.rx.preferences2.RxSharedPreferences
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LsPrefs @Inject constructor(
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
    val language = rxPrefs.getString("language", Locale.getDefault().language)
    val systemFont = rxPrefs.getBoolean("systemFont", false)
    val textSize = rxPrefs.getInteger("textSize", TEXT_SIZE_NORMAL)
    val themeId = rxPrefs.getInteger("themeId", LIGHT_MODE)

}
