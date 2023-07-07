package com.sola.anime.ai.generator.common.extension

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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