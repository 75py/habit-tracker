package com.nagopy.kmp.habittracker.notification

import com.nagopy.kmp.habittracker.domain.notification.NotificationScheduler
import com.nagopy.kmp.habittracker.domain.notification.NotificationPermissionManager
import com.nagopy.kmp.habittracker.domain.storage.AppPreferences
import com.nagopy.kmp.habittracker.storage.AndroidAppPreferences
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Android notification module providing platform-specific notification scheduler
 */
val androidNotificationModule = module {
    single<NotificationScheduler> { AndroidNotificationScheduler(androidContext()) }
    single<NotificationPermissionManager> { AndroidNotificationPermissionManager(androidContext()) }
    single<AppPreferences> { AndroidAppPreferences(androidContext()) }
}