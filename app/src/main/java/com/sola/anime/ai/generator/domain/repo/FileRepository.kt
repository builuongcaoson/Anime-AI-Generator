package com.sola.anime.ai.generator.domain.repo

import java.io.File

interface FileRepository {

    suspend fun shares(vararg files: File)

    suspend fun downloads(vararg files: File)

}