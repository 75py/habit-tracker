package com.nagopy.kmp.habittracker.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.nagopy.kmp.habittracker.domain.usecase.CompleteTaskFromNotificationUseCase
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
        if (intent.action == COMPLETE_ACTION) {
            handleCompleteAction(context, intent)
        }
    }

    private fun handleCompleteAction(context: Context, intent: Intent) {
        val habitId = intent.getLongExtra("habitId", -1L)
        val dateString = intent.getStringExtra("date")
        val timeString = intent.getStringExtra("scheduledTime")
        val notificationId = intent.getIntExtra("notificationId", -1)

        if (habitId == -1L || dateString.isNullOrEmpty() || timeString.isNullOrEmpty()) {
            return
        }

        try {
            val date = LocalDate.parse(dateString)
            val time = LocalTime.parse(timeString)

            // Use coroutine to handle the async operation
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    completeTaskFromNotificationUseCase(habitId, date, time)
                    
                    // Cancel the notification
                    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) 
                        as android.app.NotificationManager
                    notificationManager.cancel(notificationId)
                } catch (e: Exception) {
                    // In a production app, you might want to log this error
                    // or show a toast to the user
                }
            }
        } catch (e: Exception) {
            // Handle parsing errors
        }
    }
}