package com.example.opotracker.data

import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File

fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val appData = System.getenv("APPDATA")?.takeIf { it.isNotBlank() }
        ?: System.getProperty("user.home")
    val dir = File(appData, "OpoTracker").apply { mkdirs() }
    val dbFile = File(dir, NOMBRE_BASE_DATOS)
    return Room.databaseBuilder<AppDatabase>(
        name = dbFile.absolutePath,
    )
}
