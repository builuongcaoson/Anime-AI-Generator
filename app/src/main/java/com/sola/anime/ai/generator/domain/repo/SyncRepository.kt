package com.sola.anime.ai.generator.domain.repo

interface SyncRepository {

    sealed class Progress {
        object Running: Progress()
        object SyncedModelsAndLoRAs: Progress()
    }

    suspend fun syncModelsAndLoRAs(progress: (Progress) -> Unit)

    fun syncExplore()

}