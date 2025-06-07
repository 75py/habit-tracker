package com.nagopy.kmp.habittracker.domain.usecase

import com.nagopy.kmp.habittracker.domain.model.Habit
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AddHabitUseCaseTest {

    @Test
    fun `invoke should add habit to repository and return id`() = runTest {
        // Given
        val mockRepository = MockHabitRepository()
        val useCase = AddHabitUseCase(mockRepository)
        val habit = Habit(
            name = "Morning Workout",
            description = "30 minutes of exercise",
            color = "#FF5722",
            isActive = true,
            createdAt = LocalDate.parse("2024-01-01")
        )

        // When
        val habitId = useCase(habit)

        // Then
        assertTrue(habitId > 0)
        val savedHabit = mockRepository.getHabit(habitId)
        assertEquals("Morning Workout", savedHabit?.name)
        assertEquals("30 minutes of exercise", savedHabit?.description)
        assertEquals("#FF5722", savedHabit?.color)
        assertEquals(true, savedHabit?.isActive)
        assertEquals(LocalDate.parse("2024-01-01"), savedHabit?.createdAt)
    }

    @Test
    fun `invoke should generate unique ids for multiple habits`() = runTest {
        // Given
        val mockRepository = MockHabitRepository()
        val useCase = AddHabitUseCase(mockRepository)
        val habit1 = Habit(
            name = "Habit 1",
            createdAt = LocalDate.parse("2024-01-01")
        )
        val habit2 = Habit(
            name = "Habit 2", 
            createdAt = LocalDate.parse("2024-01-02")
        )

        // When
        val habitId1 = useCase(habit1)
        val habitId2 = useCase(habit2)

        // Then
        assertTrue(habitId1 != habitId2)
        assertTrue(habitId1 > 0)
        assertTrue(habitId2 > 0)
    }
}