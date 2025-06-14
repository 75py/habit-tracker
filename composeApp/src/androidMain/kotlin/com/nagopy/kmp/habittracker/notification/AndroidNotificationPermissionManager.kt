package com.nagopy.kmp.habittracker.notification

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.nagopy.kmp.habittracker.domain.notification.NotificationPermissionManager
import com.nagopy.kmp.habittracker.util.Logger

/**
 * Android implementation of NotificationPermissionManager.
 * Handles notification permission requests using Activity-based permission flow.
 */
class AndroidNotificationPermissionManager(
    private val context: Context
) : NotificationPermissionManager {
    
    private var activityRef: Activity? = null
    
    /**
     * Set the current activity reference for permission requests.
     * This should be called when the activity is created or resumed.
     */
    fun setActivity(activity: Activity) {
        activityRef = activity
    }
    
    /**
     * Clear the activity reference when activity is destroyed.
     */
    fun clearActivity() {
        activityRef = null
    }
    
    override suspend fun areNotificationsEnabled(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }
    
    override suspend fun requestNotificationPermission(): Boolean {
        // On Android 13+, we need to request POST_NOTIFICATIONS permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val activity = activityRef
            if (activity == null) {
                // No activity available, return current status
                return areNotificationsEnabled()
            }
            
            // Check if permission is already granted
            if (areNotificationsEnabled()) {
                return true
            }
            
            // Request permission and return current status
            // This avoids the deprecated onRequestPermissionsResult callback
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                NOTIFICATION_PERMISSION_REQUEST_CODE
            )
            
            // Return current status - the permission dialog will appear
            // but we don't wait for the result
            return areNotificationsEnabled()
        } else {
            // For Android < 13, notifications are enabled by default
            return NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }
    
    override suspend fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= 31) { // API 31 = Android 12
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else {
            // For Android < 12, exact alarms don't require special permission
            true
        }
    }
    
    override suspend fun requestExactAlarmPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= 31) { // API 31 = Android 12
            if (canScheduleExactAlarms()) {
                return true
            }
            
            try {
                val activity = activityRef
                if (activity == null) {
                    Logger.w("No activity available for exact alarm permission request", "AndroidNotificationPermissionManager")
                    return canScheduleExactAlarms()
                }
                
                Logger.d("Requesting exact alarm permission for SDK 31+", "AndroidNotificationPermissionManager")
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                activity.startActivity(intent)
                
                // Return current status - the settings screen will appear
                return canScheduleExactAlarms()
            } catch (e: Exception) {
                Logger.e(e, "Failed to request exact alarm permission", "AndroidNotificationPermissionManager")
                return canScheduleExactAlarms()
            }
        } else {
            // For Android < 12, exact alarms don't require special permission
            true
        }
    }
    
    companion object {
        const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001
    }
}