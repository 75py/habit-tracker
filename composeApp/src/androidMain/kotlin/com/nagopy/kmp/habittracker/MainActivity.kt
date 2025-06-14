package com.nagopy.kmp.habittracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.nagopy.kmp.habittracker.notification.AndroidNotificationPermissionManager
import com.nagopy.kmp.habittracker.presentation.app.AppViewModel
import com.nagopy.kmp.habittracker.presentation.permission.NotificationPermissionState
import com.nagopy.kmp.habittracker.util.Logger
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    
    private val notificationPermissionManager: AndroidNotificationPermissionManager by inject()
    private val appViewModel: AppViewModel by inject()
    
    private var wasRequestingNotificationPermission = false
    private var wasRequestingExactAlarmPermission = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        // Set activity reference for permission requests
        notificationPermissionManager.setActivity(this)

        setContent {
            App()
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Refresh activity reference when resuming
        notificationPermissionManager.setActivity(this)
        
        // Handle returning from permission requests
        handlePermissionRequestResults()
    }
    
    override fun onPause() {
        super.onPause()
        // Track current permission request states
        val currentState = appViewModel.notificationPermissionViewModel.permissionState.value
        wasRequestingNotificationPermission = currentState is NotificationPermissionState.RequestingNotificationPermission
        wasRequestingExactAlarmPermission = currentState is NotificationPermissionState.RequestingExactAlarmPermission
        
        Logger.d("onPause: wasRequestingNotificationPermission=$wasRequestingNotificationPermission, wasRequestingExactAlarmPermission=$wasRequestingExactAlarmPermission", "MainActivity")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Clear activity reference to prevent memory leaks
        notificationPermissionManager.clearActivity()
    }
    
    private fun handlePermissionRequestResults() {
        when {
            wasRequestingNotificationPermission -> {
                Logger.d("Returning from notification permission request", "MainActivity")
                appViewModel.notificationPermissionViewModel.onReturnFromNotificationPermissionRequest()
                wasRequestingNotificationPermission = false
            }
            wasRequestingExactAlarmPermission -> {
                Logger.d("Returning from exact alarm permission request", "MainActivity")
                appViewModel.notificationPermissionViewModel.onReturnFromExactAlarmPermissionRequest()
                wasRequestingExactAlarmPermission = false
            }
        }
    }

}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}