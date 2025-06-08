package com.nagopy.kmp.habittracker.presentation.ui

import androidx.compose.ui.graphics.Color

/**
 * iOS implementation of color parsing
 */
actual fun parseColor(colorString: String): Color {
    // Remove the '#' prefix if present
    val hex = colorString.removePrefix("#")
    
    // Parse the hex string to a long value
    val colorLong = hex.toLong(16)
    
    // Convert to ARGB format (assuming the input is RGB, so add full alpha)
    val argb = if (hex.length == 6) {
        0xFF000000L or colorLong
    } else {
        colorLong
    }
    
    return Color(argb.toULong())
}