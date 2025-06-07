package com.nagopy.kmp.habittracker

import androidx.compose.ui.window.ComposeUIViewController
import com.nagopy.kmp.habittracker.di.initKoin

fun MainViewController() = ComposeUIViewController { 
    initKoin()
    App() 
}