package com.nagopy.kmp.habittracker.domain.usecase

import com.nagopy.kmp.habittracker.domain.model.Habit
import com.nagopy.kmp.habittracker.domain.repository.HabitRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class GetTodayTasksUseCaseTest {

    @Test
    fun `invoke should return only active habits`() = runTest {
        // Given
        val activeHabits = listOf(
            Habit(
                id = 1,
                name = "Exercise",
                description = "Daily workout",
                color = "#FF5722",
                isActive = true,
                createdAt = LocalDate.parse("2024-01-01")
            ),
            Habit(
                id = 3,
                name = "Meditate",
                description = "10 minutes meditation",
                color = "#4CAF50",
                isActive = true,
                createdAt = LocalDate.parse("2024-01-03")
            )
        )
        val mockRepository = mockk<HabitRepository>()
        every { mockRepository.getActiveHabits() } returns flowOf(activeHabits)
        val useCase = GetTodayTasksUseCase(mockRepository)

        // When
        val result = useCase().first()

        // Then
        assertEquals(2, result.size)
        assertEquals("Exercise", result[0].name)
        assertEquals("Meditate", result[1].name)
        // Verify all returned habits are active
        result.forEach { habit ->
            assertEquals(true, habit.isActive)
        }
    }

    @Test
    fun `invoke should return empty list when no active habits exist`() = runTest {
        // Given
        val mockRepository = mockk<HabitRepository>()
        every { mockRepository.getActiveHabits() } returns flowOf(emptyList())
        val useCase = GetTodayTasksUseCase(mockRepository)

        // When
        val result = useCase().first()

        // Then
        assertEquals(0, result.size)
    }

    @Test
    fun `invoke should return empty list when no habits exist`() = runTest {
        // Given
        val mockRepository = mockk<HabitRepository>()
        every { mockRepository.getActiveHabits() } returns flowOf(emptyList())
        val useCase = GetTodayTasksUseCase(mockRepository)

        // When
        val result = useCase().first()

        // Then
        assertEquals(0, result.size)
    }
}