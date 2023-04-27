package com.sola.anime.ai.generator.common.extension

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import java.io.ByteArrayOutputStream

@SuppressLint("Recycle")
fun Uri.contentUriToRequestBody(context: Context): RequestBody? {
    try {
        val inputStream = context.contentResolver.openInputStream(this)
        val outputStream = ByteArrayOutputStream()

        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var n: Int
        while (inputStream?.read(buffer).also { n = it ?: -1 } != -1) {
            outputStream.write(buffer, 0, n)
        }

        val bytes = outputStream.toByteArray()
        return context.contentResolver.getType(this)?.let { RequestBody.create(it.toMediaTypeOrNull(), bytes) }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}