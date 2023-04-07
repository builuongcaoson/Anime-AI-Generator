package com.basic.common.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.basic.common.util.theme.Colors
import com.basic.data.PreferencesConfig
import javax.inject.Inject

abstract class LsThemedActivity: AppCompatActivity() {

    @Inject lateinit var prefs: PreferencesConfig
    @Inject lateinit var colors: Colors

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        when(prefs.mode.get()){
//            Preferences.AUTO_MODE, Preferences.NIGHT_MODE -> setTheme(R.style.Theme_LsPhotoEditor_Dark)
//            else -> setTheme(R.style.Theme_LsPhotoEditor)
//        }
//        Timber.e("Mode: ${prefs.mode.get()}")
    }

}