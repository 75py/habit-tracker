package com.nagopy.kmp.habittracker.presentation.ui

import androidx.compose.ui.graphics.Color

/**
 * iOS implementation of color parsing
 */
actual fun parseColor(colorString: String): Color {
    // Remove the '#' prefix if present
    val hex = colorString.removePrefix("#")
    
    // Ensure we have a 6-character hex string
    require(hex.length == 6) { "Color string must be in format #RRGGBB" }
    
    // Parse RGB components
    val red = hex.substring(0, 2).toInt(16) / 255f
    val green = hex.substring(2, 4).toInt(16) / 255f
    val blue = hex.substring(4, 6).toInt(16) / 255f
    
    return Color(red = red, green = green, blue = blue, alpha = 1f)
}