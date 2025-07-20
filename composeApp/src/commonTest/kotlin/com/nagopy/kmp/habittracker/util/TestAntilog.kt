package com.nagopy.kmp.habittracker.util

import io.github.aakira.napier.Antilog
import io.github.aakira.napier.LogLevel

/**
 * Test-specific antilog that doesn't use Android Log to avoid mocking issues in tests
 */
class TestAntilog : Antilog() {
    companion object {
        val logs = mutableListOf<String>()
        fun clear() = logs.clear()
    }

    override fun performLog(priority: LogLevel, tag: String?, throwable: Throwable?, message: String?) {
        // Simple println that works in tests without Android Log
        val tagText = tag?.let { "[$it] " } ?: ""
        val throwableText = throwable?.let { " - ${it.message}" } ?: ""
        val log = "${priority.name}: $tagText$message$throwableText"
        println(log)
        logs += log
    }
}