package com.nagopy.kmp.habittracker

import androidx.compose.ui.window.ComposeUIViewController
import com.nagopy.kmp.habittracker.di.initKoin
import org.koin.core.context.GlobalContext

fun MainViewController() = ComposeUIViewController { 
    // Initialize Koin only if it hasn't been initialized yet
    if (GlobalContext.getOrNull() == null) {
        initKoin()
    }
    App() 
}