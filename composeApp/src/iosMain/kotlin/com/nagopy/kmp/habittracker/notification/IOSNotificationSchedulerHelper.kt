package com.nagopy.kmp.habittracker.notification

import com.nagopy.kmp.habittracker.di.KoinHelper
import com.nagopy.kmp.habittracker.util.Logger
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import platform.UserNotifications.UNNotificationResponse

@OptIn(ExperimentalForeignApi::class)

/**
 * Helper functions to bridge Swift NotificationDelegate with Kotlin IOSNotificationScheduler
 */

/**
 * Handle notification response from Swift delegate
 */
fun handleNotificationResponseFromSwift(response: UNNotificationResponse) {
    Logger.d("Handling notification response from Swift delegate", "IOSNotificationSchedulerHelper")
    try {
        val scheduler = KoinHelper.get<IOSNotificationScheduler>()
        scheduler.handleNotificationResponse(response)
    } catch (e: Exception) {
        Logger.e(e, "Failed to handle notification response from Swift", "IOSNotificationSchedulerHelper")
    }
}

/**
 * Schedule next notification from notification delivery
 */
fun scheduleNextNotificationFromDelivery(identifier: String) {
    Logger.d("Scheduling next notification from delivery, identifier: $identifier", "IOSNotificationSchedulerHelper")
    
    val parts = identifier.split("_")
    if (parts.size >= 3) {
        try {
            val habitId = parts[0].toLong()
            
            // Schedule next notification for this habit in background
            CoroutineScope(Dispatchers.Default).launch {
                try {
                    val scheduleNextNotificationUseCase = KoinHelper.get<com.nagopy.kmp.habittracker.domain.usecase.ScheduleNextNotificationUseCase>()
                    
                    val wasScheduled = scheduleNextNotificationUseCase.scheduleNextNotificationForHabit(habitId)
                    if (wasScheduled) {
                        Logger.i("Successfully scheduled next notification for habitId: $habitId from delivery", "IOSNotificationSchedulerHelper")
                    } else {
                        Logger.d("No next notification to schedule for habitId: $habitId from delivery", "IOSNotificationSchedulerHelper")
                    }
                } catch (e: Exception) {
                    Logger.e(e, "Failed to schedule next notification for habitId: $habitId from delivery", "IOSNotificationSchedulerHelper")
                }
            }
        } catch (e: NumberFormatException) {
            Logger.e(e, "Invalid habitId format in notification identifier: $identifier", "IOSNotificationSchedulerHelper")
        } catch (e: Exception) {
            Logger.e(e, "Unexpected error processing notification delivery: $identifier", "IOSNotificationSchedulerHelper")
        }
    } else {
        Logger.w("Invalid notification identifier format (expected at least 3 parts): $identifier", "IOSNotificationSchedulerHelper")
    }
}

/**
 * Setup notification categories - called from Swift app initialization
 */
fun setupNotificationCategories() {
    Logger.d("Setting up notification categories from Swift", "IOSNotificationSchedulerHelper")
    try {
        val scheduler = KoinHelper.get<IOSNotificationScheduler>()
        scheduler.setupNotificationCategories()
    } catch (e: Exception) {
        Logger.e(e, "Failed to setup notification categories from Swift", "IOSNotificationSchedulerHelper")
    }
}