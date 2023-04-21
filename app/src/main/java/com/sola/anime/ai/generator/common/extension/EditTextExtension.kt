package com.sola.anime.ai.generator.common.extension

import android.text.InputFilter
import android.widget.EditText

fun EditText.disableEnter() {
    this.filters = arrayOf(InputFilter { source, _, _, _, _, _ ->
        if (source == "\n") {
            return@InputFilter ""
        }
        return@InputFilter null
    })
}