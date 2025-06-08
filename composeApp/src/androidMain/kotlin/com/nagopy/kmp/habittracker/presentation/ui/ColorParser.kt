package com.nagopy.kmp.habittracker.presentation.ui

import androidx.compose.ui.graphics.Color

/**
 * Android implementation of color parsing using Android graphics Color
 */
actual fun parseColor(colorString: String): Color {
    return Color(android.graphics.Color.parseColor(colorString))
}