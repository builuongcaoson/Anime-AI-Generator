package com.basic.common.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.basic.data.PreferencesConfig
import java.util.*
import javax.inject.Inject

abstract class LsActivity: AppCompatActivity() {

    @Inject lateinit var prefsConfig: PreferencesConfig

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupConfigLanguage(prefsConfig.language.get())
    }

    private fun setupConfigLanguage(languageCode: String){
        val locale = Locale(languageCode)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

}