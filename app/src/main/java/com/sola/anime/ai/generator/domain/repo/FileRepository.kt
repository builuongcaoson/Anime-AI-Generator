package com.sola.anime.ai.generator.domain.repo

import android.graphics.Bitmap
import java.io.File

interface FileRepository {

    suspend fun shares(vararg files: File)

    suspend fun downloads(vararg files: File)

    suspend fun downloads(vararg bitmaps: Bitmap)

}