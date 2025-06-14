package com.nagopy.kmp.habittracker.presentation.permission

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nagopy.kmp.habittracker.domain.notification.NotificationPermissionManager
import com.nagopy.kmp.habittracker.domain.storage.AppPreferences
import com.nagopy.kmp.habittracker.util.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing notification permission request flow.
 * Handles the step-by-step process of requesting notification and exact alarm permissions.
 */
class NotificationPermissionViewModel(
    private val notificationPermissionManager: NotificationPermissionManager,
    private val appPreferences: AppPreferences
) : ViewModel() {
    
    private val _permissionState = MutableStateFlow<NotificationPermissionState>(NotificationPermissionState.Initial)
    val permissionState: StateFlow<NotificationPermissionState> = _permissionState.asStateFlow()
    
    /**
     * Starts the permission request flow if not already requested.
     */
    fun startPermissionFlow() {
        viewModelScope.launch {
            try {
                // Check if we've already requested permission
                val alreadyRequested = appPreferences.getBoolean(
                    AppPreferences.KEY_NOTIFICATION_PERMISSION_REQUESTED, 
                    false
                )
                
                if (alreadyRequested) {
                    Logger.d("Notification permission already requested, skipping flow", "NotificationPermissionViewModel")
                    _permissionState.value = NotificationPermissionState.Completed
                    return@launch
                }
                
                // Check if notification permission is already granted
                if (notificationPermissionManager.areNotificationsEnabled()) {
                    Logger.d("Notification permission already granted", "NotificationPermissionViewModel")
                    markPermissionRequested()
                    
                    // Check if we need exact alarm permission
                    if (!notificationPermissionManager.canScheduleExactAlarms()) {
                        _permissionState.value = NotificationPermissionState.ShowExactAlarmExplanation
                    } else {
                        _permissionState.value = NotificationPermissionState.Completed
                    }
                } else {
                    // Start the permission flow with explanation
                    _permissionState.value = NotificationPermissionState.ShowNotificationExplanation
                }
                
            } catch (e: Exception) {
                Logger.e(e, "Failed to start permission flow", "NotificationPermissionViewModel")
                _permissionState.value = NotificationPermissionState.Completed
            }
        }
    }
    
    /**
     * User confirmed they want to proceed with notification permission request.
     */
    fun onNotificationExplanationConfirmed() {
        _permissionState.value = NotificationPermissionState.RequestingNotificationPermission
        requestNotificationPermission()
    }
    
    /**
     * User canceled the notification permission explanation.
     */
    fun onNotificationExplanationDismissed() {
        viewModelScope.launch {
            markPermissionRequested()
            _permissionState.value = NotificationPermissionState.Completed
        }
    }
    
    /**
     * User confirmed they want to proceed with exact alarm permission request.
     */
    fun onExactAlarmExplanationConfirmed() {
        _permissionState.value = NotificationPermissionState.RequestingExactAlarmPermission
        requestExactAlarmPermission()
    }
    
    /**
     * User skipped the exact alarm permission request.
     */
    fun onExactAlarmExplanationDismissed() {
        _permissionState.value = NotificationPermissionState.Completed
    }
    
    /**
     * User dismissed the notification permission denied dialog.
     */
    fun onNotificationPermissionDeniedDismissed() {
        _permissionState.value = NotificationPermissionState.Completed
    }
    
    /**
     * Called when user returns from notification permission system dialog.
     * This should be called from activity resume or similar lifecycle method.
     */
    fun onReturnFromNotificationPermissionRequest() {
        viewModelScope.launch {
            try {
                val isGranted = notificationPermissionManager.areNotificationsEnabled()
                Logger.d("Notification permission result: granted=$isGranted", "NotificationPermissionViewModel")
                
                markPermissionRequested()
                
                if (isGranted) {
                    // Check if we need exact alarm permission
                    if (!notificationPermissionManager.canScheduleExactAlarms()) {
                        _permissionState.value = NotificationPermissionState.ShowExactAlarmExplanation
                    } else {
                        _permissionState.value = NotificationPermissionState.Completed
                    }
                } else {
                    _permissionState.value = NotificationPermissionState.NotificationPermissionDenied
                }
            } catch (e: Exception) {
                Logger.e(e, "Failed to check notification permission result", "NotificationPermissionViewModel")
                _permissionState.value = NotificationPermissionState.NotificationPermissionDenied
            }
        }
    }
    
    /**
     * Called when user returns from exact alarm permission system dialog.
     * This should be called from activity resume or similar lifecycle method.
     */
    fun onReturnFromExactAlarmPermissionRequest() {
        viewModelScope.launch {
            try {
                val isGranted = notificationPermissionManager.canScheduleExactAlarms()
                Logger.d("Exact alarm permission result: granted=$isGranted", "NotificationPermissionViewModel")
                _permissionState.value = NotificationPermissionState.Completed
            } catch (e: Exception) {
                Logger.e(e, "Failed to check exact alarm permission result", "NotificationPermissionViewModel")
                _permissionState.value = NotificationPermissionState.Completed
            }
        }
    }
    
    private fun requestNotificationPermission() {
        viewModelScope.launch {
            try {
                notificationPermissionManager.requestNotificationPermission()
                // The actual result check will happen in onReturnFromNotificationPermissionRequest
            } catch (e: Exception) {
                Logger.e(e, "Failed to request notification permission", "NotificationPermissionViewModel")
                _permissionState.value = NotificationPermissionState.NotificationPermissionDenied
            }
        }
    }
    
    private fun requestExactAlarmPermission() {
        viewModelScope.launch {
            try {
                notificationPermissionManager.requestExactAlarmPermission()
                // The actual result check will happen in onReturnFromExactAlarmPermissionRequest
            } catch (e: Exception) {
                Logger.e(e, "Failed to request exact alarm permission", "NotificationPermissionViewModel")
                _permissionState.value = NotificationPermissionState.Completed
            }
        }
    }
    
    private suspend fun markPermissionRequested() {
        appPreferences.setBoolean(AppPreferences.KEY_NOTIFICATION_PERMISSION_REQUESTED, true)
    }
}