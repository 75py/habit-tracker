package com.nagopy.kmp.habittracker.data

import org.koin.core.module.Module

/**
 * Platform-specific database module
 * Each platform should provide its own implementation
 */
expect val databaseModule: Module