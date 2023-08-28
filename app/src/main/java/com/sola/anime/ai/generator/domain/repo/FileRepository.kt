package com.sola.anime.ai.generator.domain.repo

import android.graphics.Bitmap
import android.net.Uri
import java.io.File

interface FileRepository {

    suspend fun shares(vararg files: File)

    suspend fun downloads(vararg files: File)

    suspend fun downloads(vararg bitmaps: Bitmap)

    suspend fun downloadAndSaveImages(url: String)

}