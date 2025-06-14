package com.nagopy.kmp.habittracker.presentation.permission

/**
 * Represents the state of notification permission request flow.
 */
sealed class NotificationPermissionState {
    
    /**
     * Initial state - no permission request started yet
     */
    data object Initial : NotificationPermissionState()
    
    /**
     * Show explanation dialog before requesting notification permission
     */
    data object ShowNotificationExplanation : NotificationPermissionState()
    
    /**
     * Requesting notification permission from user
     */
    data object RequestingNotificationPermission : NotificationPermissionState()
    
    /**
     * Notification permission granted, show explanation for exact alarm permission
     */
    data object ShowExactAlarmExplanation : NotificationPermissionState()
    
    /**
     * Requesting exact alarm permission from user
     */
    data object RequestingExactAlarmPermission : NotificationPermissionState()
    
    /**
     * Notification permission denied
     */
    data object NotificationPermissionDenied : NotificationPermissionState()
    
    /**
     * All permissions completed (either granted or denied)
     */
    data object Completed : NotificationPermissionState()
}