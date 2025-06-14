package com.nagopy.kmp.habittracker.presentation.permission

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

/**
 * Dialog explaining why notification permissions are needed before requesting them.
 */
@Composable
fun NotificationPermissionExplanationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Notification Permission") },
        text = { Text("Permission is required to send push notifications. Please allow push notifications in the next screen.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Dialog explaining why exact alarm permissions are needed before requesting them.
 */
@Composable
fun ExactAlarmPermissionExplanationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reminder Permission") },
        text = { Text("Please allow reminders in the next screen to send reminders at exact times.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Skip")
            }
        }
    )
}

/**
 * Dialog shown when notification permission is denied.
 */
@Composable
fun NotificationPermissionDeniedDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Notification Permission Denied") },
        text = { Text("Notification permission was not granted. You can continue using the app, but you will not receive reminder notifications.") },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}