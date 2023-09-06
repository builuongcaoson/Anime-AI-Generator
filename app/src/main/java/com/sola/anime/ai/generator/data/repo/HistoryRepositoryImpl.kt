package com.sola.anime.ai.generator.data.repo

import android.content.Context
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.data.db.query.HistoryDao
import com.sola.anime.ai.generator.data.db.query.StyleDao
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
    private val prefs: Preferences,
    private val historyDao: HistoryDao,
    private val styleDao: StyleDao
): HistoryRepository {

    override fun markHistory(childHistory: ChildHistory): Long? {
        return historyDao.findByPrompt(childHistory.prompt, childHistory.styleId, childHistory.model)?.let {
            it.childs.add(childHistory)
            it.updateAt = System.currentTimeMillis()

            historyDao.update(it)

            it.id
        } ?: run {
            File(childHistory.upscalePathPreview ?: childHistory.pathPreview)
                .takeIf { file -> file.exists() }
                ?.let {
                    historyDao.insert(
                        History(
                            title = styleDao.findById(childHistory.styleId)?.display ?: "Fantasy",
                            prompt = childHistory.prompt,
                            model = childHistory.model
                        ).apply {
                            this.styleId = childHistory.styleId
                            this.childs.add(childHistory)
                        }
                    )
                }
        }
    }

    override fun getTotalChildCount(): Int {
        return historyDao.getAll().sumOf { it.childs.size }
    }

}