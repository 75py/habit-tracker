package com.nagopy.kmp.habittracker.domain.usecase

import com.nagopy.kmp.habittracker.domain.notification.NotificationScheduler
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
    private val mockNotificationScheduler = mockk<NotificationScheduler>(relaxed = true)
    private val mockScheduleNextNotificationUseCase = mockk<ScheduleNextNotificationUseCase>(relaxed = true)
    
    private val completeTaskFromNotificationUseCase = CompleteTaskFromNotificationUseCase(
        completeTaskUseCase = mockCompleteTaskUseCase,
        notificationScheduler = mockNotificationScheduler,
        scheduleNextNotificationUseCase = mockScheduleNextNotificationUseCase
    )

    @Test
    fun `invoke should complete task cancel notification and schedule next notification`() = runTest {
        // Given
        val habitId = 1L
        val date = LocalDate(2024, 1, 20)
        val scheduledTime = LocalTime(9, 0)
        val expectedLogId = 100L

        coEvery { mockCompleteTaskUseCase(habitId, date, scheduledTime) } returns expectedLogId
        coEvery { mockScheduleNextNotificationUseCase.scheduleNextNotificationForHabit(habitId) } returns true

        // When
        val result = completeTaskFromNotificationUseCase(habitId, date, scheduledTime)

        // Then
        assertEquals(expectedLogId, result)
        coVerify { mockCompleteTaskUseCase(habitId, date, scheduledTime) }
        coVerify { 
            mockNotificationScheduler.cancelTaskNotification(
                match { task ->
                    task.habitId == habitId &&
                    task.date == date &&
                    task.scheduledTime == scheduledTime &&
                    task.isCompleted == true
                }
            )
        }
        coVerify { mockScheduleNextNotificationUseCase.scheduleNextNotificationForHabit(habitId) }
    }

    @Test
    fun `invoke should continue even if scheduling next notification fails`() = runTest {
        // Given
        val habitId = 1L
        val date = LocalDate(2024, 1, 20)
        val scheduledTime = LocalTime(9, 0)
        val expectedLogId = 100L

        coEvery { mockCompleteTaskUseCase(habitId, date, scheduledTime) } returns expectedLogId
        coEvery { mockScheduleNextNotificationUseCase.scheduleNextNotificationForHabit(habitId) } throws RuntimeException("Scheduling failed")

        // When
        val result = completeTaskFromNotificationUseCase(habitId, date, scheduledTime)

        // Then - should still complete successfully despite scheduling failure
        assertEquals(expectedLogId, result)
        coVerify { mockCompleteTaskUseCase(habitId, date, scheduledTime) }
        coVerify { 
            mockNotificationScheduler.cancelTaskNotification(
                match { task ->
                    task.habitId == habitId &&
                    task.date == date &&
                    task.scheduledTime == scheduledTime &&
                    task.isCompleted == true
                }
            )
        }
        coVerify { mockScheduleNextNotificationUseCase.scheduleNextNotificationForHabit(habitId) }
    }
}