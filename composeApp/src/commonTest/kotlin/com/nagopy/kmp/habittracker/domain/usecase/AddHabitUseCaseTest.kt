package com.nagopy.kmp.habittracker.domain.usecase

import com.nagopy.kmp.habittracker.domain.model.Habit
import com.nagopy.kmp.habittracker.domain.model.FrequencyType
import com.nagopy.kmp.habittracker.domain.repository.HabitRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals

class AddHabitUseCaseTest {

    @Test
    fun `invoke should add habit to repository and return id`() = runTest {
        // Given
        val mockRepository = mockk<HabitRepository>()
        val expectedId = 1L
        val habit = Habit(
            name = "Morning Workout",
            description = "30 minutes of exercise",
            color = "#FF5722",
            isActive = true,
            createdAt = LocalDate.parse("2024-01-01"),
            frequencyType = FrequencyType.ONCE_DAILY,
            intervalMinutes = 1440,
            scheduledTimes = listOf(LocalTime(7, 0))
        )
        coEvery { mockRepository.createHabit(habit) } returns expectedId
        val useCase = AddHabitUseCase(mockRepository)

        // When
        val habitId = useCase(habit)

        // Then
        assertEquals(expectedId, habitId)
        coVerify(exactly = 1) { mockRepository.createHabit(habit) }
    }

    @Test
    fun `invoke should call repository with correct habit data`() = runTest {
        // Given
        val mockRepository = mockk<HabitRepository>()
        val habit = Habit(
            name = "Reading",
            description = "Read for 30 minutes",
            color = "#2196F3",
            isActive = true,
            createdAt = LocalDate.parse("2024-01-02"),
            frequencyType = FrequencyType.ONCE_DAILY,
            intervalMinutes = 1440,
            scheduledTimes = listOf(LocalTime(20, 30))
        )
        coEvery { mockRepository.createHabit(habit) } returns 2L
        val useCase = AddHabitUseCase(mockRepository)

        // When
        useCase(habit)

        // Then
        coVerify(exactly = 1) { mockRepository.createHabit(habit) }
    }
}