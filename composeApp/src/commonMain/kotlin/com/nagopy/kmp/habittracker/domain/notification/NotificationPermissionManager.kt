package com.nagopy.kmp.habittracker.domain.notification

/**
 * Interface for managing notification permissions across platforms.
 * This separates permission logic from notification scheduling.
 */
interface NotificationPermissionManager {
    
    /**
     * Checks if notification permissions are currently enabled.
     * 
     * @return true if notifications are permitted, false otherwise
     */
    suspend fun areNotificationsEnabled(): Boolean
    
    /**
     * Requests notification permission from the user.
     * On platforms that don't require explicit permission, this returns true immediately.
     * 
     * @return true if permission was granted or already enabled, false otherwise
     */
    suspend fun requestNotificationPermission(): Boolean
}