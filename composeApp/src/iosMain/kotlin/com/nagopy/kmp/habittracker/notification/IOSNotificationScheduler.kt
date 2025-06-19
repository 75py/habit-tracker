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

        // Fetch the actual habit to get current name and description
        val habit = habitRepository.getHabit(task.habitId)
        if (habit == null) {
            Logger.w("Habit not found for task: ${task.habitName} (ID: ${task.habitId}), skipping notification", "IOSNotificationScheduler")
            return
        }
        
        // For interval habits that divide evenly into an hour, use interval-based scheduling
        // This ensures proper coverage of all notification times (e.g., 0, 15, 30, 45 for 15-min intervals)
        if (habit.frequencyType == FrequencyType.INTERVAL && 
            habit.intervalMinutes <= 60 && 
            60 % habit.intervalMinutes == 0) {
            
            // Check if interval notifications are already scheduled for this habit
            val existingIdentifier = "${habit.id}_interval_0"
            
            center.getPendingNotificationRequestsWithCompletionHandler { requests ->
                @Suppress("UNCHECKED_CAST")
                val typedRequests = requests as? List<UNNotificationRequest>
                val hasExistingIntervalNotifications = typedRequests?.any { request ->
                    request.identifier == existingIdentifier
                } == true
                
                if (hasExistingIntervalNotifications) {
                    Logger.d("Interval notifications already scheduled for habit: ${habit.name}, skipping task-specific notification", "IOSNotificationScheduler")
                } else {
                    // Schedule all interval notifications for this habit
                    scheduleIntervalNotifications(habit)
                }
            }
        } else {
            // For other frequency types, schedule individual task notifications
            scheduleIndividualTaskNotification(task, habit)
        }
    }
    
    private suspend fun scheduleIndividualTaskNotification(task: Task, habit: Habit) {
        val identifier = generateNotificationId(task)
        Logger.d("Generated notification ID: $identifier", "IOSNotificationScheduler")
        
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
    
    private fun scheduleIntervalNotifications(habit: Habit) {
        val intervalMinutes = habit.intervalMinutes
        
        // Only support intervals that divide evenly into 60 minutes for now
        if (60 % intervalMinutes != 0) {
            Logger.w("Interval ${intervalMinutes} minutes does not divide evenly into 60 minutes. Using fallback single notification.", "IOSNotificationScheduler")
            // For non-divisible intervals, create a single repeating notification at the first scheduled time
            if (habit.scheduledTimes.isNotEmpty()) {
                val scheduledTime = habit.scheduledTimes.first()
                
                // Create notification content
                val content = UNMutableNotificationContent().apply {
                    setTitle(habit.name)
                    setBody(habit.description.ifEmpty { "Time to complete your habit!" })
                    setSound(UNNotificationSound.defaultSound())
                    setCategoryIdentifier(HABIT_REMINDER_CATEGORY)
                }
                
                // Create trigger that repeats based on the scheduled time
                val components = NSDateComponents().apply {
                    hour = scheduledTime.hour.toLong()
                    minute = scheduledTime.minute.toLong()
                }
                
                val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(
                    components,
                    repeats = true
                )
                
                val request = UNNotificationRequest.requestWithIdentifier(
                    identifier = "${habit.id}_interval_fallback",
                    content = content,
                    trigger = trigger
                )
                
                center.addNotificationRequest(request) { error ->
                    if (error != null) {
                        Logger.e(Exception("Fallback interval notification scheduling failed: ${error.localizedDescription}"), "Failed to schedule fallback notification for habit: ${habit.name}", "IOSNotificationScheduler")
                    } else {
                        Logger.d("Successfully scheduled fallback interval notification for habit: ${habit.name} at ${scheduledTime}", "IOSNotificationScheduler")
                    }
                }
            }
            return
        }
        
        val slotsPerHour = 60 / intervalMinutes
        
        // For each scheduled time, create interval notifications starting from that time
        habit.scheduledTimes.forEach { scheduledTime ->
            Logger.d("Scheduling $slotsPerHour interval notifications for ${intervalMinutes}-minute interval habit: ${habit.name} starting at ${scheduledTime}", "IOSNotificationScheduler")
            
            // Create notification content
            val content = UNMutableNotificationContent().apply {
                setTitle(habit.name)
                setBody(habit.description.ifEmpty { "Time to complete your habit!" })
                setSound(UNNotificationSound.defaultSound())
                setCategoryIdentifier(HABIT_REMINDER_CATEGORY)
            }
            
            // Calculate the minutes within each hour where notifications should fire
            val startMinute = scheduledTime.minute
            val notificationMinutes = mutableSetOf<Int>()
            
            // Add intervals starting from the scheduled minute
            for (slot in 0 until slotsPerHour) {
                val minuteInHour = (startMinute + slot * intervalMinutes) % 60
                notificationMinutes.add(minuteInHour)
            }
            
            // Create notifications for each calculated minute
            notificationMinutes.forEach { minute ->
                val identifier = "${habit.id}_interval_${minute}"
                
                // Create trigger for this specific minute of every hour
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
                
                // Schedule this interval notification
                center.addNotificationRequest(request) { error ->
                    if (error != null) {
                        Logger.e(Exception("Interval notification scheduling failed: ${error.localizedDescription}"), "Failed to schedule interval notification for habit: ${habit.name} at minute $minute", "IOSNotificationScheduler")
                    } else {
                        Logger.d("Successfully scheduled interval notification for habit: ${habit.name} at minute $minute", "IOSNotificationScheduler")
                    }
                }
            }
        }
        
        Logger.i("Successfully scheduled interval notifications for habit: ${habit.name}", "IOSNotificationScheduler")
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
            
            Logger.d("Processing notification response for identifier: $identifier", "IOSNotificationScheduler")
            
            if (parts.size >= 2) {
                try {
                    val habitId = parts[0].toLong()
                    
                    // Check if this is an interval notification (format: habitId_interval_minute)
                    if (parts.size >= 3 && parts[1] == "interval") {
                        // For interval notifications, we complete the task for the current time
                        val currentDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                        val date = currentDateTime.date
                        val time = currentDateTime.time
                        
                        Logger.d("Processing interval notification response for habitId: $habitId at current time: $time", "IOSNotificationScheduler")
                        
                        // Handle completion in background
                        CoroutineScope(Dispatchers.Default).launch {
                            try {
                                completeTaskFromNotificationUseCase(habitId, date, time)
                                Logger.i("Successfully completed interval task for habitId: $habitId", "IOSNotificationScheduler")
                            } catch (e: Exception) {
                                Logger.e(e, "Failed to complete interval task for habitId: $habitId", "IOSNotificationScheduler")
                            }
                        }
                    } else {
                        // Original format: habitId_date or habitId_date_time
                        val date = kotlinx.datetime.LocalDate.parse(parts[1])
                        // Use default time since we're now using habit-level notifications
                        val time = kotlinx.datetime.LocalTime(9, 0)
                        
                        Logger.d("Processing standard notification response for habitId: $habitId, date: $date", "IOSNotificationScheduler")
                        
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
                // For interval habits, create a fallback trigger (though this should not be reached with new logic)
                Logger.w("createTriggerForTask called for interval habit ${habit.name} - this should be handled by scheduleIntervalNotifications", "IOSNotificationScheduler")
                val components = calendar.components(NSCalendarUnitHour or NSCalendarUnitMinute, triggerDate)
                UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(
                    components,
                    repeats = true
                )
            }
        }
}