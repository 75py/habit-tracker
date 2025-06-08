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
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toJavaLocalDateTime
import java.time.ZoneId

/**
 * Android implementation of NotificationScheduler using AlarmManager and NotificationManager.
 */
class AndroidNotificationScheduler(
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

    private val alarmManager: AlarmManager by lazy {
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    init {
        createNotificationChannel()
    }

    override suspend fun scheduleTaskNotification(task: Task) {
        if (!areNotificationsEnabled()) {
            return
        }

        val notificationId = generateNotificationId(task)
        val triggerTime = calculateTriggerTime(task)

        // Fetch the actual habit to get current name and description
        val habit = habitRepository.getHabit(task.habitId)
        val habitName = habit?.name ?: task.habitName
        val habitDescription = habit?.description ?: task.habitDescription

        // Create the notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(habitName)
            .setContentText(habitDescription.ifEmpty { "Time to complete your habit!" })
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Using system icon as fallback
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .addAction(createCompleteAction(task))
            .build()

        // Create pending intent for the notification
        val notificationIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = COMPLETE_ACTION
            putExtra("habitId", task.habitId)
            putExtra("date", task.date.toString())
            putExtra("scheduledTime", task.scheduledTime.toString())
            putExtra("notificationId", notificationId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Schedule the alarm
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }

    override suspend fun scheduleTaskNotifications(tasks: List<Task>) {
        tasks.forEach { task ->
            scheduleTaskNotification(task)
        }
    }

    override suspend fun cancelTaskNotification(task: Task) {
        val notificationId = generateNotificationId(task)
        
        // Cancel the scheduled alarm
        val intent = Intent(context, NotificationActionReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        
        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
        }
        
        // Cancel the notification if it's currently showing
        notificationManager.cancel(notificationId)
    }

    override suspend fun cancelHabitNotifications(habitId: Long) {
        // This is a simplified implementation
        // In a production app, you might want to store scheduled notification IDs
        // and cancel them individually
        cancelAllNotifications()
    }

    override suspend fun cancelAllNotifications() {
        notificationManager.cancelAll()
    }

    override suspend fun areNotificationsEnabled(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
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

    private fun createCompleteAction(task: Task): NotificationCompat.Action {
        val intent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = COMPLETE_ACTION
            putExtra("habitId", task.habitId)
            putExtra("date", task.date.toString())
            putExtra("scheduledTime", task.scheduledTime.toString())
            putExtra("notificationId", generateNotificationId(task))
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            generateNotificationId(task) + 1000, // Different ID for action
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_save, // Using system icon as fallback
            "Complete",
            pendingIntent
        ).build()
    }

    private fun calculateTriggerTime(task: Task): Long {
        val localDateTime = LocalDateTime(task.date, task.scheduledTime)
        return localDateTime.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
    }

    private fun generateNotificationId(task: Task): Int {
        // Generate a unique ID based on habit ID, date, and time
        // This ensures each task has a unique notification ID
        return "${task.habitId}_${task.date}_${task.scheduledTime}".hashCode()
    }
}