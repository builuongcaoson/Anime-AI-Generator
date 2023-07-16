package com.sola.anime.ai.generator.common.extension

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Rect
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

fun InputStream.toBitmap(): Bitmap? {
    val options = BitmapFactory.Options()
    options.inPreferredConfig = Bitmap.Config.ARGB_8888
    return BitmapFactory.decodeStream(this, null, options)
}

fun Bitmap.resizeBitmap(newWidth: Int, newHeight: Int): Bitmap {
    return Bitmap.createScaledBitmap(this, newWidth, newHeight, false)
}

fun Bitmap.cropAndResizeBitmap(): Bitmap {
    val dimen = Math.min(this.width, this.height)
    val croppedBitmap = Bitmap.createBitmap(dimen, dimen, Bitmap.Config.ARGB_8888)
    val dx = (this.width - dimen) / 2
    val dy = (this.height - dimen) / 2
    val canvas = Canvas(croppedBitmap)
    val srcRect = Rect(dx, dy, dx + dimen, dy + dimen)
    val dstRect = Rect(0, 0, dimen, dimen)
    canvas.drawBitmap(this, srcRect, dstRect, null)
    return resize(512, 512)
}

fun Bitmap.resize(width: Int, height: Int): Bitmap {
    return Bitmap.createScaledBitmap(this, width, height, true)
}

fun Bitmap.getByteArray(): ByteArray {
    val byteArrayOutputStream = ByteArrayOutputStream()
    compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
    return byteArrayOutputStream.toByteArray()
}

fun Bitmap.toFile(context: Context, fileName: String = "${System.currentTimeMillis()}.png"): File? {
    //create a file to write bitmap data
    var file: File? = null
    return try {
        file = File(context.filesDir, fileName)
        file.createNewFile()

        // Convert bitmap to byte array
        val bos = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.PNG, 0, bos) // YOU can also save it in JPEG
        val byteArray = bos.toByteArray()

        // Write the bytes in file
        val fos = FileOutputStream(file)
        fos.write(byteArray)
        fos.flush()
        fos.close()
        file
    } catch (e: Exception) {
        e.printStackTrace()
        file // it will return null
    }
}