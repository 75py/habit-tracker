package com.nagopy.kmp.habittracker.domain

import com.nagopy.kmp.habittracker.domain.usecase.AddHabitUseCase
import com.nagopy.kmp.habittracker.domain.usecase.CompleteTaskUseCase
import com.nagopy.kmp.habittracker.domain.usecase.CompleteTaskFromNotificationUseCase
import com.nagopy.kmp.habittracker.domain.usecase.DeleteHabitUseCase
import com.nagopy.kmp.habittracker.domain.usecase.GetAllHabitsUseCase
import com.nagopy.kmp.habittracker.domain.usecase.GetHabitUseCase
import com.nagopy.kmp.habittracker.domain.usecase.GetTodayTasksUseCase
import com.nagopy.kmp.habittracker.domain.usecase.ManageNotificationsUseCase
import com.nagopy.kmp.habittracker.domain.usecase.UpdateHabitUseCase
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
    single { UpdateHabitUseCase(get()) }
    single { DeleteHabitUseCase(get()) }
    single { GetHabitUseCase(get()) }
    single { GetTodayTasksUseCase(get()) }
    single { CompleteTaskUseCase(get()) }
    single { ManageNotificationsUseCase(get(), get()) }
    single { CompleteTaskFromNotificationUseCase(get(), get()) }
}