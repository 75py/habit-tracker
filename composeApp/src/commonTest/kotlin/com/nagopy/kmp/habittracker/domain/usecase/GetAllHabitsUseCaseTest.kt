package com.nagopy.kmp.habittracker.domain.usecase

import com.nagopy.kmp.habittracker.domain.model.Habit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class GetAllHabitsUseCaseTest {

    @Test
    fun `invoke should return all habits from repository`() = runTest {
        // Given
        val mockRepository = MockHabitRepository()
        val habits = listOf(
            Habit(
                id = 1,
                name = "Exercise",
                description = "Daily workout",
                color = "#FF5722",
                isActive = true,
                createdAt = LocalDate.parse("2024-01-01")
            ),
            Habit(
                id = 2,
                name = "Read",
                description = "Read for 30 minutes",
                color = "#2196F3",
                isActive = false,
                createdAt = LocalDate.parse("2024-01-02")
            )
        )
        mockRepository.setHabits(habits)
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
        val mockRepository = MockHabitRepository()
        val useCase = GetAllHabitsUseCase(mockRepository)

        // When
        val result = useCase().first()

        // Then
        assertEquals(0, result.size)
    }
}