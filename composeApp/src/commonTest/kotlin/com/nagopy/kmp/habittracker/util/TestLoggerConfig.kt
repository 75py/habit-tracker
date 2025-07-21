package com.nagopy.kmp.habittracker.util

import io.github.aakira.napier.Napier

/**
 * Test utility to configure Logger for testing environment
 */
object TestLoggerConfig {
    
    /**
     * Initialize Logger with test-appropriate configuration
     * This should be called in test setup to avoid Android Log mocking issues
     */
    fun setupForTests() {
        TestAntilog.clear()
        Napier.base(TestAntilog())
    }
    
    /**
     * Reset Logger configuration after tests
     */
    fun tearDown() {
        // Clear any existing antilog
        Napier.takeLogarithm()
    }
}