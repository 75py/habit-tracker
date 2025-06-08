package com.nagopy.kmp.habittracker.notification

import com.nagopy.kmp.habittracker.domain.notification.NotificationPermissionManager
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UserNotifications.*

/**
 * iOS implementation of NotificationPermissionManager using UserNotifications framework.
 */
@OptIn(ExperimentalForeignApi::class)
class IOSNotificationPermissionManager : NotificationPermissionManager {
    
    private val center = UNUserNotificationCenter.currentNotificationCenter()
    
    override suspend fun areNotificationsEnabled(): Boolean {
        return kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
            center.getNotificationSettingsWithCompletionHandler { settings ->
                val isEnabled = settings?.authorizationStatus == UNAuthorizationStatusAuthorized
                continuation.resumeWith(Result.success(isEnabled))
            }
        }
    }
    
    override suspend fun requestNotificationPermission(): Boolean {
        return kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
            center.requestAuthorizationWithOptions(
                UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge
            ) { granted, _ ->
                continuation.resumeWith(Result.success(granted))
            }
        }
    }
}