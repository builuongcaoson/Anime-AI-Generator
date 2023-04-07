package com.basic.common.util.theme

import android.content.Context
import android.graphics.Color
import com.widget.R
import com.basic.common.extension.getColorCompat
import com.basic.data.PreferencesConfig
import javax.inject.Inject

class Colors @Inject constructor(
    private val context: Context,
    private val prefs: PreferencesConfig
) {

    data class Theme(val theme: Int, private val colors: Colors) {
        val highlight by lazy { colors.highlightColorForTheme(theme) }
        val textPrimary by lazy { colors.textPrimaryOnThemeForColor(theme) }
        val textSecondary by lazy { colors.textSecondaryOnThemeForColor(theme) }
        val textTertiary by lazy { colors.textTertiaryOnThemeForColor(theme) }
    }

    private val minimumContrastRatio = 2

    // Cache these values so they don't need to be recalculated
    private val primaryTextLuminance = measureLuminance(context.getColorCompat(R.color.textPrimaryDark))
    private val secondaryTextLuminance = measureLuminance(context.getColorCompat(R.color.textSecondaryDark))
    private val tertiaryTextLuminance = measureLuminance(context.getColorCompat(R.color.textTertiaryDark))

    fun theme(): Theme {
        return Theme(prefs.theme.get(), this)
    }

    fun highlightColorForTheme(theme: Int): Int = FloatArray(3)
        .apply { Color.colorToHSV(theme, this) }
        .let { hsv -> hsv.apply { set(2, 0.75f) } } // 75% value
        .let { hsv -> Color.HSVToColor(85, hsv) } // 33% alpha

    fun textPrimaryOnThemeForColor(color: Int): Int = color
        .let { theme -> measureLuminance(theme) }
        .let { themeLuminance -> primaryTextLuminance / themeLuminance }
        .let { contrastRatio -> contrastRatio < minimumContrastRatio }
        .let { contrastRatio -> if (contrastRatio) R.color.textPrimary else R.color.textPrimaryDark }
        .let { res -> context.getColorCompat(res) }

    fun textSecondaryOnThemeForColor(color: Int): Int = color
        .let { theme -> measureLuminance(theme) }
        .let { themeLuminance -> secondaryTextLuminance / themeLuminance }
        .let { contrastRatio -> contrastRatio < minimumContrastRatio }
        .let { contrastRatio -> if (contrastRatio) R.color.textSecondary else R.color.textSecondaryDark }
        .let { res -> context.getColorCompat(res) }

    fun textTertiaryOnThemeForColor(color: Int): Int = color
        .let { theme -> measureLuminance(theme) }
        .let { themeLuminance -> tertiaryTextLuminance / themeLuminance }
        .let { contrastRatio -> contrastRatio < minimumContrastRatio }
        .let { contrastRatio -> if (contrastRatio) R.color.textTertiary else R.color.textTertiaryDark }
        .let { res -> context.getColorCompat(res) }

    /**
     * Measures the luminance value of a color to be able to measure the contrast ratio between two materialColors
     * Based on https://stackoverflow.com/a/9733420
     */
    private fun measureLuminance(color: Int): Double {
        val array = intArrayOf(Color.red(color), Color.green(color), Color.blue(color))
            .map { if (it < 0.03928) it / 12.92 else Math.pow((it + 0.055) / 1.055, 2.4) }

        return 0.2126 * array[0] + 0.7152 * array[1] + 0.0722 * array[2] + 0.05
    }

//    private fun generateColor(recipient: Recipient): Int {
//        val index = recipient.address.hashCode().absoluteValue % randomColors.size
//        return randomColors[index]
//    }
}
