package com.nagopy.kmp.habittracker.notification

import com.nagopy.kmp.habittracker.domain.model.Task
import com.nagopy.kmp.habittracker.domain.notification.NotificationScheduler
import com.nagopy.kmp.habittracker.domain.repository.HabitRepository
import com.nagopy.kmp.habittracker.domain.usecase.CompleteTaskFromNotificationUseCase
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toNSDate

import platform.UserNotifications.*
import platform.Foundation.*

/**
 * iOS implementation of NotificationScheduler using UserNotifications framework.
 */
@OptIn(ExperimentalForeignApi::class)
class IOSNotificationScheduler(
    private val habitRepository: HabitRepository,
    private val completeTaskFromNotificationUseCase: CompleteTaskFromNotificationUseCase
) : NotificationScheduler {

    companion object {
        private const val HABIT_REMINDER_CATEGORY = "HABIT_REMINDER"
        private const val COMPLETE_ACTION = "COMPLETE_ACTION"
    }

    private val center = UNUserNotificationCenter.currentNotificationCenter()

    override suspend fun scheduleTaskNotification(task: Task) {
        if (!areNotificationsEnabled()) {
            return
        }

        val identifier = generateNotificationId(task)
        
        // Fetch the actual habit to get current name and description
        val habit = habitRepository.getHabit(task.habitId)
        val habitName = habit?.name ?: task.habitName
        val habitDescription = habit?.description ?: task.habitDescription
        
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
        
        // Create calendar trigger
        val calendar = NSCalendar.currentCalendar()
        val components = calendar.components(
            NSCalendarUnitYear or NSCalendarUnitMonth or NSCalendarUnitDay or 
            NSCalendarUnitHour or NSCalendarUnitMinute,
            triggerDate
        )
        
        val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(
            components,
            repeats = false
        )

        // Create request
        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = identifier,
            content = content,
            trigger = trigger
        )

        // Schedule notification
        center.addNotificationRequest(request) { error ->
            if (error != null) {
                // Handle error - in production you might want to log this
            }
        }
    }

    override suspend fun scheduleTaskNotifications(tasks: List<Task>) {
        tasks.forEach { task ->
            scheduleTaskNotification(task)
        }
    }

    override suspend fun cancelTaskNotification(task: Task) {
        val identifier = generateNotificationId(task)
        center.removePendingNotificationRequestsWithIdentifiers(listOf(identifier))
        center.removeDeliveredNotificationsWithIdentifiers(listOf(identifier))
    }

    override suspend fun cancelHabitNotifications(habitId: Long) {
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
            
            if (identifiersToCancel.isNotEmpty()) {
                center.removePendingNotificationRequestsWithIdentifiers(identifiersToCancel)
                center.removeDeliveredNotificationsWithIdentifiers(identifiersToCancel)
            }
        }
    }

    override suspend fun cancelAllNotifications() {
        center.removeAllPendingNotificationRequests()
        center.removeAllDeliveredNotifications()
    }

    override suspend fun areNotificationsEnabled(): Boolean {
        return kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
            center.getNotificationSettingsWithCompletionHandler { settings ->
                val isEnabled = settings?.authorizationStatus == UNAuthorizationStatusAuthorized
                continuation.resumeWith(Result.success(isEnabled))
            }
        }
    }

    override suspend fun requestNotificationPermission(): Boolean {
        return kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
            center.requestAuthorizationWithOptions(
                UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge
            ) { granted, _ ->
                continuation.resumeWith(Result.success(granted))
            }
        }
    }

    private fun generateNotificationId(task: Task): String {
        return "${task.habitId}_${task.date}_${task.scheduledTime}"
    }

    fun setupNotificationCategories() {
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
    }

    fun handleNotificationResponse(response: UNNotificationResponse) {
        if (response.actionIdentifier == COMPLETE_ACTION) {
            val identifier = response.notification.request.identifier
            val parts = identifier.split("_")
            
            if (parts.size >= 3) {
                try {
                    val habitId = parts[0].toLong()
                    val date = kotlinx.datetime.LocalDate.parse(parts[1])
                    val time = kotlinx.datetime.LocalTime.parse(parts[2])
                    
                    // Handle completion in background
                    CoroutineScope(Dispatchers.Default).launch {
                        try {
                            completeTaskFromNotificationUseCase(habitId, date, time)
                        } catch (e: Exception) {
                            // Handle error
                        }
                    }
                } catch (e: Exception) {
                    // Handle parsing error
                }
            }
        }
    }
}