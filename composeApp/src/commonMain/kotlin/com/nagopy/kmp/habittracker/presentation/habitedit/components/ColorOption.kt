package com.nagopy.kmp.habittracker.presentation.habitedit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nagopy.kmp.habittracker.presentation.ui.parseColor
import habittracker.composeapp.generated.resources.Res
import habittracker.composeapp.generated.resources.selected
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun ColorOption(
    color: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val parsedColor = parseColor(color)
    
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(parsedColor)
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 3.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
                } else {
                    Modifier.border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = CircleShape
                    )
                }
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = stringResource(Res.string.selected),
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// ========== Previews ==========

@Preview
@Composable
private fun ColorOptionSelectedPreview() {
    MaterialTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            ColorOption(
                color = "#2196F3",
                isSelected = true,
                onClick = {}
            )
        }
    }
}

@Preview
@Composable
private fun ColorOptionUnselectedPreview() {
    MaterialTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            ColorOption(
                color = "#4CAF50",
                isSelected = false,
                onClick = {}
            )
        }
    }
}

@Preview
@Composable
private fun ColorOptionRowPreview() {
    val colors = listOf("#2196F3", "#4CAF50", "#FF9800", "#9C27B0", "#F44336")
    
    MaterialTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                colors.forEachIndexed { index, color ->
                    ColorOption(
                        color = color,
                        isSelected = index == 1, // Select the second one
                        onClick = {}
                    )
                }
            }
        }
    }
}