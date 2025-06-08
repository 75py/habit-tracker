package com.nagopy.kmp.habittracker.util

import io.github.aakira.napier.Antilog
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.LogLevel
import io.github.aakira.napier.Napier

/**
 * Simple test antilog that doesn't use Android Log to avoid mocking issues in tests
 */
class TestAntilog : Antilog() {
    override fun performLog(priority: LogLevel, tag: String?, throwable: Throwable?, message: String?) {
        // Simple println that works in tests without Android Log
        val tagText = tag?.let { "[$it] " } ?: ""
        val throwableText = throwable?.let { " - ${it.message}" } ?: ""
        println("${priority.name}: $tagText$message$throwableText")
    }
}

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
            // Check if we're in a test environment by looking for test classes in the stack trace
            val isInTest = Thread.currentThread().stackTrace.any { stackFrame ->
                stackFrame.className.contains("Test") || 
                stackFrame.className.contains("junit") ||
                stackFrame.className.contains("kotlin.test")
            }
            
            if (isInTest) {
                Napier.base(TestAntilog())
            } else {
                Napier.base(DebugAntilog())
            }
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