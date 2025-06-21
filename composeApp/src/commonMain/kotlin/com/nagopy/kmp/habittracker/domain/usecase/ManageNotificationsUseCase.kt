package com.nagopy.kmp.habittracker.domain.usecase

import com.nagopy.kmp.habittracker.domain.notification.NotificationScheduler
import com.nagopy.kmp.habittracker.domain.model.Task

/**
 * Use case for managing task notifications.
 * This encapsulates the business logic for scheduling and managing notifications for tasks.
 */
class ManageNotificationsUseCase(
    private val notificationScheduler: NotificationScheduler
) {
    
    /**
     * Cancels notification for a completed task.
     * 
     * @param task The task for which to cancel the notification
     */
    suspend fun cancelTaskNotification(task: Task) {
        notificationScheduler.cancelTaskNotification(task)
    }
    
    /**
     * Cancels all notifications for a specific habit.
     * This is useful when a habit is deleted or deactivated.
     * 
     * @param habitId The ID of the habit for which to cancel all notifications
     */
    suspend fun cancelHabitNotifications(habitId: Long) {
        notificationScheduler.cancelHabitNotifications(habitId)
    }
    
    /**
     * Checks if notifications are enabled and requests permission if needed.
     * 
     * @return true if notifications are enabled or permission was granted, false otherwise
     */
    suspend fun ensureNotificationsEnabled(): Boolean {
        return if (notificationScheduler.areNotificationsEnabled()) {
            true
        } else {
            notificationScheduler.requestNotificationPermission()
        }
    }
}