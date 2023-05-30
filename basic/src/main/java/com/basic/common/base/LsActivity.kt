package com.basic.common.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.basic.data.PreferencesConfig
import java.util.*
import javax.inject.Inject

abstract class LsActivity<VB : ViewBinding>(
    val bindingInflater: (LayoutInflater) -> VB
): AppCompatActivity() {

    @Inject lateinit var prefsConfig: PreferencesConfig

    val binding: VB by lazy { bindingInflater(layoutInflater) }

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