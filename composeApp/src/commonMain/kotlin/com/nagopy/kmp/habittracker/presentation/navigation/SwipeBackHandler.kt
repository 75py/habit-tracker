package com.nagopy.kmp.habittracker.presentation.navigation

import androidx.compose.runtime.Composable

/**
 * Interface for handling swipe back gestures across platforms
 */
interface SwipeBackGestureDetector {
    /**
     * Enables or disables swipe back gesture detection
     */
    fun setSwipeBackEnabled(enabled: Boolean)
    
    /**
     * Sets the callback to be triggered when a swipe back gesture is detected
     */
    fun setOnSwipeBack(callback: () -> Unit)
}

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