package com.sola.anime.ai.generator.domain.repo

import android.net.Uri

interface DetectFaceRepository {

    suspend fun detectFaceUris(vararg uris: Uri): List<Uri>

}