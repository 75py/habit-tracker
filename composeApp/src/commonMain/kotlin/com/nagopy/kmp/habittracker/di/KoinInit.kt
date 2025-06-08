package com.nagopy.kmp.habittracker.di

import com.nagopy.kmp.habittracker.util.Logger
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

/**
 * Common Koin initialization function for all platforms
 */
fun initKoin(appDeclaration: KoinAppDeclaration = {}) = startKoin {
    appDeclaration()
    modules(appModule)
}.also {
    // Initialize logging after Koin setup
    Logger.init()
}

/**
 * Helper function for iOS to initialize Koin at app startup
 */
fun doInitKoin() {
    initKoin()
}