package com.sola.anime.ai.generator.data.repo

import android.content.Context
import android.net.Uri
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.domain.repo.DetectFaceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DetectFaceRepositoryImpl @Inject constructor(
    private var faceDetector: FirebaseVisionFaceDetector,
    private val context: Context,
    private val prefs: Preferences
): DetectFaceRepository {

    override suspend fun detectFaceUris(vararg uris: Uri) = withContext(Dispatchers.IO) {
        uris.mapIndexedNotNull { index, uri ->
            val uriHadFace = try {
                when {
                    prefs.hadFaceWithUri(uri) -> uri
                    else -> {
                        val image = FirebaseVisionImage.fromFilePath(context, uri)
                        faceDetector.detectInImage(image).await().takeIf { it.isNotEmpty() }?.let {
                            prefs.saveFaceWithUri(uri)
                            uri
                        }
                    }
                }
            } catch (e: Exception){
                e.printStackTrace()
                null
            }

            delay(if (index == 0 || index == uris.lastIndex) 0 else 500)

            uriHadFace
        }
    }

}