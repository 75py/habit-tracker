package com.nagopy.kmp.habittracker.util

/**
 * iOS implementation to check if this is a debug build
 * For iOS, we'll default to true for development and can be configured later
 */
actual fun isDebugBuild(): Boolean {
    // For simplicity, always enable debug logging on iOS during development
    // This can be refined later with proper build configuration detection
    return true
}