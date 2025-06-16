package com.nagopy.kmp.habittracker.util

import com.nagopy.android.habittracker.BuildConfig

/**
 * Android implementation to check if this is a debug build
 */
actual fun isDebugBuild(): Boolean = BuildConfig.DEBUG