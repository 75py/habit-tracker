package com.nagopy.kmp.habittracker.notification

import com.nagopy.kmp.habittracker.domain.notification.NotificationPermissionManager
import com.nagopy.kmp.habittracker.util.Logger
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
                Logger.d("Notification authorization status check: enabled=$isEnabled", "IOSNotificationPermissionManager")
                continuation.resumeWith(Result.success(isEnabled))
            }
        }
    }
    
    override suspend fun requestNotificationPermission(): Boolean {
        Logger.d("Requesting notification permission", "IOSNotificationPermissionManager")
        return kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
            center.requestAuthorizationWithOptions(
                UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge
            ) { granted, error ->
                if (error != null) {
                    Logger.e(Exception("Permission request failed: ${error.localizedDescription}"), "Failed to request notification permission", "IOSNotificationPermissionManager")
                } else {
                    Logger.i("Notification permission request result: granted=$granted", "IOSNotificationPermissionManager")
                }
                continuation.resumeWith(Result.success(granted))
            }
        }
    }
}