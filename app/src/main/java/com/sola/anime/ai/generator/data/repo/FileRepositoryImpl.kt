package com.sola.anime.ai.generator.data.repo

import android.content.Context
import android.content.Intent
import android.os.Environment
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.basic.common.extension.tryOrNull
import com.sola.anime.ai.generator.BuildConfig
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.domain.repo.FileRepository
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileRepositoryImpl @Inject constructor(
    private val context: Context
): FileRepository {

    override suspend fun shares(vararg files: File) {
        val uris = files.map { file ->
            FileProvider.getUriForFile(
                context,
                "${BuildConfig.APPLICATION_ID}.provider",
                file)
        }
        val intent = Intent(Intent.ACTION_SEND_MULTIPLE)
        intent.type = "image/*"
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    override suspend fun downloads(vararg files: File) {
        val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), context.getString(R.string.app_name))
        dir.mkdirs()

        val destinationFiles = files.filter { file -> file.exists() }.mapNotNull { file ->
            tryOrNull {
                val fileCopy = getFileCopy(dir, file.name)
                copyFile(file, fileCopy)
            }
        }
        destinationFiles.forEach { file ->
            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            val contentUri = file.toUri()
            mediaScanIntent.data = contentUri
            context.sendBroadcast(mediaScanIntent)
        }
    }

    private fun copyFile(sourceFile: File, destinationFile: File): File? {
        var inputStream: FileInputStream? = null
        var outputStream: FileOutputStream? = null
        var file: File? = null

        try {
            inputStream = FileInputStream(sourceFile)
            outputStream = FileOutputStream(destinationFile)

            val buffer = ByteArray(1024)
            var length: Int

            while (inputStream.read(buffer).also { length = it } > 0) {
                outputStream.write(buffer, 0, length)
            }

            println("File copied successfully.")
            file = destinationFile
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                inputStream?.close()
                outputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return file
    }

    private fun getFileCopy(fileDir: File, fileName: String, index: Int = 0): File{
        val file = when (index) {
            0 -> File(fileDir, fileName)
            else -> File(fileDir, "${fileName}_$index")
        }
        if (file.exists()){
            return getFileCopy(fileDir, fileName, index = index + 1)
        }
        return file
    }

}