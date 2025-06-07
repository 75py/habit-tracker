package com.nagopy.kmp.habittracker.domain.usecase

import com.nagopy.kmp.habittracker.domain.model.HabitLog
import com.nagopy.kmp.habittracker.domain.repository.HabitRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CompleteTaskUseCaseTest {

    @Test
    fun `invoke with habitId should create habit log for today`() = runTest {
        // Given
        val mockRepository = mockk<HabitRepository>()
        val habitId = 1L
        val expectedLogId = 100L
        val logSlot = slot<HabitLog>()
        
        // Use a fixed date to make the test deterministic
        val fixedDate = LocalDate.parse("2024-01-20")
        val fixedInstant = fixedDate.atStartOfDayIn(TimeZone.currentSystemDefault())
        val fixedClock = object : Clock {
            override fun now(): Instant = fixedInstant
        }
        
        coEvery { mockRepository.addHabitLog(capture(logSlot)) } returns expectedLogId
        val useCase = CompleteTaskUseCase(mockRepository, fixedClock)

        // When
        val logId = useCase(habitId)

        // Then
        assertEquals(expectedLogId, logId)
        coVerify(exactly = 1) { mockRepository.addHabitLog(any()) }
        
        // Verify the log has correct properties
        val capturedLog = logSlot.captured
        assertEquals(habitId, capturedLog.habitId)
        assertEquals(true, capturedLog.isCompleted)
        // Date should be the fixed date we set in the clock
        assertEquals(fixedDate, capturedLog.date)
    }

    @Test
    fun `invoke with habitId and date should create habit log for specific date`() = runTest {
        // Given
        val mockRepository = mockk<HabitRepository>()
        val habitId = 2L
        val specificDate = LocalDate.parse("2024-01-15")
        val expectedLogId = 200L
        val logSlot = slot<HabitLog>()
        
        coEvery { mockRepository.addHabitLog(capture(logSlot)) } returns expectedLogId
        val useCase = CompleteTaskUseCase(mockRepository)

        // When
        val logId = useCase(habitId, specificDate)

        // Then
        assertEquals(expectedLogId, logId)
        coVerify(exactly = 1) { mockRepository.addHabitLog(any()) }
        
        // Verify the log has correct properties
        val capturedLog = logSlot.captured
        assertEquals(habitId, capturedLog.habitId)
        assertEquals(specificDate, capturedLog.date)
        assertEquals(true, capturedLog.isCompleted)
    }

    @Test
    fun `invoke should call repository with correct habit log data`() = runTest {
        // Given
        val mockRepository = mockk<HabitRepository>()
        val habitId = 3L
        val date = LocalDate.parse("2024-01-10")
        val logSlot = slot<HabitLog>()
        
        coEvery { mockRepository.addHabitLog(capture(logSlot)) } returns 300L
        val useCase = CompleteTaskUseCase(mockRepository)

        // When
        useCase(habitId, date)

        // Then
        coVerify(exactly = 1) { mockRepository.addHabitLog(any()) }
        
        val capturedLog = logSlot.captured
        assertEquals(habitId, capturedLog.habitId)
        assertEquals(date, capturedLog.date)
        assertEquals(true, capturedLog.isCompleted)
    }
}