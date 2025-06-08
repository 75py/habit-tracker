package com.nagopy.kmp.habittracker.domain.usecase

import com.nagopy.kmp.habittracker.domain.model.Task
import com.nagopy.kmp.habittracker.domain.notification.NotificationScheduler
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertTrue

class ManageNotificationsUseCaseTest {

    private val mockNotificationScheduler = mockk<NotificationScheduler>(relaxed = true)
    private val mockGetTodayTasksUseCase = mockk<GetTodayTasksUseCase>()
    
    private val manageNotificationsUseCase = ManageNotificationsUseCase(
        notificationScheduler = mockNotificationScheduler,
        getTodayTasksUseCase = mockGetTodayTasksUseCase
    )

    @Test
    fun `scheduleNotificationsForTodayTasks should schedule only pending tasks`() = runTest {
        // Given
        val completedTask = Task(
            habitId = 1L,
            habitName = "Completed Task",
            date = LocalDate(2024, 1, 20),
            scheduledTime = LocalTime(9, 0),
            isCompleted = true
        )
        val pendingTask = Task(
            habitId = 2L,
            habitName = "Pending Task",
            date = LocalDate(2024, 1, 20),
            scheduledTime = LocalTime(10, 0),
            isCompleted = false
        )
        val tasks = listOf(completedTask, pendingTask)
        
        coEvery { mockGetTodayTasksUseCase() } returns flowOf(tasks)

        // When
        manageNotificationsUseCase.scheduleNotificationsForTodayTasks()

        // Then
        coVerify { mockNotificationScheduler.scheduleTaskNotifications(listOf(pendingTask)) }
    }

    @Test
    fun `scheduleTaskNotification should schedule only if task is not completed`() = runTest {
        // Given
        val pendingTask = Task(
            habitId = 1L,
            habitName = "Pending Task",
            date = LocalDate(2024, 1, 20),
            scheduledTime = LocalTime(9, 0),
            isCompleted = false
        )

        // When
        manageNotificationsUseCase.scheduleTaskNotification(pendingTask)

        // Then
        coVerify { mockNotificationScheduler.scheduleTaskNotification(pendingTask) }
    }

    @Test
    fun `scheduleTaskNotification should not schedule if task is completed`() = runTest {
        // Given
        val completedTask = Task(
            habitId = 1L,
            habitName = "Completed Task",
            date = LocalDate(2024, 1, 20),
            scheduledTime = LocalTime(9, 0),
            isCompleted = true
        )

        // When
        manageNotificationsUseCase.scheduleTaskNotification(completedTask)

        // Then
        coVerify(exactly = 0) { mockNotificationScheduler.scheduleTaskNotification(any()) }
    }

    @Test
    fun `cancelTaskNotification should delegate to notification scheduler`() = runTest {
        // Given
        val task = Task(
            habitId = 1L,
            habitName = "Task",
            date = LocalDate(2024, 1, 20),
            scheduledTime = LocalTime(9, 0),
            isCompleted = false
        )

        // When
        manageNotificationsUseCase.cancelTaskNotification(task)

        // Then
        coVerify { mockNotificationScheduler.cancelTaskNotification(task) }
    }

    @Test
    fun `cancelHabitNotifications should delegate to notification scheduler`() = runTest {
        // Given
        val habitId = 1L

        // When
        manageNotificationsUseCase.cancelHabitNotifications(habitId)

        // Then
        coVerify { mockNotificationScheduler.cancelHabitNotifications(habitId) }
    }

    @Test
    fun `ensureNotificationsEnabled should return true when notifications are enabled`() = runTest {
        // Given
        coEvery { mockNotificationScheduler.areNotificationsEnabled() } returns true

        // When
        val result = manageNotificationsUseCase.ensureNotificationsEnabled()

        // Then
        assertTrue(result)
        coVerify { mockNotificationScheduler.areNotificationsEnabled() }
        coVerify(exactly = 0) { mockNotificationScheduler.requestNotificationPermission() }
    }

    @Test
    fun `ensureNotificationsEnabled should request permission when notifications are disabled`() = runTest {
        // Given
        coEvery { mockNotificationScheduler.areNotificationsEnabled() } returns false
        coEvery { mockNotificationScheduler.requestNotificationPermission() } returns true

        // When
        val result = manageNotificationsUseCase.ensureNotificationsEnabled()

        // Then
        assertTrue(result)
        coVerify { mockNotificationScheduler.areNotificationsEnabled() }
        coVerify { mockNotificationScheduler.requestNotificationPermission() }
    }
}