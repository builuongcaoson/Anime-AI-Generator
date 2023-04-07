package com.basic.common.util.theme

import android.content.Context
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import com.widget.R
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FontProvider @Inject constructor(
    private val context: Context
) {

    private val fonts = listOf(
        R.font.roboto to TextViewStyler.FONT_REGULAR,
        R.font.roboto_medium to TextViewStyler.FONT_MEDIUM,
        R.font.roboto_semibold to TextViewStyler.FONT_SEMI,
        R.font.roboto_bold to TextViewStyler.FONT_BOLD
    )
    private var typefaces: ArrayList<Pair<Typeface, Int>> = arrayListOf()
    private val pendingCallbacks = ArrayList<Pair<(Typeface) -> Unit, Int>>()

    init {
        fonts.map { pair ->
            ResourcesCompat.getFont(context, pair.first, object : ResourcesCompat.FontCallback() {
                override fun onFontRetrievalFailed(reason: Int) {
                    Timber.e("Font retrieval failed: $reason")
                }

                override fun onFontRetrieved(typeface: Typeface) {
                    if (typefaces.find { it.second == pair.second } == null){
                        typefaces.add(typeface to pair.second)
                    }

                    pendingCallbacks.forEach { callBack ->
                        typefaces.firstOrNull { it.second == callBack.second }?.first?.run(callBack.first)
                    }
                    checkAllFonts()
                }
            }, null)
        }
    }

    private fun checkAllFonts(){
        if (typefaces.size == fonts.size){
            pendingCallbacks.clear()
        }
    }

    fun get(textFontAttr: Int, callback: (Typeface) -> Unit) {
        typefaces.firstOrNull { it.second == textFontAttr }?.first?.run(callback) ?: pendingCallbacks.add(callback to textFontAttr)
    }

}