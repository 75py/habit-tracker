package com.nagopy.kmp.habittracker.data

import com.nagopy.kmp.habittracker.domain.repository.HabitRepository
import org.koin.dsl.module

/**
 * Data Layer - Responsible for data access, storage, and external APIs
 * 
 * This layer contains:
 * - Database entities and DAOs
 * - Repository implementations
 * - Data sources (local and remote)
 * - Data mappers
 */
val dataModule = module {
    includes(databaseModule)
    single<HabitRepository> { HabitRepositoryImpl(get()) }
}