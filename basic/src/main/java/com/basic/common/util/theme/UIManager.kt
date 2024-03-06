package com.basic.common.util.theme

import com.basic.data.LsPrefs

object UIManager {

    private val listeners = mutableSetOf<UIChangedListener>()

    var themeId = LsPrefs.NIGHT_MODE
        set(value) {
            val bool = field != value
            field = value
            listeners.forEach { it.onThemeChanged(false) }
        }

    var language = "en"
        set(value) {
            val bool = field != value
            field = value
            listeners.forEach { it.onLanguageChanged(false) }
        }

    fun addListener(listener: UIChangedListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: UIChangedListener) {
        listeners.remove(listener)
    }
}