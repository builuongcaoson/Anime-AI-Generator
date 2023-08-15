package com.sola.anime.ai.generator.domain.repo

interface SyncRepository {

    sealed class Progress {
        object Running: Progress()
        object SyncedModelsAndLoRAs: Progress()
        object SyncedExplores: Progress()
    }

    suspend fun syncModelsAndLoRAs(progress: (Progress) -> Unit)

    suspend fun syncExplores(progress: (Progress) -> Unit)

}