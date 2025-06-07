package com.nagopy.kmp.habittracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Room database for the Habit Tracker application.
 */
@Database(
    entities = [HabitEntity::class, LogEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao

    companion object {
        const val DATABASE_NAME = "habit_tracker_database"
    }
}