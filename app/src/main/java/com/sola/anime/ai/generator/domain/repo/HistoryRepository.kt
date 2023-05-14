package com.sola.anime.ai.generator.domain.repo

import com.sola.anime.ai.generator.domain.model.history.ChildHistory

interface HistoryRepository {

    fun markHistory(childHistory: ChildHistory): Long?

    fun getTotalChildCount(): Int

}