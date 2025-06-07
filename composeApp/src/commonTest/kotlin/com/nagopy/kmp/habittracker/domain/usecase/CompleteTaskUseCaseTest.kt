package com.nagopy.kmp.habittracker.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CompleteTaskUseCaseTest {

    @Test
    fun `invoke with habitId should create habit log for today`() = runTest {
        // Given
        val mockRepository = MockHabitRepository()
        val useCase = CompleteTaskUseCase(mockRepository)
        val habitId = 1L

        // When
        val logId = useCase(habitId)

        // Then
        assertTrue(logId > 0)
        
        // Verify the log was created
        val logsList = mockRepository.getHabitLogsForHabit(habitId).first()
        assertEquals(1, logsList.size)
        
        val createdLog = logsList.first()
        assertEquals(habitId, createdLog.habitId)
        assertEquals(true, createdLog.isCompleted)
        // Note: We can't easily test the exact date without mocking the clock
        // but we can verify a log was created
    }

    @Test
    fun `invoke with habitId and date should create habit log for specific date`() = runTest {
        // Given
        val mockRepository = MockHabitRepository()
        val useCase = CompleteTaskUseCase(mockRepository)
        val habitId = 2L
        val specificDate = LocalDate.parse("2024-01-15")

        // When
        val logId = useCase(habitId, specificDate)

        // Then
        assertTrue(logId > 0)
        
        // Verify the log was created with the correct date
        val createdLog = mockRepository.getHabitLog(habitId, specificDate)
        assertNotNull(createdLog)
        assertEquals(habitId, createdLog.habitId)
        assertEquals(specificDate, createdLog.date)
        assertEquals(true, createdLog.isCompleted)
    }

    @Test
    fun `invoke should generate unique ids for multiple completions`() = runTest {
        // Given
        val mockRepository = MockHabitRepository()
        val useCase = CompleteTaskUseCase(mockRepository)
        val habitId = 3L
        val date1 = LocalDate.parse("2024-01-01")
        val date2 = LocalDate.parse("2024-01-02")

        // When
        val logId1 = useCase(habitId, date1)
        val logId2 = useCase(habitId, date2)

        // Then
        assertTrue(logId1 != logId2)
        assertTrue(logId1 > 0)
        assertTrue(logId2 > 0)
        
        // Verify both logs were created
        assertNotNull(mockRepository.getHabitLog(habitId, date1))
        assertNotNull(mockRepository.getHabitLog(habitId, date2))
    }
}