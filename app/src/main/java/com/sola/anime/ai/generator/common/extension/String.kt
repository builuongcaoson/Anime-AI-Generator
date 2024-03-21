package com.sola.anime.ai.generator.common.extension

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

fun String.copyToClipboard(context: Context) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText(null, this))
}