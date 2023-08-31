package com.sola.anime.ai.generator.common.extension

import android.annotation.SuppressLint
import android.text.format.DateUtils
import java.text.SimpleDateFormat
import java.util.Date

@SuppressLint("SimpleDateFormat")
fun Long.getTimeFormatted(): String {
    return SimpleDateFormat("dd MMMM, yyyy - HH:mm").format(Date(this))
}