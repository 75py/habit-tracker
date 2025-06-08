package com.nagopy.kmp.habittracker.domain.usecase

import com.nagopy.kmp.habittracker.domain.notification.NotificationPermissionManager
import com.nagopy.kmp.habittracker.domain.storage.AppPreferences

/**
 * Use case for requesting notification permissions on app startup.
 * This ensures notification permissions are requested consistently across platforms
 * during the first app launch.
 */
class RequestNotificationPermissionOnStartupUseCase(
    private val notificationPermissionManager: NotificationPermissionManager,
    private val appPreferences: AppPreferences
) {
    
    /**
     * Requests notification permission if not already requested before.
     * This should be called once during app startup.
     * 
     * @return true if permission was granted or already enabled, false otherwise
     */
    suspend fun invoke(): Boolean {
        // Check if we've already requested permission
        val alreadyRequested = appPreferences.getBoolean(
            AppPreferences.KEY_NOTIFICATION_PERMISSION_REQUESTED, 
            false
        )
        
        // If already requested before, just check current status
        if (alreadyRequested) {
            return notificationPermissionManager.areNotificationsEnabled()
        }
        
        // Mark as requested to avoid asking again
        appPreferences.setBoolean(AppPreferences.KEY_NOTIFICATION_PERMISSION_REQUESTED, true)
        
        // Request permission
        return if (notificationPermissionManager.areNotificationsEnabled()) {
            true
        } else {
            notificationPermissionManager.requestNotificationPermission()
        }
    }
}