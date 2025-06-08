package com.nagopy.kmp.habittracker.domain.notification

import com.nagopy.kmp.habittracker.domain.model.Task

/**
 * Interface for scheduling notifications across platforms.
 * This interface defines the contract for notification scheduling without implementation details.
 */
interface NotificationScheduler {
    
    /**
     * Schedules a notification for a specific task.
     * The notification should be delivered at the task's scheduled time.
     * 
     * @param task The task for which to schedule a notification
     */
    suspend fun scheduleTaskNotification(task: Task)
    
    /**
     * Schedules notifications for multiple tasks.
     * This is useful for batch scheduling of notifications.
     * 
     * @param tasks The list of tasks for which to schedule notifications
     */
    suspend fun scheduleTaskNotifications(tasks: List<Task>)
    
    /**
     * Cancels a scheduled notification for a specific task.
     * 
     * @param task The task for which to cancel the notification
     */
    suspend fun cancelTaskNotification(task: Task)
    
    /**
     * Cancels all scheduled notifications for a specific habit.
     * This is useful when a habit is deleted or deactivated.
     * 
     * @param habitId The ID of the habit for which to cancel all notifications
     */
    suspend fun cancelHabitNotifications(habitId: Long)
    
    /**
     * Cancels all scheduled notifications.
     * This is useful for cleanup operations.
     */
    suspend fun cancelAllNotifications()
    
    /**
     * Checks if notifications are enabled/permitted by the system.
     * 
     * @return true if notifications are permitted, false otherwise
     */
    suspend fun areNotificationsEnabled(): Boolean
    
    /**
     * Requests notification permission from the user.
     * This is a no-op on platforms that don't require explicit permission.
     * 
     * @return true if permission was granted, false otherwise
     */
    suspend fun requestNotificationPermission(): Boolean
}