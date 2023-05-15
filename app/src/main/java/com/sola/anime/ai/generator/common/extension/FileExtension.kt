package com.sola.anime.ai.generator.common.extension

import android.content.Context
import android.graphics.Bitmap
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

fun InputStream.toFile(context: Context, fileName: String = "${System.currentTimeMillis()}.png"): File {
    val dir = File(context.filesDir, "${System.currentTimeMillis()}")
    dir.mkdirs()
    val file = File(dir, fileName)
    file.outputStream().use { this.copyTo(it) }
    return file
}

fun Bitmap.toFile(context: Context, fileName: String = "${System.currentTimeMillis()}.png"): File? {
    //create a file to write bitmap data
    var file: File? = null
    return try {
        val dir = File(context.filesDir, "${System.currentTimeMillis()}")
        dir.mkdirs()

        file = File(dir, fileName)
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