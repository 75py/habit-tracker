package com.nagopy.kmp.habittracker.notification

import org.koin.core.module.Module

/**
 * Platform-specific notification module.
 * Implementations should provide NotificationScheduler interface.
 */
expect val notificationModule: Module