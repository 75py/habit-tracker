package com.nagopy.kmp.habittracker

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import com.nagopy.kmp.habittracker.domain.usecase.RequestNotificationPermissionOnStartupUseCase
import com.nagopy.kmp.habittracker.presentation.habitedit.HabitEditScreen
import com.nagopy.kmp.habittracker.presentation.habitedit.HabitEditViewModel
import com.nagopy.kmp.habittracker.presentation.habitlist.HabitListScreen
import com.nagopy.kmp.habittracker.presentation.habitlist.HabitListViewModel
import com.nagopy.kmp.habittracker.presentation.today.TodayScreen
import com.nagopy.kmp.habittracker.presentation.today.TodayViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject

sealed class Screen {
    data object HabitList : Screen()
    data object AddHabit : Screen()
    data object Today : Screen()
}

@Composable
@Preview
fun App() {
    MaterialTheme {
        val requestPermissionUseCase: RequestNotificationPermissionOnStartupUseCase = koinInject()
        val coroutineScope = rememberCoroutineScope()
        
        // Request notification permission on first composition
        LaunchedEffect(Unit) {
            coroutineScope.launch {
                try {
                    requestPermissionUseCase()
                } catch (e: Exception) {
                    // Silently handle permission request failures
                    // The app should still work without notifications
                }
            }
        }
        
        var currentScreen by remember { mutableStateOf<Screen>(Screen.HabitList) }
        
        when (currentScreen) {
            Screen.HabitList -> {
                val viewModel: HabitListViewModel = koinInject()
                HabitListScreen(
                    onAddHabitClick = { currentScreen = Screen.AddHabit },
                    onTodayClick = { currentScreen = Screen.Today },
                    onHabitClick = { /* TODO: Navigate to edit habit */ },
                    viewModel = viewModel
                )
            }
            
            Screen.AddHabit -> {
                val viewModel: HabitEditViewModel = koinInject()
                HabitEditScreen(
                    onSaveSuccess = { currentScreen = Screen.HabitList },
                    onNavigateBack = { currentScreen = Screen.HabitList },
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
        }
    }
}