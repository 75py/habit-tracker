package com.nagopy.kmp.habittracker.domain.usecase

import com.nagopy.kmp.habittracker.domain.model.Habit
import com.nagopy.kmp.habittracker.domain.model.FrequencyType
import com.nagopy.kmp.habittracker.domain.repository.HabitRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals

class GetAllHabitsUseCaseTest {

    @Test
    fun `invoke should return all habits from repository`() = runTest {
        // Given
        val habits = listOf(
            Habit(
                id = 1,
                name = "Exercise",
                description = "Daily workout",
                color = "#FF5722",
                isActive = true,
                createdAt = LocalDate.parse("2024-01-01"),
                frequencyType = FrequencyType.ONCE_DAILY,
                intervalHours = 24,
                scheduledTimes = listOf(LocalTime(7, 0))
            ),
            Habit(
                id = 2,
                name = "Read",
                description = "Read for 30 minutes",
                color = "#2196F3",
                isActive = false,
                createdAt = LocalDate.parse("2024-01-02"),
                frequencyType = FrequencyType.ONCE_DAILY,
                intervalHours = 24,
                scheduledTimes = listOf(LocalTime(20, 0))
            )
        )
        val mockRepository = mockk<HabitRepository>()
        every { mockRepository.getAllHabits() } returns flowOf(habits)
        val useCase = GetAllHabitsUseCase(mockRepository)

        // When
        val result = useCase().first()

        // Then
        assertEquals(2, result.size)
        assertEquals("Exercise", result[0].name)
        assertEquals("Read", result[1].name)
    }

    @Test
    fun `invoke should return empty list when no habits exist`() = runTest {
        // Given
        val mockRepository = mockk<HabitRepository>()
        every { mockRepository.getAllHabits() } returns flowOf(emptyList())
        val useCase = GetAllHabitsUseCase(mockRepository)

        // When
        val result = useCase().first()

        // Then
        assertEquals(0, result.size)
    }
}