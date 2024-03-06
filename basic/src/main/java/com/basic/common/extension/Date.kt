package com.basic.common.extension

import java.util.Calendar

fun isDayOrNight(): Boolean {
    val cal = Calendar.getInstance()
    cal.timeInMillis = System.currentTimeMillis()
    val hour = cal[Calendar.HOUR_OF_DAY]
    return hour in 6..17
}