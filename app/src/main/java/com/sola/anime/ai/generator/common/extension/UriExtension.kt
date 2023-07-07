package com.sola.anime.ai.generator.common.extension

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import androidx.core.net.toFile
import androidx.core.net.toUri
import com.basic.common.extension.tryOrNull
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

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

fun Uri.resizeAndReturnUri(context: Context, targetWidth: Int, targetHeight: Int): Uri? {
    return this.getBitmapFromUri(context)?.resizeBitmap(targetWidth, targetHeight)?.toFile(context)?.toUri().also {
        tryOrNull { this.toFile().delete() }
    }
}

fun Uri.getBitmapFromUri(context: Context): Bitmap? {
    var bitmap: Bitmap? = null
    try {
        val inputStream = context.contentResolver.openInputStream(this)
        bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return bitmap
}

fun Uri.resizeImage(context: Context, targetWidth: Int, targetHeight: Int): Uri? {
    try {
        val options = BitmapFactory.Options().apply {
            // Chỉ đọc thông tin ảnh, không đọc toàn bộ ảnh vào bộ nhớ
            inJustDecodeBounds = true
            BitmapFactory.decodeStream(context.contentResolver.openInputStream(this@resizeImage), null, this)

            // Tính toán tỷ lệ scale để resize ảnh
            inSampleSize = calculateInSampleSize(outWidth, outHeight, targetWidth, targetHeight)

            // Đọc toàn bộ ảnh vào bộ nhớ với tỷ lệ scale đã tính
            inJustDecodeBounds = false
        }

        val bitmap = BitmapFactory.decodeStream(context.contentResolver.openInputStream(this), null, options)

        val scaledBitmap = scaleBitmap(bitmap!!, targetWidth, targetHeight)

        val targetFile = File(context.filesDir, "resized_image.png")
        val outputStream = FileOutputStream(targetFile)
        scaledBitmap?.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        outputStream.flush()
        outputStream.close()

        return Uri.fromFile(targetFile)
    } catch (e: IOException) {
        Log.e("Main12345","Error: $e")
        e.printStackTrace()
    }

    return null
}

private fun calculateInSampleSize(originalWidth: Int, originalHeight: Int, targetWidth: Int, targetHeight: Int): Int {
    var inSampleSize = 1
    if (originalHeight > targetHeight || originalWidth > targetWidth) {
        val halfHeight = originalHeight / 2
        val halfWidth = originalWidth / 2
        while ((halfHeight / inSampleSize) >= targetHeight && (halfWidth / inSampleSize) >= targetWidth) {
            inSampleSize *= 2
        }
    }
    return inSampleSize
}

private fun scaleBitmap(bitmap: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap? {
    val scaleX = targetWidth.toFloat() / bitmap.width
    val scaleY = targetHeight.toFloat() / bitmap.height

    val matrix = Matrix().apply {
        postScale(scaleX, scaleY)
    }

    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}
