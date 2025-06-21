package com.nagopy.kmp.habittracker.presentation.navigation

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.nagopy.kmp.habittracker.util.Logger
import kotlin.math.abs

/**
 * iOS implementation of SwipeBackHandler that detects edge swipe gestures
 * Uses Compose's gesture detection to provide iOS-like swipe back functionality
 */
@Composable
actual fun SwipeBackHandler(
    enabled: Boolean,
    onSwipeBack: () -> Unit,
    content: @Composable () -> Unit
) {
    var swipeTriggered by remember { mutableStateOf(false) }
    
    Box(modifier = Modifier.fillMaxSize()) {
        content()
        
        if (enabled) {
            // Create an invisible gesture detection area on the left edge (20dp wide strip)
            Box(
                modifier = Modifier
                    .width(20.dp)
                    .fillMaxHeight()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                swipeTriggered = false
                                Logger.d("Swipe gesture started at offset: $offset", tag = "SwipeBack")
                            },
                            onDragEnd = {
                                Logger.d("Swipe gesture ended, triggered: $swipeTriggered", tag = "SwipeBack")
                            }
                        ) { change, dragAmount ->
                            // Only trigger swipe back for rightward gestures that start from the left edge
                            val totalDragX = dragAmount.x
                            val isRightwardSwipe = totalDragX > 0
                            val swipeDistance = abs(totalDragX)
                            
                            // Log every few pixels to avoid spam
                            if (swipeDistance.toInt() % 20 == 0) {
                                Logger.d("Swipe progress: dragX=$totalDragX, distance=$swipeDistance", tag = "SwipeBack")
                            }
                            
                            // Trigger swipe back if the swipe is rightward and significant enough (30dp threshold)
                            if (isRightwardSwipe && swipeDistance > 30.0f && !swipeTriggered) {
                                swipeTriggered = true
                                Logger.d("Triggering swipe back - rightward swipe detected with distance: $swipeDistance", tag = "SwipeBack")
                                onSwipeBack()
                            }
                        }
                    }
            )
        }
    }
}