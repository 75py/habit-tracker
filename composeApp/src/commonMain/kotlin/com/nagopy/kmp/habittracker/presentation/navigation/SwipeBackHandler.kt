package com.nagopy.kmp.habittracker.presentation.navigation

import androidx.compose.runtime.Composable

/**
 * Composable function that handles swipe back gestures in a platform-specific way
 * On iOS, this enables native swipe back gesture recognition
 * On other platforms, this may provide touch-based swipe detection
 */
@Composable
expect fun SwipeBackHandler(
    enabled: Boolean = true,
    onSwipeBack: () -> Unit,
    content: @Composable () -> Unit
)