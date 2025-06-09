package com.nagopy.kmp.habittracker.domain.usecase

import com.nagopy.kmp.habittracker.domain.model.Task
import com.nagopy.kmp.habittracker.domain.model.Habit
import com.nagopy.kmp.habittracker.domain.model.FrequencyType
import com.nagopy.kmp.habittracker.domain.notification.NotificationScheduler
import com.nagopy.kmp.habittracker.domain.repository.HabitRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ScheduleNextNotificationUseCaseTest {

    private val mockNotificationScheduler = mockk<NotificationScheduler>(relaxed = true)
    private val mockGetNextTasksUseCase = mockk<GetNextTasksUseCase>()
    private val mockHabitRepository = mockk<HabitRepository>()
    
    private val scheduleNextNotificationUseCase = ScheduleNextNotificationUseCase(
        notificationScheduler = mockNotificationScheduler,
        getNextTasksUseCase = mockGetNextTasksUseCase,
        habitRepository = mockHabitRepository
    )

    @Test
    fun `scheduleNextNotificationForHabit should schedule task when available and notifications enabled`() = runTest {
        // Given
        val habitId = 1L
        val nextTask = Task(
            habitId = habitId,
            habitName = "Test Habit",
            date = LocalDate(2024, 1, 20),
            scheduledTime = LocalTime(14, 0),
            isCompleted = false
        )
        
        coEvery { mockNotificationScheduler.areNotificationsEnabled() } returns true
        coEvery { mockGetNextTasksUseCase.getNextTaskForHabit(habitId) } returns nextTask

        // When
        val result = scheduleNextNotificationUseCase.scheduleNextNotificationForHabit(habitId)

        // Then
        assertTrue(result)
        coVerify { mockNotificationScheduler.scheduleTaskNotification(nextTask) }
    }

    @Test
    fun `scheduleNextNotificationForHabit should return false when no next task available`() = runTest {
        // Given
        val habitId = 1L
        
        coEvery { mockNotificationScheduler.areNotificationsEnabled() } returns true
        coEvery { mockGetNextTasksUseCase.getNextTaskForHabit(habitId) } returns null

        // When
        val result = scheduleNextNotificationUseCase.scheduleNextNotificationForHabit(habitId)

        // Then
        assertFalse(result)
        coVerify(exactly = 0) { mockNotificationScheduler.scheduleTaskNotification(any()) }
    }

    @Test
    fun `scheduleNextNotificationForHabit should return false when notifications disabled`() = runTest {
        // Given
        val habitId = 1L
        
        coEvery { mockNotificationScheduler.areNotificationsEnabled() } returns false

        // When
        val result = scheduleNextNotificationUseCase.scheduleNextNotificationForHabit(habitId)

        // Then
        assertFalse(result)
        coVerify(exactly = 0) { mockGetNextTasksUseCase.getNextTaskForHabit(any()) }
        coVerify(exactly = 0) { mockNotificationScheduler.scheduleTaskNotification(any()) }
    }

    @Test
    fun `scheduleNextUpcomingNotification should schedule earliest task when available`() = runTest {
        // Given
        val nextTask = Task(
            habitId = 1L,
            habitName = "Test Habit",
            date = LocalDate(2024, 1, 20),
            scheduledTime = LocalTime(14, 0),
            isCompleted = false
        )
        
        coEvery { mockNotificationScheduler.areNotificationsEnabled() } returns true
        coEvery { mockGetNextTasksUseCase.getNextUpcomingTask() } returns nextTask

        // When
        val result = scheduleNextNotificationUseCase.scheduleNextUpcomingNotification()

        // Then
        assertTrue(result)
        coVerify { mockNotificationScheduler.scheduleTaskNotification(nextTask) }
    }

    @Test
    fun `rescheduleAllHabitNotifications should cancel all and reschedule for each habit`() = runTest {
        // Given
        val habits = listOf(
            Habit(
                id = 1L,
                name = "Habit 1",
                description = "Test",
                color = "#FF0000",
                isActive = true,
                createdAt = LocalDate(2024, 1, 1),
                frequencyType = FrequencyType.ONCE_DAILY,
                intervalMinutes = 1440,
                scheduledTimes = listOf(LocalTime(9, 0))
            ),
            Habit(
                id = 2L,
                name = "Habit 2",
                description = "Test",
                color = "#00FF00",
                isActive = true,
                createdAt = LocalDate(2024, 1, 1),
                frequencyType = FrequencyType.ONCE_DAILY,
                intervalMinutes = 1440,
                scheduledTimes = listOf(LocalTime(10, 0))
            )
        )
        
        coEvery { mockNotificationScheduler.areNotificationsEnabled() } returns true
        coEvery { mockHabitRepository.getActiveHabits() } returns flowOf(habits)
        coEvery { mockGetNextTasksUseCase.getNextTaskForHabit(any()) } returns createMockTask(1L)

        // When
        scheduleNextNotificationUseCase.rescheduleAllHabitNotifications()

        // Then
        coVerify { mockNotificationScheduler.cancelAllNotifications() }
        coVerify { mockGetNextTasksUseCase.getNextTaskForHabit(1L) }
        coVerify { mockGetNextTasksUseCase.getNextTaskForHabit(2L) }
    }

    @Test
    fun `rescheduleAllHabitNotifications should not schedule when notifications disabled`() = runTest {
        // Given
        coEvery { mockNotificationScheduler.areNotificationsEnabled() } returns false

        // When
        scheduleNextNotificationUseCase.rescheduleAllHabitNotifications()

        // Then
        coVerify(exactly = 0) { mockNotificationScheduler.cancelAllNotifications() }
        coVerify(exactly = 0) { mockHabitRepository.getActiveHabits() }
    }

    private fun createMockTask(habitId: Long) = Task(
        habitId = habitId,
        habitName = "Test Habit",
        date = LocalDate(2024, 1, 20),
        scheduledTime = LocalTime(14, 0),
        isCompleted = false
    )
}