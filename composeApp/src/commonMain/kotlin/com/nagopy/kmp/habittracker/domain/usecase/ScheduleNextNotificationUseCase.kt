package com.nagopy.kmp.habittracker.domain.usecase

import com.nagopy.kmp.habittracker.domain.notification.NotificationScheduler
import com.nagopy.kmp.habittracker.domain.repository.HabitRepository
import kotlinx.coroutines.flow.first

/**
 * Use case for scheduling the next notification in a sequential manner.
 * This replaces the batch scheduling approach with a sequential one where
 * only the next upcoming notification is scheduled at any given time.
 */
class ScheduleNextNotificationUseCase(
    private val notificationScheduler: NotificationScheduler,
    private val getNextTasksUseCase: GetNextTasksUseCase,
    private val habitRepository: HabitRepository
) {
    
    /**
     * Schedules the next upcoming notification for a specific habit.
     * Only schedules one notification - the next upcoming task for that habit.
     * 
     * @param habitId The ID of the habit to schedule the next notification for
     * @return true if a notification was scheduled, false if no future tasks exist
     */
    suspend fun scheduleNextNotificationForHabit(habitId: Long): Boolean {
        if (!notificationScheduler.areNotificationsEnabled()) {
            return false
        }
        
        val nextTask = getNextTasksUseCase.getNextTaskForHabit(habitId)
        return if (nextTask != null) {
            notificationScheduler.scheduleTaskNotification(nextTask)
            true
        } else {
            false
        }
    }
    
    /**
     * Schedules the next upcoming notification across all active habits.
     * Only schedules one notification - the earliest upcoming task across all habits.
     * 
     * @return true if a notification was scheduled, false if no future tasks exist
     */
    suspend fun scheduleNextUpcomingNotification(): Boolean {
        if (!notificationScheduler.areNotificationsEnabled()) {
            return false
        }
        
        val nextTask = getNextTasksUseCase.getNextUpcomingTask()
        return if (nextTask != null) {
            notificationScheduler.scheduleTaskNotification(nextTask)
            true
        } else {
            false
        }
    }
    
    /**
     * Reschedules notifications for all active habits.
     * This cancels all existing notifications and schedules the next upcoming notification
     * for each habit. This is used for device restart scenarios or when the notification
     * chain needs to be reset.
     */
    suspend fun rescheduleAllHabitNotifications() {
        if (!notificationScheduler.areNotificationsEnabled()) {
            return
        }
        
        // Cancel all existing notifications to start fresh
        notificationScheduler.cancelAllNotifications()
        
        // Get all active habits and schedule the next notification for each
        val activeHabits = habitRepository.getActiveHabits().first()
        for (habit in activeHabits) {
            scheduleNextNotificationForHabit(habit.id)
        }
    }
}