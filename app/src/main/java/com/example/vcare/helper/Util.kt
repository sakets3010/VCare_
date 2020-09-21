package com.example.vcare.helper

import android.content.res.Resources
import java.util.concurrent.TimeUnit

private val ONE_MINUTE_MILLIS = TimeUnit.MILLISECONDS.convert(1, TimeUnit.MINUTES)

fun convertDurationToFormatted(startTimeMilli: Long, endTimeMilli: Long): Boolean {
    val durationMilli = endTimeMilli - startTimeMilli
    return durationMilli > (2*ONE_MINUTE_MILLIS)

}