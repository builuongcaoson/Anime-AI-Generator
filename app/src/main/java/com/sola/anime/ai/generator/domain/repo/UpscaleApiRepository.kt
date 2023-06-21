package com.sola.anime.ai.generator.domain.repo

import java.io.File

interface UpscaleApiRepository {

    suspend fun upscale(file: File, done: (File?) -> Unit)

}