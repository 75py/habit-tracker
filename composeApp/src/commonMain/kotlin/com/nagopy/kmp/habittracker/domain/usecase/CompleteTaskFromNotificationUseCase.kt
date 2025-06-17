package com.nagopy.kmp.habittracker.domain.usecase

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

/**
 * Use case for completing tasks from notifications.
 * This encapsulates the business logic for handling task completion triggered by notification actions.
 * Platform-specific notification handling (cancellation, scheduling) should be done by the calling code.
 */
class CompleteTaskFromNotificationUseCase(
    private val completeTaskUseCase: CompleteTaskUseCase
) {
    
    /**
     * Completes a task from a notification action.
     * This only marks the task as completed. Platform-specific notification handling
     * (cancellation, next notification scheduling) should be handled by the caller.
     * 
     * @param habitId The ID of the habit to mark as completed
     * @param date The date for which to mark the habit as completed
     * @param scheduledTime The scheduled time for this task instance
     * @return The ID of the created habit log entry
     */
    suspend operator fun invoke(habitId: Long, date: LocalDate, scheduledTime: LocalTime): Long {
        return completeTaskUseCase(habitId, date, scheduledTime)
    }
}