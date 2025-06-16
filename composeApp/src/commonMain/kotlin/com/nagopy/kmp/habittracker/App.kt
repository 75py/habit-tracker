package com.nagopy.kmp.habittracker

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import com.nagopy.kmp.habittracker.presentation.app.AppViewModel
import com.nagopy.kmp.habittracker.presentation.navigation.HabitTrackerNavigation
import com.nagopy.kmp.habittracker.presentation.permission.*
import org.koin.compose.koinInject

@Composable
fun App() {
    MaterialTheme {
        // Initialize app-level functionality (including notification permissions)
        val appViewModel: AppViewModel = koinInject()
        val notificationPermissionState by appViewModel.notificationPermissionViewModel.permissionState.collectAsState()
        val initializationCompleted by appViewModel.initializationCompleted.collectAsState()
        
        // Show notification permission dialogs if needed
        when (notificationPermissionState) {
            is NotificationPermissionState.ShowNotificationExplanation -> {
                NotificationPermissionExplanationDialog(
                    onConfirm = { appViewModel.notificationPermissionViewModel.onNotificationExplanationConfirmed() },
                    onDismiss = { appViewModel.notificationPermissionViewModel.onNotificationExplanationDismissed() }
                )
            }
            is NotificationPermissionState.ShowExactAlarmExplanation -> {
                ExactAlarmPermissionExplanationDialog(
                    onConfirm = { appViewModel.notificationPermissionViewModel.onExactAlarmExplanationConfirmed() },
                    onDismiss = { appViewModel.notificationPermissionViewModel.onExactAlarmExplanationDismissed() }
                )
            }
            is NotificationPermissionState.NotificationPermissionDenied -> {
                NotificationPermissionDeniedDialog(
                    onDismiss = { appViewModel.notificationPermissionViewModel.onNotificationPermissionDeniedDismissed() }
                )
            }
            else -> {
                // No dialog needed for other states
            }
        }
        
        // Show main app screens only after initialization is completed
        if (initializationCompleted) {
            HabitTrackerNavigation()
        }
    }
}