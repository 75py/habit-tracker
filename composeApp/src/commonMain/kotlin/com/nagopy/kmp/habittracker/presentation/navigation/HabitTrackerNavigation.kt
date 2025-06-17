package com.nagopy.kmp.habittracker.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
 * Animation constants for navigation transitions
 */
private object NavigationAnimations {
    const val ANIMATION_DURATION = 300
    const val FADE_DURATION = 200
}

/**
 * Standard slide-in animation from right to left
 */
private val slideInFromRight = slideInHorizontally(
    initialOffsetX = { it },
    animationSpec = tween(NavigationAnimations.ANIMATION_DURATION)
) + fadeIn(animationSpec = tween(NavigationAnimations.FADE_DURATION))

/**
 * Standard slide-out animation from left to right
 */
private val slideOutToRight = slideOutHorizontally(
    targetOffsetX = { it },
    animationSpec = tween(NavigationAnimations.ANIMATION_DURATION)
) + fadeOut(animationSpec = tween(NavigationAnimations.FADE_DURATION))

/**
 * Standard slide-in animation from left to right (for back navigation)
 */
private val slideInFromLeft = slideInHorizontally(
    initialOffsetX = { -it },
    animationSpec = tween(NavigationAnimations.ANIMATION_DURATION)
) + fadeIn(animationSpec = tween(NavigationAnimations.FADE_DURATION))

/**
 * Standard slide-out animation from right to left (for back navigation)
 */
private val slideOutToLeft = slideOutHorizontally(
    targetOffsetX = { -it },
    animationSpec = tween(NavigationAnimations.ANIMATION_DURATION)
) + fadeOut(animationSpec = tween(NavigationAnimations.FADE_DURATION))

/**
 * Main navigation host for the habit tracker app
 */
@Composable
fun HabitTrackerNavigation(
    navController: NavHostController = rememberNavController()
) {
    // State to prevent multiple rapid navigation actions
    val isNavigating = remember { mutableStateOf(false) }
    
    // Safe navigation function that prevents multiple rapid calls
    val safeNavigateBack: () -> Unit = {
        if (!isNavigating.value && navController.previousBackStackEntry != null) {
            isNavigating.value = true
            Logger.d("Safe navigation: popping back stack", tag = "Navigation")
            try {
                navController.popBackStack()
            } catch (e: Exception) {
                Logger.e(e, "Failed to pop back stack", tag = "Navigation")
            } finally {
                // Reset flag after a short delay to allow navigation to complete
                isNavigating.value = false
            }
        } else {
            Logger.d("Navigation blocked: isNavigating=${isNavigating.value}, hasBackStack=${navController.previousBackStackEntry != null}", tag = "Navigation")
        }
    }
    
    NavHost(
        navController = navController,
        startDestination = HabitTrackerRoutes.HABIT_LIST
    ) {
        composable(
            route = HabitTrackerRoutes.HABIT_LIST,
            enterTransition = { slideInFromLeft },
            exitTransition = { slideOutToLeft },
            popEnterTransition = { slideInFromLeft },
            popExitTransition = { slideOutToRight }
        ) {
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
        
        composable(
            route = HabitTrackerRoutes.ADD_HABIT,
            enterTransition = { slideInFromRight },
            exitTransition = { slideOutToRight },
            popEnterTransition = { slideInFromLeft },
            popExitTransition = { slideOutToRight }
        ) {
            Logger.d("Navigating to screen: AddHabit", tag = "Navigation")
            val viewModel: HabitEditViewModel = koinInject()
            SwipeBackHandler(
                enabled = true,
                onSwipeBack = { safeNavigateBack() }
            ) {
                HabitEditScreen(
                    onSaveSuccess = { 
                        safeNavigateBack()
                    },
                    onNavigateBack = { 
                        safeNavigateBack()
                    },
                    viewModel = viewModel
                )
            }
        }
        
        composable(
            route = HabitTrackerRoutes.EDIT_HABIT,
            enterTransition = { slideInFromRight },
            exitTransition = { slideOutToRight },
            popEnterTransition = { slideInFromLeft },
            popExitTransition = { slideOutToRight }
        ) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getString("habitId")?.toLongOrNull()
            Logger.d("Navigating to screen: EditHabit with habitId: $habitId", tag = "Navigation")
            val viewModel: HabitEditViewModel = koinInject()
            SwipeBackHandler(
                enabled = true,
                onSwipeBack = { safeNavigateBack() }
            ) {
                HabitEditScreen(
                    habitId = habitId,
                    onSaveSuccess = { 
                        safeNavigateBack()
                    },
                    onNavigateBack = { 
                        safeNavigateBack()
                    },
                    viewModel = viewModel
                )
            }
        }
        
        composable(
            route = HabitTrackerRoutes.TODAY,
            enterTransition = { slideInFromRight },
            exitTransition = { slideOutToRight },
            popEnterTransition = { slideInFromLeft },
            popExitTransition = { slideOutToRight }
        ) {
            Logger.d("Navigating to screen: Today", tag = "Navigation")
            val viewModel: TodayViewModel = koinInject()
            SwipeBackHandler(
                enabled = true,
                onSwipeBack = { safeNavigateBack() }
            ) {
                TodayScreen(
                    viewModel = viewModel,
                    onNavigateBack = { 
                        safeNavigateBack()
                    }
                )
            }
        }
    }
}