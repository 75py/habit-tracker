package com.nagopy.kmp.habittracker.notification

import com.nagopy.kmp.habittracker.domain.model.Task
import com.nagopy.kmp.habittracker.domain.model.Habit
import com.nagopy.kmp.habittracker.domain.model.FrequencyType
import com.nagopy.kmp.habittracker.domain.notification.NotificationScheduler
import com.nagopy.kmp.habittracker.domain.repository.HabitRepository
import com.nagopy.kmp.habittracker.domain.usecase.CompleteTaskFromNotificationUseCase
import com.nagopy.kmp.habittracker.domain.usecase.ScheduleNextNotificationUseCase
import com.nagopy.kmp.habittracker.util.Logger
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone

import kotlinx.datetime.toInstant
import kotlinx.datetime.toNSDate
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

import platform.UserNotifications.*
import platform.Foundation.*

/**
 * iOS implementation of NotificationScheduler using UserNotifications framework.
 */
@OptIn(ExperimentalForeignApi::class)
class IOSNotificationScheduler(
    private val habitRepository: HabitRepository
) : NotificationScheduler, KoinComponent {

    companion object {
        private const val HABIT_REMINDER_CATEGORY = "HABIT_REMINDER"
        private const val COMPLETE_ACTION = "COMPLETE_ACTION"
    }

    private val completeTaskFromNotificationUseCase: CompleteTaskFromNotificationUseCase by inject()
    private val scheduleNextNotificationUseCase: ScheduleNextNotificationUseCase by inject()
    private val center = UNUserNotificationCenter.currentNotificationCenter()

    override suspend fun scheduleTaskNotification(task: Task) {
        Logger.d("Attempting to schedule notification for task: ${task.habitName} at ${task.scheduledTime}", "IOSNotificationScheduler")
        
        if (!areNotificationsEnabled()) {
            Logger.w("Notifications are not enabled, skipping notification for task: ${task.habitName}", "IOSNotificationScheduler")
            return
        }

        val identifier = generateNotificationId(task)
        Logger.d("Generated notification ID: $identifier", "IOSNotificationScheduler")
        
        // Fetch the actual habit to get current name and description
        val habit = habitRepository.getHabit(task.habitId)
        if (habit == null) {
            Logger.w("Habit not found for task: ${task.habitName} (ID: ${task.habitId}), skipping notification", "IOSNotificationScheduler")
            return
        }
        val habitName = habit.name
        val habitDescription = habit.description
        
        // Create notification content
        val content = UNMutableNotificationContent().apply {
            setTitle(habitName)
            setBody(habitDescription.ifEmpty { "Time to complete your habit!" })
            setSound(UNNotificationSound.defaultSound())
            setCategoryIdentifier(HABIT_REMINDER_CATEGORY)
        }

        // Calculate trigger time
        val localDateTime = LocalDateTime(task.date, task.scheduledTime)
        val triggerDate = localDateTime.toInstant(TimeZone.currentSystemDefault()).toNSDate()
        
        // Create calendar trigger based on habit frequency type
        val calendar = NSCalendar.currentCalendar()
        val trigger = createTriggerForTask(habit, triggerDate, calendar)

        // Create request
        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = identifier,
            content = content,
            trigger = trigger
        )

        // Schedule notification
        center.addNotificationRequest(request) { error ->
            if (error != null) {
                Logger.e(Exception("Notification scheduling failed: ${error.localizedDescription}"), "Failed to schedule notification for task: ${task.habitName}", "IOSNotificationScheduler")
            } else {
                Logger.i("Successfully scheduled notification for task: ${task.habitName} at ${task.scheduledTime}", "IOSNotificationScheduler")
            }
        }
    }

    override suspend fun scheduleTaskNotifications(tasks: List<Task>) {
        Logger.d("Scheduling ${tasks.size} task notifications", "IOSNotificationScheduler")
        tasks.forEach { task ->
            scheduleTaskNotification(task)
        }
    }

    override suspend fun cancelTaskNotification(task: Task) {
        val identifier = generateNotificationId(task)
        Logger.d("Canceling notification for task: ${task.habitName} with identifier: $identifier", "IOSNotificationScheduler")
        center.removePendingNotificationRequestsWithIdentifiers(listOf(identifier))
        center.removeDeliveredNotificationsWithIdentifiers(listOf(identifier))
        Logger.i("Successfully canceled notification for task: ${task.habitName}", "IOSNotificationScheduler")
    }

    override suspend fun cancelHabitNotifications(habitId: Long) {
        Logger.d("Canceling notifications for habitId: $habitId", "IOSNotificationScheduler")
        // Get all pending notifications and filter by habit ID
        center.getPendingNotificationRequestsWithCompletionHandler { requests ->
            @Suppress("UNCHECKED_CAST")
            val typedRequests = requests as? List<UNNotificationRequest>
            val identifiersToCancel = typedRequests?.mapNotNull { request ->
                val requestIdentifier = request.identifier
                if (requestIdentifier.startsWith("${habitId}_")) {
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
            } else {
                Logger.d("No notifications found to cancel for habitId: $habitId", "IOSNotificationScheduler")
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

    private fun generateNotificationId(task: Task): String {
        // Generate an ID based on habit ID and date only
        // This ensures all tasks for the same habit on the same day share the same notification ID,
        // causing new notifications to replace existing ones instead of creating multiple notifications
        return "${task.habitId}_${task.date}"
    }

    fun setupNotificationCategories() {
        Logger.d("Setting up notification categories", "IOSNotificationScheduler")
        // Create the complete action
        val completeAction = UNNotificationAction.actionWithIdentifier(
            identifier = COMPLETE_ACTION,
            title = "Complete",
            options = UNNotificationActionOptionNone
        )

        // Create the category
        val category = UNNotificationCategory.categoryWithIdentifier(
            identifier = HABIT_REMINDER_CATEGORY,
            actions = listOf<UNNotificationAction>(completeAction),
            intentIdentifiers = listOf<String>(),
            options = UNNotificationCategoryOptionNone
        )

        // Register the category
        center.setNotificationCategories(setOf(category))
        Logger.i("Successfully set up notification categories", "IOSNotificationScheduler")
    }

    fun handleNotificationResponse(response: UNNotificationResponse) {
        if (response.actionIdentifier == COMPLETE_ACTION) {
            val identifier = response.notification.request.identifier
            val parts = identifier.split("_")
            
            if (parts.size >= 2) {
                try {
                    val habitId = parts[0].toLong()
                    val date = kotlinx.datetime.LocalDate.parse(parts[1])
                    // Use default time since we're now using habit-level notifications
                    val time = kotlinx.datetime.LocalTime(9, 0)
                    
                    Logger.d("Processing notification response for habitId: $habitId, date: $date", "IOSNotificationScheduler")
                    
                    // Handle completion in background
                    CoroutineScope(Dispatchers.Default).launch {
                        try {
                            completeTaskFromNotificationUseCase(habitId, date, time)
                            Logger.i("Successfully completed task for habitId: $habitId", "IOSNotificationScheduler")
                            
                            // Schedule the next notification for this habit
                            // This is critical for maintaining the notification chain, but should not fail the current completion
                            try {
                                val wasScheduled = scheduleNextNotificationUseCase.scheduleNextNotificationForHabit(habitId)
                                if (wasScheduled) {
                                    Logger.i("Successfully scheduled next notification for habitId: $habitId", "IOSNotificationScheduler")
                                } else {
                                    Logger.d("No next notification to schedule for habitId: $habitId", "IOSNotificationScheduler")
                                }
                            } catch (e: Exception) {
                                // Log and continue - failing to schedule next notification shouldn't affect current completion
                                Logger.e(e, "Failed to schedule next notification for habitId: $habitId", "IOSNotificationScheduler")
                            }
                        } catch (e: Exception) {
                            // This catches database exceptions and other unexpected errors during task completion
                            Logger.e(e, "Failed to complete task for habitId: $habitId", "IOSNotificationScheduler")
                        }
                    }
                } catch (e: NumberFormatException) {
                    Logger.e(e, "Invalid habitId format in notification identifier: $identifier", "IOSNotificationScheduler")
                } catch (e: IllegalArgumentException) {
                    Logger.e(e, "Invalid date/time format in notification identifier: $identifier", "IOSNotificationScheduler")
                } catch (e: Exception) {
                    Logger.e(e, "Unexpected error parsing notification identifier: $identifier", "IOSNotificationScheduler")
                }
            } else {
                Logger.w("Invalid notification identifier format (expected at least 2 parts): $identifier", "IOSNotificationScheduler")
            }
        }
    }

    private fun createTriggerForTask(habit: Habit, triggerDate: NSDate, calendar: NSCalendar): UNCalendarNotificationTrigger {
        return when (habit.frequencyType) {
            FrequencyType.ONCE_DAILY -> {
                // For daily habits, repeat daily at the same time
                val components = calendar.components(
                    NSCalendarUnitHour or NSCalendarUnitMinute,
                    triggerDate
                )
                Logger.d("Creating daily repeating trigger for habit: ${habit.name} at ${components.hour}:${components.minute}", "IOSNotificationScheduler")
                UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(
                    components,
                    repeats = true
                )
            }
            FrequencyType.HOURLY -> {
                // For hourly habits, repeat every hour at the same minute
                val components = calendar.components(NSCalendarUnitMinute, triggerDate)
                Logger.d("Creating hourly repeating trigger for habit: ${habit.name} at minute ${components.minute}", "IOSNotificationScheduler")
                UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(
                    components,
                    repeats = true
                )
            }
            FrequencyType.INTERVAL -> {
                // For interval habits, we need to handle different interval types
                createIntervalTrigger(habit, triggerDate, calendar)
            }
        }
    }

    private fun createIntervalTrigger(habit: Habit, triggerDate: NSDate, calendar: NSCalendar): UNCalendarNotificationTrigger {
        val intervalMinutes = habit.intervalMinutes
        
        Logger.d("Creating interval trigger for habit: ${habit.name} with interval ${intervalMinutes} minutes", "IOSNotificationScheduler")
        
        when {
            // For intervals that divide evenly into an hour, use minute-based repetition
            intervalMinutes <= 60 && 60 % intervalMinutes == 0 -> {
                val components = calendar.components(NSCalendarUnitMinute, triggerDate)
                Logger.d("Using minute-based repetition for ${intervalMinutes}min interval at minute ${components.minute}", "IOSNotificationScheduler")
                return UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(
                    components,
                    repeats = true
                )
            }
            // For intervals that divide evenly into a day, use hour/minute repetition
            intervalMinutes < 1440 && 1440 % intervalMinutes == 0 -> {
                val components = calendar.components(NSCalendarUnitHour or NSCalendarUnitMinute, triggerDate)
                Logger.d("Using hour/minute-based repetition for ${intervalMinutes}min interval at ${components.hour}:${components.minute}", "IOSNotificationScheduler")
                return UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(
                    components,
                    repeats = true
                )
            }
            // For other intervals, fall back to daily repetition (not ideal, but better than no repetition)
            else -> {
                Logger.w("Interval ${intervalMinutes} minutes doesn't divide evenly into hour/day, using daily repetition", "IOSNotificationScheduler")
                val components = calendar.components(NSCalendarUnitHour or NSCalendarUnitMinute, triggerDate)
                return UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(
                    components,
                    repeats = true
                )
            }
        }
    }
}