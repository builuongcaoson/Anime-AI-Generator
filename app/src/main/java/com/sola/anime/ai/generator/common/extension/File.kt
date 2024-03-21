package com.sola.anime.ai.generator.common.extension

import android.content.Context
import java.io.File
import java.io.InputStream

fun InputStream.toFile(context: Context, fileName: String = "${System.currentTimeMillis()}.png"): File {
    val file = File(context.filesDir, fileName)
    file.outputStream().use { this.copyTo(it) }
    return file
}