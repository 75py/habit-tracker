package com.nagopy.kmp.habittracker

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import com.nagopy.kmp.habittracker.presentation.app.AppViewModel
import com.nagopy.kmp.habittracker.presentation.habitedit.HabitEditScreen
import com.nagopy.kmp.habittracker.presentation.habitedit.HabitEditViewModel
import com.nagopy.kmp.habittracker.presentation.habitlist.HabitListScreen
import com.nagopy.kmp.habittracker.presentation.habitlist.HabitListViewModel
import com.nagopy.kmp.habittracker.presentation.today.TodayScreen
import com.nagopy.kmp.habittracker.presentation.today.TodayViewModel
import com.nagopy.kmp.habittracker.util.Logger
import org.jetbrains.compose.ui.tooling.preview.Preview
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
        
        var currentScreen by remember { mutableStateOf<Screen>(Screen.HabitList) }
        
        // Log screen navigation for debugging
        LaunchedEffect(currentScreen) {
            Logger.d("Navigating to screen: $currentScreen", tag = "Navigation")
        }
        
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
            
            Screen.AddHabit -> {
                val viewModel: HabitEditViewModel = koinInject()
                HabitEditScreen(
                    onSaveSuccess = { currentScreen = Screen.HabitList },
                    onNavigateBack = { currentScreen = Screen.HabitList },
                    viewModel = viewModel
                )
            }
            
            is Screen.EditHabit -> {
                val viewModel: HabitEditViewModel = koinInject()
                val editScreen = currentScreen as Screen.EditHabit
                HabitEditScreen(
                    habitId = editScreen.habitId,
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