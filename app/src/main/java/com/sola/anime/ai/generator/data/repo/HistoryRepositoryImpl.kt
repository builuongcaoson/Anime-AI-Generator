package com.sola.anime.ai.generator.data.repo

import android.content.Context
import com.sola.anime.ai.generator.data.db.query.HistoryDao
import com.sola.anime.ai.generator.domain.model.history.ChildHistory
import com.sola.anime.ai.generator.domain.model.history.History
import com.sola.anime.ai.generator.domain.repo.HistoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistoryRepositoryImpl @Inject constructor(
    private val context: Context,
    private val historyDao: HistoryDao
): HistoryRepository {

    override suspend fun markHistory(childHistory: ChildHistory): Long? = withContext(Dispatchers.IO) {
        File(childHistory.pathPreview)
            .takeIf { file -> file.exists() }
            ?.let {
                historyDao.insert(
                    History(
                        title = "Fantasy"
                    ).apply {
                        this.childs.add(childHistory)
                    }
                )
            }
    }

}