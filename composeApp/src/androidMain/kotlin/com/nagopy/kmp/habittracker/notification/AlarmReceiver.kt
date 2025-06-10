package com.nagopy.kmp.habittracker.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.nagopy.kmp.habittracker.domain.repository.HabitRepository
import com.nagopy.kmp.habittracker.domain.usecase.ScheduleNextNotificationUseCase
import com.nagopy.kmp.habittracker.util.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.format.DateTimeParseException
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * BroadcastReceiver to handle alarm broadcasts and display notifications.
 * This receiver is triggered by AlarmManager at the scheduled time and displays
 * a notification with action buttons for user interaction.
 */
class AlarmReceiver : BroadcastReceiver(), KoinComponent {

    private val habitRepository: HabitRepository by inject()
    private val scheduleNextNotificationUseCase: ScheduleNextNotificationUseCase by inject()

    companion object {
        const val SHOW_NOTIFICATION_ACTION = "com.nagopy.kmp.habittracker.SHOW_NOTIFICATION"
        private const val COMPLETE_ACTION = "com.nagopy.kmp.habittracker.COMPLETE_TASK"
        private const val CHANNEL_ID = "habit_tracker_notifications"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Logger.d("AlarmReceiver received intent with action: ${intent.action}", "AlarmReceiver")
        
        if (intent.action == SHOW_NOTIFICATION_ACTION) {
            handleShowNotification(context, intent)
        } else {
            Logger.w("Unknown action received: ${intent.action}", "AlarmReceiver")
        }
    }

    private fun handleShowNotification(context: Context, intent: Intent) {
        val habitId = intent.getLongExtra("habitId", -1L)
        val dateString = intent.getStringExtra("date")
        val timeString = intent.getStringExtra("scheduledTime")
        val notificationId = intent.getIntExtra("notificationId", -1)

        Logger.d("Showing notification for habitId: $habitId, date: $dateString, time: $timeString", "AlarmReceiver")

        if (habitId == -1L || dateString.isNullOrEmpty() || timeString.isNullOrEmpty() || notificationId == -1) {
            Logger.w("Invalid data in show notification intent: habitId=$habitId, date=$dateString, time=$timeString, notificationId=$notificationId", "AlarmReceiver")
            return
        }

        try {
            val date = LocalDate.parse(dateString)
            val time = LocalTime.parse(timeString)

            // Use coroutine to fetch habit data asynchronously
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Fetch the actual habit to get current name and description
                    val habit = habitRepository.getHabit(habitId)
                    val habitName = habit?.name ?: "Habit Reminder"
                    val habitDescription = habit?.description ?: "Time to complete your habit!"

                    Logger.d("Using habit name: '$habitName', description: '$habitDescription'", "AlarmReceiver")

                    // Create complete action intent
                    val completeActionIntent = Intent(context, NotificationActionReceiver::class.java).apply {
                        action = COMPLETE_ACTION
                        putExtra("habitId", habitId)
                        putExtra("date", dateString)
                        putExtra("scheduledTime", timeString)
                        putExtra("notificationId", notificationId)
                    }

                    val completeActionPendingIntent = PendingIntent.getBroadcast(
                        context,
                        notificationId + 1000, // Different ID for action
                        completeActionIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    val completeAction = NotificationCompat.Action.Builder(
                        android.R.drawable.ic_menu_save, // Using system icon as fallback
                        "Complete",
                        completeActionPendingIntent
                    ).build()

                    // Build the notification
                    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                        .setContentTitle(habitName)
                        .setContentText(habitDescription.ifEmpty { "Time to complete your habit!" })
                        .setSmallIcon(android.R.drawable.ic_dialog_info) // Using system icon as fallback
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true)
                        .addAction(completeAction)
                        .build()

                    // Display the notification
                    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) 
                        as NotificationManager
                    notificationManager.notify(notificationId, notification)
                    
                    Logger.i("Successfully displayed notification for habitId: $habitId, notificationId: $notificationId", "AlarmReceiver")
                    
                    // Schedule the next notification for this habit
                    // This is critical for maintaining the notification chain, but should not fail the current notification
                    try {
                        val wasScheduled = scheduleNextNotificationUseCase.scheduleNextNotificationForHabit(habitId)
                        if (wasScheduled) {
                            Logger.i("Successfully scheduled next notification for habitId: $habitId", "AlarmReceiver")
                        } else {
                            Logger.d("No next notification to schedule for habitId: $habitId", "AlarmReceiver")
                        }
                    } catch (e: Exception) {
                        // Log and continue - failing to schedule next notification shouldn't affect current notification
                        Logger.e(e, "Failed to schedule next notification for habitId: $habitId", "AlarmReceiver")
                    }
                } catch (e: Exception) {
                    // This catches database exceptions, notification system failures, and other unexpected errors
                    Logger.e(e, "Failed to display notification for habitId: $habitId", "AlarmReceiver")
                }
            }
        } catch (e: DateTimeParseException) {
            Logger.e(e, "Invalid date/time format in alarm intent: date=$dateString, time=$timeString", "AlarmReceiver")
        } catch (e: Exception) {
            Logger.e(e, "Unexpected error processing alarm intent for habitId: $habitId", "AlarmReceiver")
        }
    }
}