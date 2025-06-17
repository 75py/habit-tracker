package com.nagopy.kmp.habittracker.di

import org.koin.core.context.GlobalContext

/**
 * Helper to access Koin from Swift/Objective-C code
 */
object KoinHelper {
    
    /**
     * Get the global Koin application instance
     */
    fun getKoinApplication() = GlobalContext.get()
    
    /**
     * Get an instance of the specified class from Koin
     */
    inline fun <reified T> get(): T = GlobalContext.get().get()
}