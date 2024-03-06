package com.basic.common.util.theme

import android.content.Context
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import com.basic.R
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FontManager @Inject constructor(
    private val context: Context
) {

    companion object {
        const val FONT_REGULAR = 0
        const val FONT_MEDIUM = 1
        const val FONT_SEMI = 2
        const val FONT_BOLD = 3

        fun fontById(context: Context, id: Int) = when (id) {
            FONT_REGULAR -> ResourcesCompat.getFont(context, R.font.roboto)
            FONT_MEDIUM -> ResourcesCompat.getFont(context, R.font.roboto_medium)
            FONT_SEMI -> ResourcesCompat.getFont(context, R.font.roboto_semibold)
            FONT_BOLD -> ResourcesCompat.getFont(context, R.font.roboto_bold)
            else -> null
        }
    }

    private var typeFaceRegular: Typeface? = null
    private var typeFaceMedium: Typeface? = null
    private var typeFaceSemi: Typeface? = null
    private var typeFaceBold: Typeface? = null
    private val fonts = listOf(
        R.font.roboto to FONT_REGULAR,
        R.font.roboto_medium to FONT_MEDIUM,
        R.font.roboto_semibold to FONT_SEMI,
        R.font.roboto_bold to FONT_BOLD
    )
    private val pendingCallbacks = ArrayList<Pair<(Typeface) -> Unit, Int>>()
    private val fontsRetrieved = arrayListOf<Typeface>()

    init {
        fonts.map { pair ->
            ResourcesCompat.getFont(context, pair.first, object : ResourcesCompat.FontCallback() {
                override fun onFontRetrievalFailed(reason: Int) {
                    Timber.e("Font retrieval failed: $reason")
                }

                override fun onFontRetrieved(typeface: Typeface) {
                    when (pair.second) {
                        FONT_REGULAR -> typeFaceRegular = typeface
                        FONT_MEDIUM -> typeFaceMedium = typeface
                        FONT_SEMI -> typeFaceSemi = typeface
                        FONT_BOLD -> typeFaceBold = typeface
                    }
                    fontsRetrieved.add(typeface)

                    if (fontsRetrieved.size == fonts.size) {
                        pendingCallbacks.forEach { callBack ->
                            val typefaceCallBack = when (callBack.second){
                                FONT_REGULAR -> typeFaceRegular
                                FONT_MEDIUM -> typeFaceMedium
                                FONT_SEMI -> typeFaceSemi
                                else -> typeFaceBold
                            }
                            typefaceCallBack?.run(callBack.first)
                        }
                    }
                }
            }, null)
        }
    }

    fun get(textFontAttr: Int, callback: (Typeface) -> Unit) {
        val typeface = when (textFontAttr){
            FONT_REGULAR -> typeFaceRegular
            FONT_MEDIUM -> typeFaceMedium
            FONT_SEMI -> typeFaceSemi
            else -> typeFaceBold
        }
        typeface?.run(callback) ?: pendingCallbacks.add(callback to textFontAttr)
    }

}