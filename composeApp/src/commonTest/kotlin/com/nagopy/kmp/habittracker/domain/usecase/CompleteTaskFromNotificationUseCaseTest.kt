package com.nagopy.kmp.habittracker.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals

class CompleteTaskFromNotificationUseCaseTest {

    private val mockCompleteTaskUseCase = mockk<CompleteTaskUseCase>()
    
    private val completeTaskFromNotificationUseCase = CompleteTaskFromNotificationUseCase(
        completeTaskUseCase = mockCompleteTaskUseCase
    )

    @Test
    fun `invoke should complete task`() = runTest {
        // Given
        val habitId = 1L
        val date = LocalDate(2024, 1, 20)
        val scheduledTime = LocalTime(9, 0)
        val expectedLogId = 100L

        coEvery { mockCompleteTaskUseCase(habitId, date, scheduledTime) } returns expectedLogId

        // When
        val result = completeTaskFromNotificationUseCase(habitId, date, scheduledTime)

        // Then
        assertEquals(expectedLogId, result)
        coVerify { mockCompleteTaskUseCase(habitId, date, scheduledTime) }
    }
}