package com.nagopy.kmp.habittracker.presentation.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nagopy.kmp.habittracker.presentation.permission.NotificationPermissionViewModel
import com.nagopy.kmp.habittracker.util.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for app-level initialization tasks.
 */
class AppViewModel(
    val notificationPermissionViewModel: NotificationPermissionViewModel
) : ViewModel() {
    
    private val _initializationCompleted = MutableStateFlow(false)
    val initializationCompleted: StateFlow<Boolean> = _initializationCompleted.asStateFlow()
    
    init {
        initializeApp()
    }
    
    private fun initializeApp() {
        viewModelScope.launch {
            try {
                // Start the notification permission flow
                notificationPermissionViewModel.startPermissionFlow()
                _initializationCompleted.value = true
            } catch (e: Exception) {
                Logger.e(e, "Failed to initialize app", "AppViewModel")
                _initializationCompleted.value = true
            }
        }
    }
}