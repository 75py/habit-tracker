package com.nagopy.kmp.habittracker.notification

import com.nagopy.kmp.habittracker.domain.model.Task
import com.nagopy.kmp.habittracker.domain.model.Habit
import com.nagopy.kmp.habittracker.domain.model.FrequencyType
import com.nagopy.kmp.habittracker.domain.notification.NotificationScheduler
import com.nagopy.kmp.habittracker.domain.repository.HabitRepository
import com.nagopy.kmp.habittracker.domain.usecase.CompleteTaskFromNotificationUseCase
import com.nagopy.kmp.habittracker.util.Logger
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toNSDate
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import platform.UserNotifications.*
import platform.Foundation.*

/**
 * iOS implementation of NotificationScheduler using UserNotifications framework.
 * Uses native iOS repeating notifications with UNCalendarNotificationTrigger.
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
    private val center = UNUserNotificationCenter.currentNotificationCenter()

    override suspend fun scheduleTaskNotification(task: Task) {
        Logger.d("Scheduling notification for task: ${task.habitName} at ${task.scheduledTime}", "IOSNotificationScheduler")
        
        if (!areNotificationsEnabled()) {
            Logger.w("Notifications are not enabled, skipping notification for task: ${task.habitName}", "IOSNotificationScheduler")
            return
        }

        // Fetch the actual habit to get frequency type and interval info
        val habit = habitRepository.getHabit(task.habitId)
        if (habit == null) {
            Logger.w("Habit not found for task: ${task.habitName} (ID: ${task.habitId}), skipping notification", "IOSNotificationScheduler")
            return
        }
        
        scheduleHabitNotification(habit)
    }
    
    private suspend fun scheduleHabitNotification(habit: Habit) {
        // Use a simple identifier per habit to avoid duplicates
        val identifier = "habit_${habit.id}"
        
        // Create notification content
        val content = UNMutableNotificationContent().apply {
            setTitle(habit.name)
            setBody(habit.description.ifEmpty { "Time to complete your habit!" })
            setSound(UNNotificationSound.defaultSound())
            setCategoryIdentifier(HABIT_REMINDER_CATEGORY)
        }

        // Create trigger based on frequency type
        when (habit.frequencyType) {
            FrequencyType.ONCE_DAILY -> {
                // Daily at specific times
                createDailyTriggers(habit, identifier, content)
                return
            }
            FrequencyType.HOURLY -> {
                // Hourly at specific minute
                createHourlyTriggers(habit, identifier, content)
                return
            }
            FrequencyType.INTERVAL -> {
                // Create multiple triggers for interval minutes within each hour
                createIntervalTriggers(habit, identifier, content)
                return
            }
        }
    }
    
    private fun createDailyTriggers(habit: Habit, baseIdentifier: String, content: UNMutableNotificationContent) {
        habit.scheduledTimes.forEachIndexed { index, scheduledTime ->
            val identifier = "${baseIdentifier}_daily_$index"
            
            val components = NSDateComponents().apply {
                hour = scheduledTime.hour.toLong()
                minute = scheduledTime.minute.toLong()
            }
            
            val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(
                components,
                repeats = true
            )
            
            val request = UNNotificationRequest.requestWithIdentifier(
                identifier = identifier,
                content = content,
                trigger = trigger
            )
            
            center.addNotificationRequest(request) { error ->
                if (error != null) {
                    Logger.e(Exception("Daily notification scheduling failed: ${error.localizedDescription}"), "Failed to schedule daily notification for habit: ${habit.name} at ${scheduledTime}", "IOSNotificationScheduler")
                } else {
                    Logger.d("Successfully scheduled daily notification for habit: ${habit.name} at ${scheduledTime}", "IOSNotificationScheduler")
                }
            }
        }
    }
    
    private fun createHourlyTriggers(habit: Habit, baseIdentifier: String, content: UNMutableNotificationContent) {
        val identifier = "${baseIdentifier}_hourly"
        
        // Every hour at the specific minute - use minute component with repeat=true
        val components = NSDateComponents().apply {
            minute = habit.scheduledTimes.firstOrNull()?.minute?.toLong() ?: 0L
        }
        
        val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(
            components,
            repeats = true
        )
        
        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = identifier,
            content = content,
            trigger = trigger
        )
        
        center.addNotificationRequest(request) { error ->
            if (error != null) {
                Logger.e(Exception("Hourly notification scheduling failed: ${error.localizedDescription}"), "Failed to schedule hourly notification for habit: ${habit.name}", "IOSNotificationScheduler")
            } else {
                Logger.i("Successfully scheduled hourly notification for habit: ${habit.name} at minute ${habit.scheduledTimes.firstOrNull()?.minute ?: 0}", "IOSNotificationScheduler")
            }
        }
    }
    
    private fun createIntervalTriggers(habit: Habit, baseIdentifier: String, content: UNMutableNotificationContent) {
        val intervalMinutes = habit.intervalMinutes
        Logger.d("Creating interval triggers for ${intervalMinutes}-minute intervals", "IOSNotificationScheduler")
        
        // Calculate all minute marks within an hour where notifications should fire
        val notificationMinutes = mutableSetOf<Int>()
        
        // For each scheduled time, add intervals starting from that minute
        habit.scheduledTimes.forEach { scheduledTime ->
            val startMinute = scheduledTime.minute
            
            // Add all intervals within the hour, wrapping around
            var currentMinute = startMinute
            repeat(60 / intervalMinutes) {
                notificationMinutes.add(currentMinute)
                currentMinute = (currentMinute + intervalMinutes) % 60
            }
        }
        
        Logger.d("Scheduling notifications at minutes: ${notificationMinutes.sorted()}", "IOSNotificationScheduler")
        
        // Create a notification for each calculated minute
        notificationMinutes.forEach { minute ->
            val identifier = "${baseIdentifier}_interval_$minute"
            
            val components = NSDateComponents().apply {
                this.minute = minute.toLong()
            }
            
            val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(
                components,
                repeats = true
            )
            
            val request = UNNotificationRequest.requestWithIdentifier(
                identifier = identifier,
                content = content,
                trigger = trigger
            )
            
            center.addNotificationRequest(request) { error ->
                if (error != null) {
                    Logger.e(Exception("Interval notification scheduling failed: ${error.localizedDescription}"), "Failed to schedule interval notification for habit: ${habit.name} at minute $minute", "IOSNotificationScheduler")
                } else {
                    Logger.d("Successfully scheduled interval notification for habit: ${habit.name} at minute $minute", "IOSNotificationScheduler")
                }
            }
        }
    }

    override suspend fun scheduleTaskNotifications(tasks: List<Task>) {
        Logger.d("Scheduling ${tasks.size} task notifications", "IOSNotificationScheduler")
        
        // Group tasks by habit to avoid duplicate scheduling
        val habitIds = tasks.map { it.habitId }.distinct()
        
        habitIds.forEach { habitId ->
            val habit = habitRepository.getHabit(habitId)
            if (habit != null) {
                scheduleHabitNotification(habit)
            } else {
                Logger.w("Habit not found for habitId: $habitId", "IOSNotificationScheduler")
            }
        }
    }

    override suspend fun cancelTaskNotification(task: Task) {
        // Cancel all notifications for this habit
        cancelHabitNotifications(task.habitId)
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
}