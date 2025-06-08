package com.nagopy.kmp.habittracker.domain.usecase

import com.nagopy.kmp.habittracker.domain.model.Habit
import com.nagopy.kmp.habittracker.domain.model.FrequencyType
import com.nagopy.kmp.habittracker.domain.repository.HabitRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.test.Test

class UpdateHabitUseCaseTest {

    @Test
    fun `invoke should update habit in repository`() = runTest {
        // Given
        val mockRepository = mockk<HabitRepository>()
        val habit = Habit(
            id = 1L,
            name = "Updated Morning Workout",
            description = "45 minutes of exercise",
            color = "#FF5722",
            isActive = true,
            createdAt = LocalDate.parse("2024-01-01"),
            frequencyType = FrequencyType.ONCE_DAILY,
            intervalHours = 24,
            scheduledTimes = listOf(LocalTime(7, 30))
        )
        coEvery { mockRepository.updateHabit(habit) } just runs
        val useCase = UpdateHabitUseCase(mockRepository)

        // When
        useCase(habit)

        // Then
        coVerify(exactly = 1) { mockRepository.updateHabit(habit) }
    }

    @Test
    fun `invoke should call repository with correct habit data`() = runTest {
        // Given
        val mockRepository = mockk<HabitRepository>()
        val habit = Habit(
            id = 2L,
            name = "Updated Reading",
            description = "Read for 45 minutes",
            color = "#2196F3",
            isActive = false,
            createdAt = LocalDate.parse("2024-01-02"),
            frequencyType = FrequencyType.HOURLY,
            intervalHours = 2,
            scheduledTimes = listOf(LocalTime(20, 0))
        )
        coEvery { mockRepository.updateHabit(habit) } just runs
        val useCase = UpdateHabitUseCase(mockRepository)

        // When
        useCase(habit)

        // Then
        coVerify(exactly = 1) { mockRepository.updateHabit(habit) }
    }
}