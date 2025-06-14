package com.nagopy.kmp.habittracker.notification

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.nagopy.kmp.habittracker.domain.model.Task
import com.nagopy.kmp.habittracker.domain.notification.NotificationScheduler
import com.nagopy.kmp.habittracker.domain.repository.HabitRepository
import com.nagopy.kmp.habittracker.util.Logger
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toJavaLocalDateTime
import java.time.ZoneId

/**
 * Android implementation of NotificationScheduler using AlarmManager and NotificationManager.
 */
open class AndroidNotificationScheduler(
    private val context: Context,
    private val habitRepository: HabitRepository
) : NotificationScheduler {

    companion object {
        private const val CHANNEL_ID = "habit_tracker_notifications"
        private const val CHANNEL_NAME = "Habit Reminders"
        private const val CHANNEL_DESCRIPTION = "Notifications for habit reminders"
        private const val COMPLETE_ACTION = "com.nagopy.kmp.habittracker.COMPLETE_TASK"
    }

    private val notificationManager: NotificationManager by lazy {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    protected open val alarmManager: AlarmManager by lazy {
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    init {
        createNotificationChannel()
    }

    override suspend fun scheduleTaskNotification(task: Task) {
        Logger.d("Attempting to schedule notification for task: ${task.habitName} at ${task.scheduledTime}", "AndroidNotificationScheduler")
        
        if (!areNotificationsEnabled()) {
            Logger.w("Notifications are not enabled, skipping notification for task: ${task.habitName}", "AndroidNotificationScheduler")
            return
        }

        val notificationId = generateNotificationId(task)
        val triggerTime = calculateTriggerTime(task)
        
        Logger.d("Generated notification ID: $notificationId, trigger time: $triggerTime", "AndroidNotificationScheduler")

        // Create pending intent for the alarm to trigger AlarmReceiver
        val alarmIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.SHOW_NOTIFICATION_ACTION
            putExtra("habitId", task.habitId)
            putExtra("date", task.date.toString())
            putExtra("scheduledTime", task.scheduledTime.toString())
            putExtra("notificationId", notificationId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Schedule the alarm
        try {
            // Check if we can schedule exact alarms on Android 12+ (API 31+)
            if (Build.VERSION.SDK_INT >= 31 && !canScheduleExactAlarms()) {
                // Cannot schedule exact alarms - fall back to inexact scheduling
                Logger.w("Cannot schedule exact alarms, falling back to inexact scheduling for task: ${task.habitName}", "AndroidNotificationScheduler")
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                Logger.d("Scheduling exact alarm with setExactAndAllowWhileIdle for task: ${task.habitName}", "AndroidNotificationScheduler")
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
            Logger.i("Successfully scheduled notification for task: ${task.habitName} at ${task.scheduledTime}", "AndroidNotificationScheduler")
        } catch (e: SecurityException) {
            // SecurityException can occur if the app doesn't have permission to schedule exact alarms
            Logger.e(e, "SecurityException when scheduling exact alarm for task: ${task.habitName}, falling back to inexact scheduling", "AndroidNotificationScheduler")
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        } catch (e: Exception) {
            // Any other exception during alarm scheduling
            Logger.e(e, "Unexpected exception when scheduling alarm for task: ${task.habitName}", "AndroidNotificationScheduler")
        }
    }

    override suspend fun scheduleTaskNotifications(tasks: List<Task>) {
        tasks.forEach { task ->
            scheduleTaskNotification(task)
        }
    }

    override suspend fun cancelTaskNotification(task: Task) {
        Logger.d("Cancelling notification for task: ${task.habitName} at ${task.scheduledTime}", "AndroidNotificationScheduler")
        
        val notificationId = generateNotificationId(task)
        
        // Cancel the scheduled alarm
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.SHOW_NOTIFICATION_ACTION
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        
        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
            Logger.d("Cancelled scheduled alarm for task: ${task.habitName}", "AndroidNotificationScheduler")
        } ?: Logger.w("No pending intent found to cancel for task: ${task.habitName}", "AndroidNotificationScheduler")
        
        // Cancel the notification if it's currently showing
        notificationManager.cancel(notificationId)
        Logger.d("Cancelled notification display for task: ${task.habitName}", "AndroidNotificationScheduler")
    }

    override suspend fun cancelHabitNotifications(habitId: Long) {
        // This is a simplified implementation
        // In a production app, you might want to store scheduled notification IDs
        // and cancel them individually
        cancelAllNotifications()
    }

    override suspend fun cancelAllNotifications() {
        Logger.i("Cancelling all notifications", "AndroidNotificationScheduler")
        notificationManager.cancelAll()
    }

    /**
     * Get information about the current alarm scheduling capabilities.
     * This can be useful for debugging notification delivery issues.
     */
    fun getAlarmCapabilities(): String {
        val canScheduleExact = canScheduleExactAlarms()
        val notificationsEnabled = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
        
        return "Android API ${Build.VERSION.SDK_INT}: " +
               "Notifications enabled: $notificationsEnabled, " +
               "Can schedule exact alarms: $canScheduleExact"
    }

    override suspend fun areNotificationsEnabled(): Boolean {
        val basicNotificationsEnabled = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
        
        Logger.d("Notifications enabled check: basic=$basicNotificationsEnabled, canScheduleExact=${canScheduleExactAlarms()}", "AndroidNotificationScheduler")
        
        // Notifications are considered enabled if basic permissions are granted
        // Exact alarm permission is checked separately during scheduling
        return basicNotificationsEnabled
    }

    /**
     * Check if the app can schedule exact alarms.
     * This is used internally to determine the best alarm scheduling strategy.
     */
    private fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= 31) { // API 31 = Android 12
            alarmManager.canScheduleExactAlarms()
        } else {
            // On Android < 12, exact alarms don't require special permission
            true
        }
    }

    override suspend fun requestNotificationPermission(): Boolean {
        // This implementation cannot request permissions directly
        // Permission requests should be handled at the Activity level
        return areNotificationsEnabled()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESCRIPTION
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun calculateTriggerTime(task: Task): Long {
        val localDateTime = LocalDateTime(task.date, task.scheduledTime)
        return localDateTime.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
    }

    private fun generateNotificationId(task: Task): Int {
        // Generate an ID based on habit ID and date only
        // This ensures all tasks for the same habit on the same day share the same notification ID,
        // causing new notifications to replace existing ones instead of creating multiple notifications
        return "${task.habitId}_${task.date}".hashCode()
    }
}