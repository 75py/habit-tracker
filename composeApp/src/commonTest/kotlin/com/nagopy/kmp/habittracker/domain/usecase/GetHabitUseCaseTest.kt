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
import kotlin.test.assertNull

class GetHabitUseCaseTest {

    @Test
    fun `invoke should return habit from repository when found`() = runTest {
        // Given
        val mockRepository = mockk<HabitRepository>()
        val habitId = 1L
        val expectedHabit = Habit(
            id = habitId,
            name = "Morning Workout",
            description = "30 minutes of exercise",
            color = "#FF5722",
            isActive = true,
            createdAt = LocalDate.parse("2024-01-01"),
            frequencyType = FrequencyType.ONCE_DAILY,
            intervalHours = 24,
            scheduledTimes = listOf(LocalTime(7, 0))
        )
        coEvery { mockRepository.getHabit(habitId) } returns expectedHabit
        val useCase = GetHabitUseCase(mockRepository)

        // When
        val result = useCase(habitId)

        // Then
        assertEquals(expectedHabit, result)
        coVerify(exactly = 1) { mockRepository.getHabit(habitId) }
    }

    @Test
    fun `invoke should return null when habit not found`() = runTest {
        // Given
        val mockRepository = mockk<HabitRepository>()
        val habitId = 999L
        coEvery { mockRepository.getHabit(habitId) } returns null
        val useCase = GetHabitUseCase(mockRepository)

        // When
        val result = useCase(habitId)

        // Then
        assertNull(result)
        coVerify(exactly = 1) { mockRepository.getHabit(habitId) }
    }
}