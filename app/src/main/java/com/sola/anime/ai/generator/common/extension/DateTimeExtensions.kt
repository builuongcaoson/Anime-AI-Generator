package com.sola.anime.ai.generator.common.extension

import android.annotation.SuppressLint
import com.basic.common.extension.tryOrNull
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@SuppressLint("SimpleDateFormat")
fun Long.getTimeFormatted(): String {
    return SimpleDateFormat("dd/MM/yyyy - HH:mm").format(Date(this))
}

@SuppressLint("SimpleDateFormat")
fun String.toDate(): Date? {
    val format = SimpleDateFormat("dd/MM/yyyy - HH:mm")
    return tryOrNull { format.parse(this) }
}

@SuppressLint("SimpleDateFormat")
fun compareDates(dateString1: String, dateString2: String): Int {
    val format = SimpleDateFormat("dd/MM/yyyy - HH:mm")
    val date1 = format.parse(dateString1)
    val date2 = format.parse(dateString2)

    return tryOrNull { date1?.compareTo(date2) } ?: -1001
}