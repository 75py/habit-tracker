package com.nagopy.kmp.habittracker.data.local

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

/**
 * iOS-specific database module for Room database
 */
val iosDatabaseModule = module {
    single<AppDatabase> {
        val dbFile = documentDirectory() + "/${AppDatabase.DATABASE_NAME}"
        Room.databaseBuilder<AppDatabase>(
            name = dbFile,
        )
        .setDriver(BundledSQLiteDriver())
        .fallbackToDestructiveMigration()
        .build()
    }
    
    single<HabitDao> {
        get<AppDatabase>().habitDao()
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun documentDirectory(): String {
    val documentDirectory: NSURL? = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null,
    )
    return requireNotNull(documentDirectory).path!!
}