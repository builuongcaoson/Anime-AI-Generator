package com.sola.anime.ai.generator.common.extension

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.InputStream

fun InputStream.toBitmap(): Bitmap? {
    val options = BitmapFactory.Options()
    options.inPreferredConfig = Bitmap.Config.ARGB_8888
    return BitmapFactory.decodeStream(this, null, options)
}