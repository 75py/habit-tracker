package com.nagopy.kmp.habittracker.data.local

import androidx.room.Room
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Android-specific database module for Room database
 */
val androidDatabaseModule = module {
    single<AppDatabase> {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        ).build()
    }
    
    single<HabitDao> {
        get<AppDatabase>().habitDao()
    }
}