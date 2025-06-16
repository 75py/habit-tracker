package com.nagopy.kmp.habittracker.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nagopy.kmp.habittracker.presentation.habitedit.HabitEditScreen
import com.nagopy.kmp.habittracker.presentation.habitedit.HabitEditViewModel
import com.nagopy.kmp.habittracker.presentation.habitlist.HabitListScreen
import com.nagopy.kmp.habittracker.presentation.habitlist.HabitListViewModel
import com.nagopy.kmp.habittracker.presentation.today.TodayScreen
import com.nagopy.kmp.habittracker.presentation.today.TodayViewModel
import com.nagopy.kmp.habittracker.util.Logger
import org.koin.compose.koinInject

/**
 * Navigation routes for the habit tracker app
 */
object HabitTrackerRoutes {
    const val HABIT_LIST = "habit_list"
    const val ADD_HABIT = "add_habit"
    const val EDIT_HABIT = "edit_habit/{habitId}"
    const val TODAY = "today"
    
    fun editHabit(habitId: Long): String = "edit_habit/$habitId"
}

/**
 * Main navigation host for the habit tracker app
 */
@Composable
fun HabitTrackerNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = HabitTrackerRoutes.HABIT_LIST
    ) {
        composable(HabitTrackerRoutes.HABIT_LIST) {
            Logger.d("Navigating to screen: HabitList", tag = "Navigation")
            val viewModel: HabitListViewModel = koinInject()
            HabitListScreen(
                onAddHabitClick = { 
                    navController.navigate(HabitTrackerRoutes.ADD_HABIT)
                },
                onTodayClick = { 
                    navController.navigate(HabitTrackerRoutes.TODAY)
                },
                onHabitEdit = { habit -> 
                    navController.navigate(HabitTrackerRoutes.editHabit(habit.id))
                },
                viewModel = viewModel
            )
        }
        
        composable(HabitTrackerRoutes.ADD_HABIT) {
            Logger.d("Navigating to screen: AddHabit", tag = "Navigation")
            val viewModel: HabitEditViewModel = koinInject()
            HabitEditScreen(
                onSaveSuccess = { 
                    navController.popBackStack()
                },
                onNavigateBack = { 
                    navController.popBackStack()
                },
                viewModel = viewModel
            )
        }
        
        composable(HabitTrackerRoutes.EDIT_HABIT) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getString("habitId")?.toLongOrNull()
            Logger.d("Navigating to screen: EditHabit with habitId: $habitId", tag = "Navigation")
            val viewModel: HabitEditViewModel = koinInject()
            HabitEditScreen(
                habitId = habitId,
                onSaveSuccess = { 
                    navController.popBackStack()
                },
                onNavigateBack = { 
                    navController.popBackStack()
                },
                viewModel = viewModel
            )
        }
        
        composable(HabitTrackerRoutes.TODAY) {
            Logger.d("Navigating to screen: Today", tag = "Navigation")
            val viewModel: TodayViewModel = koinInject()
            TodayScreen(
                viewModel = viewModel,
                onNavigateBack = { 
                    navController.popBackStack()
                }
            )
        }
    }
}