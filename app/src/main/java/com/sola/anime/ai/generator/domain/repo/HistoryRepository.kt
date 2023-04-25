package com.sola.anime.ai.generator.domain.repo

import com.sola.anime.ai.generator.domain.model.history.ChildHistory

interface HistoryRepository {

    suspend fun markHistories(vararg childHistories: ChildHistory): List<Long>

}