package com.nagopy.kmp.habittracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.nagopy.kmp.habittracker.notification.AndroidNotificationPermissionManager
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    
    private val notificationPermissionManager: AndroidNotificationPermissionManager by inject()
    
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
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Clear activity reference to prevent memory leaks
        notificationPermissionManager.clearActivity()
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        notificationPermissionManager.onPermissionResult(requestCode, grantResults)
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}