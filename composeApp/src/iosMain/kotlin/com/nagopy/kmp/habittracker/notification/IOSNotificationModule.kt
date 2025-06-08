package com.nagopy.kmp.habittracker.notification

import com.nagopy.kmp.habittracker.domain.notification.NotificationScheduler
import org.koin.dsl.module

/**
 * iOS notification module providing platform-specific notification scheduler
 */
val iosNotificationModule = module {
    single<NotificationScheduler> { IOSNotificationScheduler() }
}