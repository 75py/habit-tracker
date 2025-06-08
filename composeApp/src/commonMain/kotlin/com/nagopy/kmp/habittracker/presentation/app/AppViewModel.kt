package com.nagopy.kmp.habittracker.presentation.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nagopy.kmp.habittracker.domain.usecase.RequestNotificationPermissionOnStartupUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for app-level initialization tasks.
 */
class AppViewModel(
    private val requestNotificationPermissionOnStartupUseCase: RequestNotificationPermissionOnStartupUseCase
) : ViewModel() {
    
    private val _permissionRequestCompleted = MutableStateFlow(false)
    val permissionRequestCompleted: StateFlow<Boolean> = _permissionRequestCompleted.asStateFlow()
    
    init {
        requestNotificationPermissionOnStartup()
    }
    
    private fun requestNotificationPermissionOnStartup() {
        viewModelScope.launch {
            try {
                requestNotificationPermissionOnStartupUseCase()
            } catch (e: Exception) {
                // Silently handle permission request failures
                // The app should still work without notifications
            } finally {
                _permissionRequestCompleted.value = true
            }
        }
    }
}