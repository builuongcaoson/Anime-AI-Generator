package com.sola.anime.ai.generator.data.repo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.sola.anime.ai.generator.common.extension.toBitmap
import com.sola.anime.ai.generator.common.extension.toFile
import com.sola.anime.ai.generator.data.db.query.ChildHistoryDao
import com.sola.anime.ai.generator.data.db.query.HistoryDao
import com.sola.anime.ai.generator.domain.model.history.ChildHistory
import com.sola.anime.ai.generator.domain.model.history.History
import com.sola.anime.ai.generator.domain.model.status.GenerateTextsToImagesProgress
import com.sola.anime.ai.generator.domain.model.textToImage.DezgoBodyTextToImage
import com.sola.anime.ai.generator.domain.model.textToImage.ResponseTextToImage
import com.sola.anime.ai.generator.domain.repo.DezgoApiRepository
import com.sola.anime.ai.generator.domain.repo.HistoryRepository
import com.sola.anime.ai.generator.inject.dezgo.DezgoApi
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistoryRepositoryImpl @Inject constructor(
    private val context: Context,
    private val historyDao: HistoryDao,
    private val childHistoryDao: ChildHistoryDao
): HistoryRepository {

    override suspend fun markHistories(vararg childHistories: ChildHistory): List<Long> = withContext(Dispatchers.IO){
        val histories = childHistories.mapNotNull { childHistory ->
            childHistoryDao.inserts(childHistory)

            File(childHistory.pathPreview)
                .takeIf { file -> file.exists() }
                ?.let { filePreview ->
                    History(
                        title = "Fantasy",
                        prompt = "ABC",
                        pathDir = filePreview.parentFile?.path,
                        pathPreview = filePreview.path
                    )
                }
        }
        historyDao.inserts(*histories.toTypedArray())
    }

}