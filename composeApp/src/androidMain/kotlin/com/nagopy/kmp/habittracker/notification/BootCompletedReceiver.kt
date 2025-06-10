package com.nagopy.kmp.habittracker.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.nagopy.kmp.habittracker.domain.usecase.ScheduleNextNotificationUseCase
import com.nagopy.kmp.habittracker.util.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * BroadcastReceiver to handle device boot completion.
 * When the device restarts, all AlarmManager alarms are lost, so we need to
 * reschedule notifications for all active habits to maintain the notification chain.
 */
class BootCompletedReceiver : BroadcastReceiver(), KoinComponent {

    private val scheduleNextNotificationUseCase: ScheduleNextNotificationUseCase by inject()

    override fun onReceive(context: Context, intent: Intent) {
        Logger.d("BootCompletedReceiver received intent with action: ${intent.action}", "BootCompletedReceiver")
        
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Logger.i("Device boot completed, rescheduling notifications", "BootCompletedReceiver")
            handleBootCompleted()
        } else {
            Logger.w("Unknown action received: ${intent.action}", "BootCompletedReceiver")
        }
    }

    private fun handleBootCompleted() {
        // Use coroutine to handle the async operation
        CoroutineScope(Dispatchers.IO).launch {
            try {
                scheduleNextNotificationUseCase.rescheduleAllHabitNotifications()
                Logger.i("Successfully rescheduled notifications after boot", "BootCompletedReceiver")
            } catch (e: Exception) {
                Logger.e(e, "Failed to reschedule notifications after boot", "BootCompletedReceiver")
            }
        }
    }
}