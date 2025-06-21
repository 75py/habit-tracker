package com.nagopy.kmp.habittracker.presentation.permission

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import habittracker.composeapp.generated.resources.Res
import habittracker.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

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
        title = { Text(stringResource(Res.string.notification_permission)) },
        text = { Text(stringResource(Res.string.notification_permission_explanation)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(Res.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.cancel))
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
        title = { Text(stringResource(Res.string.reminder_permission)) },
        text = { Text(stringResource(Res.string.reminder_permission_explanation)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(Res.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.skip))
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
        title = { Text(stringResource(Res.string.notification_permission_denied)) },
        text = { Text(stringResource(Res.string.notification_permission_denied_explanation)) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.ok))
            }
        }
    )
}

// ========== Previews ==========

@Preview
@Composable
private fun NotificationPermissionExplanationDialogPreview() {
    MaterialTheme {
        NotificationPermissionExplanationDialog(
            onConfirm = {},
            onDismiss = {}
        )
    }
}

@Preview
@Composable
private fun ExactAlarmPermissionExplanationDialogPreview() {
    MaterialTheme {
        ExactAlarmPermissionExplanationDialog(
            onConfirm = {},
            onDismiss = {}
        )
    }
}

@Preview
@Composable
private fun NotificationPermissionDeniedDialogPreview() {
    MaterialTheme {
        NotificationPermissionDeniedDialog(
            onDismiss = {}
        )
    }
}