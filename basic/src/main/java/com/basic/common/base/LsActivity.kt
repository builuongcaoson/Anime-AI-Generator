package com.basic.common.base

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.basic.R
import com.basic.common.extension.lightNavigationBar
import com.basic.common.extension.lightStatusBar
import com.basic.common.extension.transparent
import com.basic.data.LsPrefs
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import java.util.Locale
import javax.inject.Inject

abstract class LsActivity<VB : ViewBinding>(
    val bindingInflater: (LayoutInflater) -> VB
): AppCompatActivity() {

    @Inject lateinit var lsPrefs: LsPrefs

    val binding: VB by lazy { bindingInflater(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transparent()
        if (lsPrefs.themeId.get() == LsPrefs.LIGHT_MODE){
            lightStatusBar()
            lightNavigationBar()
        }
        lsPrefs
            .language
            .asObservable()
            .autoDispose(scope())
            .subscribe { language ->
                initLanguage(language)
            }
        setContentView(binding.root)
    }

    private fun initLanguage(language: String) {
        val locale = Locale(language)
        val config = resources.configuration.apply {
            setLocale(locale)
        }
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}