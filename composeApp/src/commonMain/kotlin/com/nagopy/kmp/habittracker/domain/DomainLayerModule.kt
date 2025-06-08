package com.nagopy.kmp.habittracker.domain

import com.nagopy.kmp.habittracker.domain.usecase.AddHabitUseCase
import com.nagopy.kmp.habittracker.domain.usecase.CompleteTaskUseCase
import com.nagopy.kmp.habittracker.domain.usecase.GetAllHabitsUseCase
import com.nagopy.kmp.habittracker.domain.usecase.GetTodayTasksUseCase
import org.koin.dsl.module

/**
 * Domain Layer - Contains business logic and domain entities
 * 
 * This layer contains:
 * - Domain entities (business models)
 * - Use cases (business logic)
 * - Repository interfaces
 * - Domain services
 */
val domainModule = module {
    single { GetAllHabitsUseCase(get()) }
    single { AddHabitUseCase(get()) }
    single { GetTodayTasksUseCase(get()) }
    single { CompleteTaskUseCase(get()) }
}