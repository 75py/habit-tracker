package com.nagopy.kmp.habittracker.presentation

import com.nagopy.kmp.habittracker.presentation.habitedit.HabitEditViewModel
import com.nagopy.kmp.habittracker.presentation.habitlist.HabitListViewModel
import com.nagopy.kmp.habittracker.presentation.today.TodayViewModel
import org.koin.dsl.module

/**
 * Presentation Layer - Handles UI and user interactions
 * 
 * This layer contains:
 * - ViewModels
 * - UI components and screens
 * - Navigation logic
 * - State management
 */
val presentationModule = module {
    factory { HabitListViewModel(get()) }
    factory { HabitEditViewModel(get()) }
    factory { TodayViewModel(get(), get()) }
}