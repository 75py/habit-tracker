package com.nagopy.kmp.habittracker.notification

import com.nagopy.kmp.habittracker.domain.notification.NotificationScheduler
import com.nagopy.kmp.habittracker.domain.notification.NotificationPermissionManager
import com.nagopy.kmp.habittracker.domain.storage.AppPreferences
import com.nagopy.kmp.habittracker.storage.IOSAppPreferences
import org.koin.dsl.module

/**
 * iOS notification module providing platform-specific notification scheduler
 */
val iosNotificationModule = module {
    single<NotificationScheduler> { IOSNotificationScheduler(get(), get()) }
    single<NotificationPermissionManager> { IOSNotificationPermissionManager() }
    single<AppPreferences> { IOSAppPreferences() }
}