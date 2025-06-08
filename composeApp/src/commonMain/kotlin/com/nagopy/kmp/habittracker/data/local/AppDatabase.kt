package com.nagopy.kmp.habittracker.data.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor

/**
 * Room database for the Habit Tracker application.
 */
@Database(
    entities = [HabitEntity::class, LogEntity::class],
    version = 1,
    exportSchema = false
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao

    companion object {
        const val DATABASE_NAME = "habit_tracker_database"
    }
}

// Room will generate the actual constructor implementation
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase>