package com.nagopy.kmp.habittracker.data

import com.nagopy.kmp.habittracker.data.local.iosDatabaseModule
import org.koin.core.module.Module

/**
 * iOS implementation of the database module
 */
actual val databaseModule: Module = iosDatabaseModule