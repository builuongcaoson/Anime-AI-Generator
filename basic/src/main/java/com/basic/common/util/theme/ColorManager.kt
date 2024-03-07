package com.basic.common.util.theme

import android.content.Context
import androidx.annotation.ColorRes
import com.basic.R
import com.basic.common.extension.getColorCompat
import com.basic.common.extension.isDayOrNight
import com.basic.data.LsPrefs
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ColorManager @Inject constructor(
    private val context: Context,
    private val lsPrefs: LsPrefs
) {

    companion object {
        const val COLOR_NONE = -1
        private const val COLOR_THEME = 0
        private const val COLOR_BACKGROUND = 1
        private const val COLOR_CARD_BACKGROUND = 2
        private const val COLOR_PRIMARY = 3
        private const val COLOR_SECONDARY = 4
        private const val COLOR_TERTIARY = 5
        private const val COLOR_ON_PRIMARY = 6
        private const val COLOR_ON_SECONDARY = 7
        private const val COLOR_ON_TERTIARY = 8
        private const val COLOR_TEXT_PRIMARY = 9
        private const val COLOR_TEXT_SECONDARY = 10
        private const val COLOR_TEXT_TERTIARY = 11
        private const val COLOR_INDICATOR = 12

        fun colorById(context: Context, id: Int) = when (id) {
            COLOR_THEME -> context.getColorCompat(R.color.tools_theme)
            COLOR_BACKGROUND -> context.getColorCompat(R.color.backgroundDark)
            COLOR_CARD_BACKGROUND -> context.getColorCompat(R.color.cardBackgroundDark)
            COLOR_PRIMARY -> context.getColorCompat(R.color.primaryDark)
            COLOR_SECONDARY -> context.getColorCompat(R.color.secondaryDark)
            COLOR_TERTIARY -> context.getColorCompat(R.color.tertiaryDark)
            COLOR_ON_PRIMARY -> context.getColorCompat(R.color.onPrimaryDark)
            COLOR_ON_SECONDARY -> context.getColorCompat(R.color.onSecondaryDark)
            COLOR_ON_TERTIARY -> context.getColorCompat(R.color.onTertiaryDark)
            COLOR_TEXT_PRIMARY -> context.getColorCompat(R.color.textPrimaryDark)
            COLOR_TEXT_SECONDARY -> context.getColorCompat(R.color.textSecondaryDark)
            COLOR_TEXT_TERTIARY -> context.getColorCompat(R.color.textTertiaryDark)
            COLOR_INDICATOR -> context.getColorCompat(R.color.indicatorDark)
            else -> null
        }
    }

    private var theme = Theme(lsPrefs.themeId.get(), this)

    fun reload() {
        theme = Theme(lsPrefs.themeId.get(), this)
    }

    fun theme() = theme

    data class Theme(private val theme: Int, private val colorManager: ColorManager) {
        private val colors by lazy { listOf(toolsTheme, background, cardBackground, primary, secondary, tertiary, onPrimary, onSecondary, onTertiary, textPrimary, textSecondary, textTertiary, indicator) }

        val toolsTheme by lazy { colorManager.toolsTheme(theme) }
        val background by lazy { colorManager.background(theme) }
        val cardBackground by lazy { colorManager.cardBackground(theme) }
        val primary by lazy { colorManager.primary(theme) }
        val secondary by lazy { colorManager.secondary(theme) }
        val tertiary by lazy { colorManager.tertiary(theme) }
        val onPrimary by lazy { colorManager.onPrimary(theme) }
        val onSecondary by lazy { colorManager.onSecondary(theme) }
        val onTertiary by lazy { colorManager.onTertiary(theme) }
        val textPrimary by lazy { colorManager.textPrimary(theme) }
        val textSecondary by lazy { colorManager.textSecondary(theme) }
        val textTertiary by lazy { colorManager.textTertiary(theme) }
        val indicator by lazy { colorManager.indicator(theme) }

        fun colorById(colorId: Int) = colors.getOrNull(colorId)
    }

    private fun toolsTheme(theme: Int) = when (theme) {
        LsPrefs.LIGHT_MODE -> color(R.color.tools_theme)
        LsPrefs.NIGHT_MODE -> color(R.color.tools_theme)
        else -> color(if (isDayOrNight()) R.color.tools_theme else R.color.tools_theme)
    }

    private fun primary(theme: Int) = when (theme) {
        LsPrefs.LIGHT_MODE -> color(R.color.primaryLight)
        LsPrefs.NIGHT_MODE -> color(R.color.primaryDark)
        else -> color(if (isDayOrNight()) R.color.primaryLight else R.color.primaryDark)
    }

    private fun secondary(theme: Int) = when (theme) {
        LsPrefs.LIGHT_MODE -> color(R.color.secondaryLight)
        LsPrefs.NIGHT_MODE -> color(R.color.secondaryDark)
        else -> color(if (isDayOrNight()) R.color.secondaryLight else R.color.secondaryDark)
    }

    private fun tertiary(theme: Int) = when (theme) {
        LsPrefs.LIGHT_MODE -> color(R.color.tertiaryLight)
        LsPrefs.NIGHT_MODE -> color(R.color.tertiaryDark)
        else -> color(if (isDayOrNight()) R.color.tertiaryLight else R.color.tertiaryDark)
    }

    private fun onPrimary(theme: Int) = when (theme) {
        LsPrefs.LIGHT_MODE -> color(R.color.onPrimary)
        LsPrefs.NIGHT_MODE -> color(R.color.onPrimaryDark)
        else -> color(if (isDayOrNight()) R.color.onPrimary else R.color.onPrimaryDark)
    }

    private fun onSecondary(theme: Int) = when (theme) {
        LsPrefs.LIGHT_MODE -> color(R.color.onSecondary)
        LsPrefs.NIGHT_MODE -> color(R.color.onSecondaryDark)
        else -> color(if (isDayOrNight()) R.color.onSecondary else R.color.onSecondaryDark)
    }

    private fun onTertiary(theme: Int) = when (theme) {
        LsPrefs.LIGHT_MODE -> color(R.color.onTertiary)
        LsPrefs.NIGHT_MODE -> color(R.color.onTertiaryDark)
        else -> color(if (isDayOrNight()) R.color.onTertiary else R.color.onTertiaryDark)
    }

    private fun background(theme: Int) = when (theme) {
        LsPrefs.LIGHT_MODE -> color(R.color.backgroundLight)
        LsPrefs.NIGHT_MODE -> color(R.color.backgroundDark)
        else -> color(if (isDayOrNight()) R.color.backgroundLight else R.color.backgroundDark)
    }

    private fun cardBackground(theme: Int) = when (theme) {
        LsPrefs.LIGHT_MODE -> color(R.color.cardBackgroundLight)
        LsPrefs.NIGHT_MODE -> color(R.color.cardBackgroundDark)
        else -> color(if (isDayOrNight()) R.color.cardBackgroundLight else R.color.cardBackgroundDark)
    }

    private fun textPrimary(theme: Int) = when (theme) {
        LsPrefs.LIGHT_MODE -> color(R.color.textPrimary)
        LsPrefs.NIGHT_MODE -> color(R.color.textPrimaryDark)
        else -> color(if (isDayOrNight()) R.color.textPrimary else R.color.textPrimaryDark)
    }

    private fun textSecondary(theme: Int) = when (theme) {
        LsPrefs.LIGHT_MODE -> color(R.color.textSecondary)
        LsPrefs.NIGHT_MODE -> color(R.color.textSecondaryDark)
        else -> color(if (isDayOrNight()) R.color.textSecondary else R.color.textSecondaryDark)
    }

    private fun textTertiary(theme: Int) = when (theme) {
        LsPrefs.LIGHT_MODE -> color(R.color.textTertiary)
        LsPrefs.NIGHT_MODE -> color(R.color.textTertiaryDark)
        else -> color(if (isDayOrNight()) R.color.textTertiary else R.color.textTertiaryDark)
    }

    private fun indicator(theme: Int) = when (theme) {
        LsPrefs.LIGHT_MODE -> color(R.color.indicator)
        LsPrefs.NIGHT_MODE -> color(R.color.indicatorDark)
        else -> color(if (isDayOrNight()) R.color.indicator else R.color.indicatorDark)
    }

    private fun color(@ColorRes colorRes: Int) = context.getColorCompat(colorRes)

}
