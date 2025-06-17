package com.nagopy.kmp.habittracker.domain.usecase

import com.nagopy.kmp.habittracker.util.Logger
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

/**
 * Use case for completing tasks from notifications.
 * This encapsulates the business logic for handling task completion triggered by notification actions.
 */
class CompleteTaskFromNotificationUseCase(
    private val completeTaskUseCase: CompleteTaskUseCase,
    private val scheduleNextNotificationUseCase: ScheduleNextNotificationUseCase
) {
    
    /**
     * Completes a task from a notification action.
     * This marks the task as completed and schedules the next notification.
     * Platform-specific code should handle notification cancellation.
     * 
     * @param habitId The ID of the habit to mark as completed
     * @param date The date for which to mark the habit as completed
     * @param scheduledTime The scheduled time for this task instance
     * @return The ID of the created habit log entry
     */
    suspend operator fun invoke(habitId: Long, date: LocalDate, scheduledTime: LocalTime): Long {
        // Complete the task using the existing use case
        val logId = completeTaskUseCase(habitId, date, scheduledTime)
        
        // Schedule the next notification for this habit
        // This is critical for maintaining the notification chain, but should not fail the current completion
        try {
            val wasScheduled = scheduleNextNotificationUseCase.scheduleNextNotificationForHabit(habitId)
            if (wasScheduled) {
                Logger.i("Successfully scheduled next notification for habitId: $habitId", "CompleteTaskFromNotificationUseCase")
            } else {
                Logger.d("No next notification to schedule for habitId: $habitId", "CompleteTaskFromNotificationUseCase")
            }
        } catch (e: Exception) {
            // Log and continue - failing to schedule next notification shouldn't affect current completion
            Logger.e(e, "Failed to schedule next notification for habitId: $habitId", "CompleteTaskFromNotificationUseCase")
        }
        
        return logId
    }
}