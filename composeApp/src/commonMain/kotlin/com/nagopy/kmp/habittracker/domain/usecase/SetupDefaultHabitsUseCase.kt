package com.nagopy.kmp.habittracker.domain.usecase

import com.nagopy.kmp.habittracker.domain.model.Habit
import com.nagopy.kmp.habittracker.domain.model.HabitDetail
import com.nagopy.kmp.habittracker.domain.repository.HabitRepository
import com.nagopy.kmp.habittracker.util.Logger
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/**
 * Use case to setup default habits for new users.
 * Creates a default "水を飲む" (drink water) habit if it doesn't already exist.
 */
class SetupDefaultHabitsUseCase(
    private val habitRepository: HabitRepository
) {
    
    /**
     * Creates default habits if they don't already exist.
     * This prevents duplicate default habits from being created.
     */
    suspend operator fun invoke() {
        try {
            val defaultHabitName = "水を飲む"
            
            // Check if default habit already exists
            if (!habitRepository.doesHabitExistByName(defaultHabitName)) {
                Logger.d("Creating default habit: $defaultHabitName", tag = "SetupDefaultHabitsUseCase")
                
                val defaultHabit = Habit(
                    name = defaultHabitName,
                    description = "",
                    color = "#2196F3",
                    isActive = true,
                    createdAt = Clock.System.todayIn(TimeZone.currentSystemDefault()),
                    detail = HabitDetail.IntervalHabitDetail(
                        intervalMinutes = 30,
                        startTime = LocalTime(9, 5),
                        endTime = LocalTime(17, 35)
                    )
                )
                
                habitRepository.createHabit(defaultHabit)
                Logger.d("Default habit created successfully", tag = "SetupDefaultHabitsUseCase")
            } else {
                Logger.d("Default habit already exists, skipping creation", tag = "SetupDefaultHabitsUseCase")
            }
        } catch (e: Exception) {
            Logger.e(e, "Failed to setup default habits", tag = "SetupDefaultHabitsUseCase")
            // Don't re-throw the exception to avoid breaking app initialization
        }
    }
}