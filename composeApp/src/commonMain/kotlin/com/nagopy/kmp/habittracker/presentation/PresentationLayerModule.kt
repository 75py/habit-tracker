package com.nagopy.kmp.habittracker.presentation

import com.nagopy.kmp.habittracker.presentation.habitedit.HabitEditViewModel
import com.nagopy.kmp.habittracker.presentation.habitlist.HabitListViewModel
import org.koin.core.module.dsl.viewModel
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
    viewModel { HabitListViewModel(get()) }
    viewModel { HabitEditViewModel(get()) }
}