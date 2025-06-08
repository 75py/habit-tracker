package com.nagopy.kmp.habittracker.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.nagopy.kmp.habittracker.domain.usecase.CompleteTaskFromNotificationUseCase
import com.nagopy.kmp.habittracker.util.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * BroadcastReceiver to handle notification actions, particularly the "Complete" action.
 */
class NotificationActionReceiver : BroadcastReceiver(), KoinComponent {

    private val completeTaskFromNotificationUseCase: CompleteTaskFromNotificationUseCase by inject()

    companion object {
        private const val COMPLETE_ACTION = "com.nagopy.kmp.habittracker.COMPLETE_TASK"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Logger.d("NotificationActionReceiver received intent with action: ${intent.action}", "NotificationActionReceiver")
        
        if (intent.action == COMPLETE_ACTION) {
            handleCompleteAction(context, intent)
        } else {
            Logger.w("Unknown action received: ${intent.action}", "NotificationActionReceiver")
        }
    }

    private fun handleCompleteAction(context: Context, intent: Intent) {
        val habitId = intent.getLongExtra("habitId", -1L)
        val dateString = intent.getStringExtra("date")
        val timeString = intent.getStringExtra("scheduledTime")
        val notificationId = intent.getIntExtra("notificationId", -1)

        Logger.d("Handling complete action for habitId: $habitId, date: $dateString, time: $timeString", "NotificationActionReceiver")

        if (habitId == -1L || dateString.isNullOrEmpty() || timeString.isNullOrEmpty()) {
            Logger.w("Invalid data in complete action intent: habitId=$habitId, date=$dateString, time=$timeString", "NotificationActionReceiver")
            return
        }

        try {
            val date = LocalDate.parse(dateString)
            val time = LocalTime.parse(timeString)

            // Use coroutine to handle the async operation
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    completeTaskFromNotificationUseCase(habitId, date, time)
                    Logger.i("Successfully completed task from notification: habitId=$habitId, date=$date, time=$time", "NotificationActionReceiver")
                    
                    // Cancel the notification
                    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) 
                        as android.app.NotificationManager
                    notificationManager.cancel(notificationId)
                    Logger.d("Cancelled notification with ID: $notificationId", "NotificationActionReceiver")
                } catch (e: Exception) {
                    Logger.e(e, "Failed to complete task from notification: habitId=$habitId", "NotificationActionReceiver")
                }
            }
        } catch (e: Exception) {
            Logger.e(e, "Failed to parse date/time from notification intent: date=$dateString, time=$timeString", "NotificationActionReceiver")
        }
    }
}