package com.nagopy.kmp.habittracker.presentation.navigation

import androidx.compose.runtime.Composable

/**
 * Android implementation of SwipeBackHandler
 * On Android, swipe back is typically handled by the system gesture navigation
 * so this implementation simply renders the content without additional gesture handling
 */
@Composable
actual fun SwipeBackHandler(
    enabled: Boolean,
    onSwipeBack: () -> Unit,
    content: @Composable () -> Unit
) {
    // On Android, we don't typically implement custom swipe back
    // as the system handles this through gesture navigation
    content()
}