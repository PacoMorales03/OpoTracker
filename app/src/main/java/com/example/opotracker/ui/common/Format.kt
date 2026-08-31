package com.example.opotracker.ui.common

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val dateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm")

fun formatDuration(totalSeconds: Long): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

fun formatFecha(fechaMillis: Long): String {
    val instant = Instant.ofEpochMilli(fechaMillis)
    return dateFormatter.format(instant.atZone(ZoneId.systemDefault()))
}
