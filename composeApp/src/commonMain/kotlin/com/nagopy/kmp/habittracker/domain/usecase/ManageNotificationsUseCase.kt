package com.nagopy.kmp.habittracker.domain.usecase

import com.nagopy.kmp.habittracker.domain.notification.NotificationScheduler
import com.nagopy.kmp.habittracker.domain.model.Task
import kotlinx.coroutines.flow.first

/**
 * Use case for managing task notifications.
 * This encapsulates the business logic for scheduling and managing notifications for tasks.
 */
class ManageNotificationsUseCase(
    private val notificationScheduler: NotificationScheduler,
    private val getTodayTasksUseCase: GetTodayTasksUseCase
) {
    
    /**
     * Schedules notifications for today's tasks.
     * This should be called when the app starts or when tasks are updated.
     */
    suspend fun scheduleNotificationsForTodayTasks() {
        // Get today's tasks as a snapshot (not continuous observation) to schedule notifications
        val tasks = getTodayTasksUseCase().first()
        // Only schedule notifications for tasks that haven't been completed yet
        val pendingTasks = tasks.filter { !it.isCompleted }
        notificationScheduler.scheduleTaskNotifications(pendingTasks)
    }
    
    /**
     * Schedules notification for a specific task.
     * 
     * @param task The task for which to schedule a notification
     */
    suspend fun scheduleTaskNotification(task: Task) {
        if (!task.isCompleted) {
            notificationScheduler.scheduleTaskNotification(task)
        }
    }
    
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