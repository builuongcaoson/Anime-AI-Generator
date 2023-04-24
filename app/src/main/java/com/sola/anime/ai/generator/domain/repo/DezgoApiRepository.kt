package com.sola.anime.ai.generator.domain.repo

interface DezgoApiRepository {

    suspend fun generateTextsToImages()

}