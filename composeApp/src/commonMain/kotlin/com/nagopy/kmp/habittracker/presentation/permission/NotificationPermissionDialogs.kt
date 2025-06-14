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
        title = { Text("通知の許可について") },
        text = { Text("プッシュ通知を送るために許可が必要です。この後表示される画面でプッシュ通知を許可してください。") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル")
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
        title = { Text("リマインダーの許可について") },
        text = { Text("正確な時刻にリマインダーを送るため、次の画面でリマインダーの許可をしてください。") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("スキップ")
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
        title = { Text("通知の許可が得られませんでした") },
        text = { Text("通知の許可を得られませんでした。アプリは引き続き使用できますが、リマインダー通知は届きません。") },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}