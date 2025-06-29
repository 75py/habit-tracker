package com.nagopy.kmp.habittracker.notification

import com.nagopy.kmp.habittracker.domain.model.Task
import com.nagopy.kmp.habittracker.domain.model.Habit
import com.nagopy.kmp.habittracker.domain.model.FrequencyType
import com.nagopy.kmp.habittracker.domain.model.HabitDetail
import com.nagopy.kmp.habittracker.domain.model.frequencyType
import com.nagopy.kmp.habittracker.domain.notification.NotificationScheduler
import com.nagopy.kmp.habittracker.domain.repository.HabitRepository
import com.nagopy.kmp.habittracker.domain.usecase.CompleteTaskFromNotificationUseCase
import com.nagopy.kmp.habittracker.util.Logger
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import platform.UserNotifications.*
import platform.Foundation.*

/**
 * iOS implementation of NotificationScheduler using UserNotifications framework.
 * Handles iOS's 64-notification limit by prioritizing notifications closest to current time
 * and using background tasks for periodic re-scheduling.
 */
@OptIn(ExperimentalForeignApi::class)
class IOSNotificationScheduler(
    private val habitRepository: HabitRepository
) : NotificationScheduler, KoinComponent {

    companion object {
        private const val HABIT_REMINDER_CATEGORY = "HABIT_REMINDER"
        private const val COMPLETE_ACTION = "COMPLETE_ACTION"
        private const val MAX_IOS_NOTIFICATIONS = 64
    }

    /**
     * Data class representing a scheduled notification with timing information
     */
    private data class ScheduledNotification(
        val habit: Habit,
        val time: LocalTime,
        val identifier: String,
        val distanceFromNow: Long // Minutes from current time
    )

    private val completeTaskFromNotificationUseCase: CompleteTaskFromNotificationUseCase by inject()
    private val center = UNUserNotificationCenter.currentNotificationCenter()

    override suspend fun scheduleTaskNotification(task: Task) {
        Logger.d("Scheduling notification for task: ${task.habitName} at ${task.scheduledTime}", "IOSNotificationScheduler")
        
        if (!areNotificationsEnabled()) {
            Logger.w("Notifications are not enabled, skipping notification for task: ${task.habitName}", "IOSNotificationScheduler")
            return
        }

        // Re-schedule all notifications to ensure proper prioritization
        rescheduleAllNotifications()
    }

    override suspend fun scheduleTaskNotifications(tasks: List<Task>) {
        Logger.d("Scheduling ${tasks.size} task notifications", "IOSNotificationScheduler")
        
        if (!areNotificationsEnabled()) {
            Logger.w("Notifications are not enabled, skipping all notifications", "IOSNotificationScheduler")
            return
        }

        // Re-schedule all notifications to ensure proper prioritization
        rescheduleAllNotifications()
    }

    /**
     * Reschedules all notifications, respecting the 64-notification limit by prioritizing
     * notifications closest to the current time.
     */
    private suspend fun rescheduleAllNotifications() {
        Logger.d("Rescheduling all notifications with 64-limit prioritization", "IOSNotificationScheduler")
        
        // Cancel all existing notifications first
        Logger.d("Cancelling all existing notifications", "IOSNotificationScheduler")
        cancelAllNotifications()
        
        // Get all active habits
        Logger.d("Fetching all active habits from repository", "IOSNotificationScheduler")
        val allHabits = habitRepository.getAllHabits().first().filter { it.isActive }
        Logger.d("Found ${allHabits.size} active habits", "IOSNotificationScheduler")

        // Generate all potential notifications
        val allNotifications = mutableListOf<ScheduledNotification>()
        val currentTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time
        Logger.d("Current time: $currentTime", "IOSNotificationScheduler")
        
        allHabits.forEach { habit ->
            Logger.d("Generating notifications for habit: ${habit.name} (ID: ${habit.id})", "IOSNotificationScheduler")
            val habitNotifications = generateNotificationsForHabit(habit, currentTime)
            Logger.d("Generated ${habitNotifications.size} notifications for habit: ${habit.name}", "IOSNotificationScheduler")
            allNotifications.addAll(habitNotifications)
        }
        
        // Sort by distance from current time and take only 64
        Logger.d("Sorting ${allNotifications.size} notifications by distance from current time", "IOSNotificationScheduler")
        val prioritizedNotifications = allNotifications
            .sortedBy { it.distanceFromNow }
            .take(MAX_IOS_NOTIFICATIONS)
        
        Logger.d("Generated ${allNotifications.size} total notifications, scheduling ${prioritizedNotifications.size} closest ones", "IOSNotificationScheduler")
        
        if (allNotifications.size > MAX_IOS_NOTIFICATIONS) {
            val furthestScheduled = prioritizedNotifications.last()
            val closestSkipped = allNotifications
                .sortedBy { it.distanceFromNow }
                .drop(MAX_IOS_NOTIFICATIONS)
                .firstOrNull()
            
            if (closestSkipped != null) {
                Logger.i(
                    "iOS 64-notification limit reached. Furthest scheduled: ${furthestScheduled.time} (${furthestScheduled.distanceFromNow}min), " +
                    "closest skipped: ${closestSkipped.time} (${closestSkipped.distanceFromNow}min)",
                    "IOSNotificationScheduler"
                )
            }
        }
        
        // Schedule the prioritized notifications
        Logger.d("Scheduling ${prioritizedNotifications.size} prioritized notifications", "IOSNotificationScheduler")
        prioritizedNotifications.forEach { notification ->
            Logger.d("Scheduling notification for habit ${notification.habit.id} at ${notification.time} (${notification.distanceFromNow}min from now)", "IOSNotificationScheduler")
            scheduleNotification(notification)
        }
        
        // Register background task for next refresh
        Logger.d("Registering background task for next refresh", "IOSNotificationScheduler")
        registerBackgroundRefresh()
        Logger.d("Notification rescheduling completed", "IOSNotificationScheduler")
    }

    /**
     * Generates all potential notifications for a habit
     */
    private fun generateNotificationsForHabit(habit: Habit, currentTime: LocalTime): List<ScheduledNotification> {
        val notifications = mutableListOf<ScheduledNotification>()
        
        when (habit.frequencyType) {
            FrequencyType.ONCE_DAILY -> {
                val detail = habit.detail as HabitDetail.OnceDailyHabitDetail
                detail.scheduledTimes.forEachIndexed { index, scheduledTime ->
                    val identifier = "habit_${habit.id}_daily_$index"
                    val distance = calculateTimeDistance(currentTime, scheduledTime)
                    notifications.add(
                        ScheduledNotification(habit, scheduledTime, identifier, distance)
                    )
                }
            }
            FrequencyType.INTERVAL -> {
                // Note: This branch now handles all interval-based habits, including
                // those with hourly intervals (60, 120, 180 minutes, etc.) that were
                // previously handled by the removed HOURLY branch.
                val detail = habit.detail as HabitDetail.IntervalHabitDetail
                val intervalMinutes = detail.intervalMinutes
                val endTime = detail.endTime ?: LocalTime(23, 59)
                val startTime = detail.startTime
                
                val notificationTimes = mutableSetOf<LocalTime>()
                
                if (startTime <= endTime) {
                    var time = startTime
                    
                    while (time <= endTime) {
                        notificationTimes.add(time)
                        
                        val totalMinutes = time.hour * 60 + time.minute + intervalMinutes
                        val newHour = (totalMinutes / 60) % 24
                        val newMinute = totalMinutes % 60
                        time = LocalTime(newHour, newMinute)
                        
                        if (totalMinutes >= 24 * 60) break
                    }
                }
                
                notificationTimes.forEachIndexed { index, time ->
                    val identifier = "habit_${habit.id}_interval_${time.hour}_${time.minute}"
                    val distance = calculateTimeDistance(currentTime, time)
                    notifications.add(
                        ScheduledNotification(habit, time, identifier, distance)
                    )
                }
            }
        }
        
        return notifications
    }

    /**
     * Calculates distance in minutes from current time to target time.
     * Considers next occurrence if target time has already passed today.
     */
    private fun calculateTimeDistance(currentTime: LocalTime, targetTime: LocalTime): Long {
        val currentMinutes = currentTime.hour * 60 + currentTime.minute
        val targetMinutes = targetTime.hour * 60 + targetTime.minute
        
        return if (targetMinutes >= currentMinutes) {
            // Today
            (targetMinutes - currentMinutes).toLong()
        } else {
            // Tomorrow
            (24 * 60 - currentMinutes + targetMinutes).toLong()
        }
    }

    /**
     * Schedules a single notification
     */
    private fun scheduleNotification(notification: ScheduledNotification) {
        val content = UNMutableNotificationContent().apply {
            setTitle(notification.habit.name)
            setBody(notification.habit.description.ifEmpty { "Time to complete your habit!" })
            setSound(UNNotificationSound.defaultSound())
            setCategoryIdentifier(HABIT_REMINDER_CATEGORY)
        }

        val components = NSDateComponents().apply {
            hour = notification.time.hour.toLong()
            minute = notification.time.minute.toLong()
        }

        val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(
            components,
            repeats = true
        )

        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = notification.identifier,
            content = content,
            trigger = trigger
        )

        center.addNotificationRequest(request) { error ->
            if (error != null) {
                Logger.e(
                    Exception("Notification scheduling failed: ${error.localizedDescription}"),
                    "Failed to schedule notification for habit: ${notification.habit.name} at ${notification.time}",
                    "IOSNotificationScheduler"
                )
            } else {
                Logger.d(
                    "Successfully scheduled notification for habit: ${notification.habit.name} at ${notification.time}",
                    "IOSNotificationScheduler"
                )
            }
        }
    }

    /**
     * Registers a background task to refresh notifications periodically
     */
    private fun registerBackgroundRefresh() {
        // Call Swift BackgroundTaskManager to schedule background refresh
        // This is a no-op here as the actual scheduling is done in Swift
        Logger.d("Background refresh will be managed by Swift BackgroundTaskManager", "IOSNotificationScheduler")
    }

    override suspend fun cancelTaskNotification(task: Task) {
        // Cancel all notifications for this habit
        cancelHabitNotifications(task.habitId)
        
        // Re-schedule all notifications to fill the gap with next priority notifications
        rescheduleAllNotifications()
    }

    override suspend fun cancelHabitNotifications(habitId: Long) {
        Logger.d("Canceling notifications for habitId: $habitId", "IOSNotificationScheduler")
        
        center.getPendingNotificationRequestsWithCompletionHandler { requests ->
            @Suppress("UNCHECKED_CAST")
            val typedRequests = requests as? List<UNNotificationRequest>
            val identifiersToCancel = typedRequests?.mapNotNull { request ->
                val requestIdentifier = request.identifier
                if (requestIdentifier.startsWith("habit_${habitId}")) {
                    requestIdentifier
                } else {
                    null
                }
            } ?: emptyList()
            
            Logger.d("Found ${identifiersToCancel.size} notifications to cancel for habitId: $habitId", "IOSNotificationScheduler")
            
            if (identifiersToCancel.isNotEmpty()) {
                center.removePendingNotificationRequestsWithIdentifiers(identifiersToCancel)
                center.removeDeliveredNotificationsWithIdentifiers(identifiersToCancel)
                Logger.i("Successfully canceled ${identifiersToCancel.size} notifications for habitId: $habitId", "IOSNotificationScheduler")
            }
        }
    }

    override suspend fun cancelAllNotifications() {
        Logger.d("Canceling all notifications", "IOSNotificationScheduler")
        center.removeAllPendingNotificationRequests()
        center.removeAllDeliveredNotifications()
        Logger.i("Successfully canceled all notifications", "IOSNotificationScheduler")
    }

    override suspend fun areNotificationsEnabled(): Boolean {
        return kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
            center.getNotificationSettingsWithCompletionHandler { settings ->
                val isEnabled = settings?.authorizationStatus == UNAuthorizationStatusAuthorized
                Logger.d("Notification authorization status check: enabled=$isEnabled", "IOSNotificationScheduler")
                continuation.resumeWith(Result.success(isEnabled))
            }
        }
    }

    override suspend fun requestNotificationPermission(): Boolean {
        Logger.d("Requesting notification permission", "IOSNotificationScheduler")
        return kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
            center.requestAuthorizationWithOptions(
                UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge
            ) { granted, error ->
                if (error != null) {
                    Logger.e(Exception("Permission request failed: ${error.localizedDescription}"), "Failed to request notification permission", "IOSNotificationScheduler")
                } else {
                    Logger.i("Notification permission request result: granted=$granted", "IOSNotificationScheduler")
                }
                continuation.resumeWith(Result.success(granted))
            }
        }
    }

    fun setupNotificationCategories() {
        Logger.d("Setting up notification categories", "IOSNotificationScheduler")
        
        val completeAction = UNNotificationAction.actionWithIdentifier(
            identifier = COMPLETE_ACTION,
            title = "Complete",
            options = UNNotificationActionOptionNone
        )

        val category = UNNotificationCategory.categoryWithIdentifier(
            identifier = HABIT_REMINDER_CATEGORY,
            actions = listOf<UNNotificationAction>(completeAction),
            intentIdentifiers = listOf<String>(),
            options = UNNotificationCategoryOptionNone
        )

        center.setNotificationCategories(setOf(category))
        Logger.i("Successfully set up notification categories", "IOSNotificationScheduler")
    }

    fun handleNotificationResponse(response: UNNotificationResponse) {
        if (response.actionIdentifier == COMPLETE_ACTION) {
            val identifier = response.notification.request.identifier
            Logger.d("Processing notification response for identifier: $identifier", "IOSNotificationScheduler")
            
            // Extract habit ID from identifier (format: habit_12345 or habit_12345_daily_0, etc.)
            val habitIdMatch = Regex("habit_(\\d+)").find(identifier)
            if (habitIdMatch != null) {
                try {
                    val habitId = habitIdMatch.groupValues[1].toLong()
                    val currentDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                    
                    Logger.d("Completing task for habitId: $habitId at current time: ${currentDateTime.time}", "IOSNotificationScheduler")
                    
                    CoroutineScope(Dispatchers.Default).launch {
                        try {
                            completeTaskFromNotificationUseCase(habitId, currentDateTime.date, currentDateTime.time)
                            Logger.i("Successfully completed task for habitId: $habitId", "IOSNotificationScheduler")
                        } catch (e: Exception) {
                            Logger.e(e, "Failed to complete task for habitId: $habitId", "IOSNotificationScheduler")
                        }
                    }
                } catch (e: NumberFormatException) {
                    Logger.e(e, "Invalid habitId format in notification identifier: $identifier", "IOSNotificationScheduler")
                }
            } else {
                Logger.w("Could not extract habitId from notification identifier: $identifier", "IOSNotificationScheduler")
            }
        }
    }

    /**
     * Called by background tasks to refresh notifications.
     * Should be exposed to Swift for BGTaskScheduler integration.
     */
    suspend fun performBackgroundRefresh() {
        Logger.d("Performing background notification refresh", "IOSNotificationScheduler")
        Logger.d("Current time: ${Clock.System.now()}", "IOSNotificationScheduler")
        
        try {
            Logger.d("Calling rescheduleAllNotifications() to refresh all habit notifications", "IOSNotificationScheduler")
            rescheduleAllNotifications()
            Logger.i("Background notification refresh completed successfully", "IOSNotificationScheduler")
        } catch (e: Exception) {
            Logger.e(e, "Background notification refresh failed with error: ${e.message}", "IOSNotificationScheduler")
            throw e
        }
    }
}