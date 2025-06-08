package com.nagopy.kmp.habittracker.domain.usecase

import com.nagopy.kmp.habittracker.domain.notification.NotificationScheduler
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

/**
 * Use case for completing tasks from notifications.
 * This encapsulates the business logic for handling task completion triggered by notification actions.
 */
class CompleteTaskFromNotificationUseCase(
    private val completeTaskUseCase: CompleteTaskUseCase,
    private val notificationScheduler: NotificationScheduler
) {
    
    /**
     * Completes a task from a notification action.
     * This cancels the notification and marks the task as completed.
     * 
     * @param habitId The ID of the habit to mark as completed
     * @param date The date for which to mark the habit as completed
     * @param scheduledTime The scheduled time for this task instance
     * @return The ID of the created habit log entry
     */
    suspend operator fun invoke(habitId: Long, date: LocalDate, scheduledTime: LocalTime): Long {
        // Complete the task using the existing use case
        val logId = completeTaskUseCase(habitId, date, scheduledTime)
        
        // Cancel the notification for this specific task
        // We create a temporary task object to identify the notification to cancel
        val task = com.nagopy.kmp.habittracker.domain.model.Task(
            habitId = habitId,
            habitName = "", // These fields are not used for cancellation
            habitDescription = "",
            habitColor = "",
            date = date,
            scheduledTime = scheduledTime,
            isCompleted = true
        )
        notificationScheduler.cancelTaskNotification(task)
        
        return logId
    }
}