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
            }
        }
    }
    
    private fun scheduleNotificationRequest(
        identifier: String,
        content: UNMutableNotificationContent,
        trigger: UNCalendarNotificationTrigger,
        habit: Habit,
        description: String
    ) {
        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = identifier,
            content = content,
            trigger = trigger
        )
        
        center.addNotificationRequest(request) { error ->
            if (error != null) {
                Logger.e(Exception("Notification scheduling failed: ${error.localizedDescription}"), "Failed to schedule $description for habit: ${habit.name}", "IOSNotificationScheduler")
            } else {
                Logger.d("Successfully scheduled $description for habit: ${habit.name}", "IOSNotificationScheduler")
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
            
            scheduleNotificationRequest(
                identifier = identifier,
                content = content,
                trigger = trigger,
                habit = habit,
                description = "daily notification at ${scheduledTime}"
            )
        }
    }
    
    private fun createHourlyTriggers(habit: Habit, baseIdentifier: String, content: UNMutableNotificationContent) {
        val startTime = habit.scheduledTimes.firstOrNull() ?: return
        val endTime = habit.endTime ?: LocalTime(23, 59)
        val minute = startTime.minute
        
        Logger.d("Creating hourly triggers from ${startTime.hour}:${minute} to ${endTime.hour}:${endTime.minute}", "IOSNotificationScheduler")
        
        // Create individual triggers for each hour from start to end time
        var currentHour = startTime.hour
        var triggerIndex = 0
        
        while (true) {
            val currentTime = LocalTime(currentHour, minute)
            if (currentTime > endTime) break
            
            val identifier = "${baseIdentifier}_hourly_$triggerIndex"
            
            val components = NSDateComponents().apply {
                hour = currentHour.toLong()
                this.minute = minute.toLong()
            }
            
            val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(
                components,
                repeats = true
            )
            
            scheduleNotificationRequest(
                identifier = identifier,
                content = content,
                trigger = trigger,
                habit = habit,
                description = "hourly notification at $currentHour:${minute.toString().padStart(2, '0')}"
            )
            
            currentHour++
            if (currentHour > 23) currentHour = 0 // Wrap to next day
            triggerIndex++
            
            // Prevent infinite loop - max 24 hours
            if (triggerIndex >= 24) break
        }
    }
    
    private fun createIntervalTriggers(habit: Habit, baseIdentifier: String, content: UNMutableNotificationContent) {
        val intervalMinutes = habit.intervalMinutes
        val endTime = habit.endTime ?: LocalTime(23, 59)
        
        Logger.d("Creating interval triggers for ${intervalMinutes}-minute intervals until ${endTime}", "IOSNotificationScheduler")
        
        // Calculate all specific times when notifications should fire
        val notificationTimes = mutableSetOf<LocalTime>()
        
        habit.scheduledTimes.forEach { startTime ->
            if (startTime <= endTime) {
                var currentTime = startTime
                
                // Add notifications from start time until end time
                while (currentTime <= endTime) {
                    notificationTimes.add(currentTime)
                    
                    // Calculate next notification time
                    val totalMinutes = currentTime.hour * 60 + currentTime.minute + intervalMinutes
                    val newHour = (totalMinutes / 60) % 24
                    val newMinute = totalMinutes % 60
                    currentTime = LocalTime(newHour, newMinute)
                    
                    // If we've wrapped to the next day, stop
                    if (totalMinutes >= 24 * 60) break
                }
            }
        }
        
        Logger.d("Scheduling notifications at times: ${notificationTimes.sortedBy { it.hour * 60 + it.minute }}", "IOSNotificationScheduler")
        
        // Create a notification for each calculated time
        notificationTimes.forEachIndexed { index, time ->
            val identifier = "${baseIdentifier}_interval_${time.hour}_${time.minute}"
            
            val components = NSDateComponents().apply {
                hour = time.hour.toLong()
                minute = time.minute.toLong()
            }
            
            val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(
                components,
                repeats = true
            )
            
            scheduleNotificationRequest(
                identifier = identifier,
                content = content,
                trigger = trigger,
                habit = habit,
                description = "interval notification at ${time.hour}:${time.minute.toString().padStart(2, '0')}"
            )
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