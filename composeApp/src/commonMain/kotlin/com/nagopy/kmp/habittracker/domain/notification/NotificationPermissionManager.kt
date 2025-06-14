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
    
    /**
     * Checks if exact alarm permission is available and granted.
     * Only relevant on Android SDK 31+ where SCHEDULE_EXACT_ALARM permission is required.
     * 
     * @return true if exact alarm permission is granted or not required, false otherwise
     */
    suspend fun canScheduleExactAlarms(): Boolean
    
    /**
     * Requests exact alarm permission from the user.
     * Only relevant on Android SDK 31+ where SCHEDULE_EXACT_ALARM permission is required.
     * On platforms that don't require this permission, this returns true immediately.
     * 
     * @return true if permission was granted or already enabled, false otherwise
     */
    suspend fun requestExactAlarmPermission(): Boolean
}