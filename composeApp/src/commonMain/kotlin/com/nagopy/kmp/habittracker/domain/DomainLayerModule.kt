package com.nagopy.kmp.habittracker.domain

import com.nagopy.kmp.habittracker.domain.usecase.AddHabitUseCase
import com.nagopy.kmp.habittracker.domain.usecase.CompleteTaskUseCase
import com.nagopy.kmp.habittracker.domain.usecase.CompleteTaskFromNotificationUseCase
import com.nagopy.kmp.habittracker.domain.usecase.DeleteHabitUseCase
import com.nagopy.kmp.habittracker.domain.usecase.GetAllHabitsUseCase
import com.nagopy.kmp.habittracker.domain.usecase.GetHabitUseCase
import com.nagopy.kmp.habittracker.domain.usecase.GetNextTasksUseCase
import com.nagopy.kmp.habittracker.domain.usecase.GetTodayTasksUseCase
import com.nagopy.kmp.habittracker.domain.usecase.ManageNotificationsUseCase
import com.nagopy.kmp.habittracker.domain.usecase.ScheduleNextNotificationUseCase
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
    single { UpdateHabitUseCase(get(), get()) }
    single { DeleteHabitUseCase(get(), get()) }
    single { GetHabitUseCase(get()) }
    single { GetTodayTasksUseCase(get()) }
    single { GetNextTasksUseCase(get()) }
    single { CompleteTaskUseCase(get()) }
    single { ManageNotificationsUseCase(get()) }
    single { ScheduleNextNotificationUseCase(get(), get(), get()) }
    single { CompleteTaskFromNotificationUseCase(get(), get()) }
}