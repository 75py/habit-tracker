package com.nagopy.kmp.habittracker

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import com.nagopy.kmp.habittracker.presentation.app.AppViewModel
import com.nagopy.kmp.habittracker.presentation.habitedit.HabitEditDialog
import com.nagopy.kmp.habittracker.presentation.habitedit.HabitEditViewModel
import com.nagopy.kmp.habittracker.presentation.habitlist.HabitListScreen
import com.nagopy.kmp.habittracker.presentation.habitlist.HabitListViewModel
import com.nagopy.kmp.habittracker.presentation.permission.*
import com.nagopy.kmp.habittracker.presentation.today.TodayScreen
import com.nagopy.kmp.habittracker.presentation.today.TodayViewModel
import com.nagopy.kmp.habittracker.util.Logger
import org.koin.compose.koinInject

sealed class Screen {
    data object HabitList : Screen()
    data object AddHabit : Screen()
    data class EditHabit(val habitId: Long) : Screen()
    data object Today : Screen()
}

@Composable
fun App() {
    MaterialTheme {
        // Initialize app-level functionality (including notification permissions)
        val appViewModel: AppViewModel = koinInject()
        val notificationPermissionState by appViewModel.notificationPermissionViewModel.permissionState.collectAsState()
        val initializationCompleted by appViewModel.initializationCompleted.collectAsState()
        
        var currentScreen by remember { mutableStateOf<Screen>(Screen.HabitList) }
        
        // Log screen navigation for debugging
        LaunchedEffect(currentScreen) {
            Logger.d("Navigating to screen: $currentScreen", tag = "Navigation")
        }
        
        // Show notification permission dialogs if needed
        when (notificationPermissionState) {
            is NotificationPermissionState.ShowNotificationExplanation -> {
                NotificationPermissionExplanationDialog(
                    onConfirm = { appViewModel.notificationPermissionViewModel.onNotificationExplanationConfirmed() },
                    onDismiss = { appViewModel.notificationPermissionViewModel.onNotificationExplanationDismissed() }
                )
            }
            is NotificationPermissionState.ShowExactAlarmExplanation -> {
                ExactAlarmPermissionExplanationDialog(
                    onConfirm = { appViewModel.notificationPermissionViewModel.onExactAlarmExplanationConfirmed() },
                    onDismiss = { appViewModel.notificationPermissionViewModel.onExactAlarmExplanationDismissed() }
                )
            }
            is NotificationPermissionState.NotificationPermissionDenied -> {
                NotificationPermissionDeniedDialog(
                    onDismiss = { appViewModel.notificationPermissionViewModel.onNotificationPermissionDeniedDismissed() }
                )
            }
            else -> {
                // No dialog needed for other states
            }
        }
        
        // Show main app screens only after initialization is completed
        if (initializationCompleted) {
            when (currentScreen) {
                Screen.HabitList -> {
                    val viewModel: HabitListViewModel = koinInject()
                    HabitListScreen(
                        onAddHabitClick = { currentScreen = Screen.AddHabit },
                        onTodayClick = { currentScreen = Screen.Today },
                        onHabitEdit = { habit -> currentScreen = Screen.EditHabit(habit.id) },
                        viewModel = viewModel
                    )
                }
                
                Screen.Today -> {
                    val viewModel: TodayViewModel = koinInject()
                    TodayScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = Screen.HabitList }
                    )
                }
                
                // For modal screens (AddHabit, EditHabit), show the base HabitListScreen
                Screen.AddHabit, is Screen.EditHabit -> {
                    val viewModel: HabitListViewModel = koinInject()
                    HabitListScreen(
                        onAddHabitClick = { currentScreen = Screen.AddHabit },
                        onTodayClick = { currentScreen = Screen.Today },
                        onHabitEdit = { habit -> currentScreen = Screen.EditHabit(habit.id) },
                        viewModel = viewModel
                    )
                }
            }

            // Show modal dialogs for habit add/edit
            when (currentScreen) {
                Screen.AddHabit -> {
                    val viewModel: HabitEditViewModel = koinInject()
                    HabitEditDialog(
                        onSaveSuccess = { currentScreen = Screen.HabitList },
                        onDismiss = { currentScreen = Screen.HabitList },
                        viewModel = viewModel
                    )
                }
                
                is Screen.EditHabit -> {
                    val viewModel: HabitEditViewModel = koinInject()
                    val editScreen = currentScreen as Screen.EditHabit
                    HabitEditDialog(
                        habitId = editScreen.habitId,
                        onSaveSuccess = { currentScreen = Screen.HabitList },
                        onDismiss = { currentScreen = Screen.HabitList },
                        viewModel = viewModel
                    )
                }
                
                else -> {
                    // No modal for other screens
                }
            }
        }
    }
}