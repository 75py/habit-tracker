package com.nagopy.kmp.habittracker.data

import com.nagopy.kmp.habittracker.data.local.androidDatabaseModule
import org.koin.core.module.Module

/**
 * Android implementation of the database module
 */
actual val databaseModule: Module = androidDatabaseModule