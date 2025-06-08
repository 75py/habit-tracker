package com.nagopy.kmp.habittracker.util

import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier

/**
 * Logging utility using Napier for cross-platform logging
 */
object Logger {
    
    /**
     * Initialize Napier logging for debug builds only
     */
    fun init() {
        // Only add antilog in debug builds
        if (isDebugBuild()) {
            Napier.base(DebugAntilog())
        }
    }
    
    /**
     * Log debug information
     */
    fun d(message: String, tag: String? = null) {
        Napier.d(message, tag = tag)
    }
    
    /**
     * Log exception/error
     */
    fun e(throwable: Throwable, message: String? = null, tag: String? = null) {
        Napier.e(message ?: throwable.message ?: "Unknown error", throwable, tag = tag)
    }
    
    /**
     * Log info message
     */
    fun i(message: String, tag: String? = null) {
        Napier.i(message, tag = tag)
    }
    
    /**
     * Log warning message
     */
    fun w(message: String, tag: String? = null) {
        Napier.w(message, tag = tag)
    }
}

/**
 * Check if this is a debug build
 * Platform-specific implementations will be provided
 */
expect fun isDebugBuild(): Boolean