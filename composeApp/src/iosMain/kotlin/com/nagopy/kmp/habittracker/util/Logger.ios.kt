package com.nagopy.kmp.habittracker.util

import kotlin.native.Platform

/**
 * iOS implementation to check if this is a debug build
 * Uses simple compilation-time approach for debug detection
 */
@OptIn(kotlin.experimental.ExperimentalNativeApi::class)
actual fun isDebugBuild(): Boolean {
    // In iOS, we can detect debug builds by checking if DEBUG is defined
    // This is a simple approach that works with standard iOS debug builds
    return Platform.isDebugBinary
}