package com.nagopy.kmp.habittracker.domain.usecase

import com.nagopy.kmp.habittracker.domain.model.Task
import com.nagopy.kmp.habittracker.domain.notification.NotificationScheduler
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertTrue

class ManageNotificationsUseCaseTest {

    private val mockNotificationScheduler = mockk<NotificationScheduler>(relaxed = true)
    
    private val manageNotificationsUseCase = ManageNotificationsUseCase(
        notificationScheduler = mockNotificationScheduler
    )

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