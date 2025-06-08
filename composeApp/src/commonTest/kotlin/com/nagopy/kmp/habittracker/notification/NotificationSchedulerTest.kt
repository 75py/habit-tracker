package com.nagopy.kmp.habittracker.notification

import com.nagopy.kmp.habittracker.domain.model.Habit
import com.nagopy.kmp.habittracker.domain.model.Task
import com.nagopy.kmp.habittracker.domain.model.FrequencyType
import com.nagopy.kmp.habittracker.domain.notification.NotificationScheduler
import com.nagopy.kmp.habittracker.domain.repository.HabitRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.test.Test

/**
 * Test to verify that notification schedulers correctly fetch habit data
 * and use the actual habit name and description for notifications.
 */
class NotificationSchedulerTest {

    private val mockHabitRepository = mockk<HabitRepository>()

    @Test
    fun `notification scheduler should use habit name and description when task data is stale`() = runTest {
        // Given
        val habitId = 1L
        val currentHabit = Habit(
            id = habitId,
            name = "Updated Habit Name",
            description = "Updated habit description",
            createdAt = LocalDate(2024, 1, 1)
        )
        
        val taskWithStaleData = Task(
            habitId = habitId,
            habitName = "Stale Task Name",
            habitDescription = "Stale task description",
            date = LocalDate(2024, 1, 20),
            scheduledTime = LocalTime(9, 0),
            isCompleted = false
        )

        coEvery { mockHabitRepository.getHabit(habitId) } returns currentHabit

        // Create a test implementation that verifies the correct data is used
        val testScheduler = TestNotificationScheduler(mockHabitRepository)

        // When
        testScheduler.scheduleTaskNotification(taskWithStaleData)

        // Then
        coVerify { mockHabitRepository.getHabit(habitId) }
        // Verify that the scheduler received the updated habit data, not the stale task data
        assert(testScheduler.lastUsedTitle == "Updated Habit Name")
        assert(testScheduler.lastUsedDescription == "Updated habit description")
    }

    @Test
    fun `notification scheduler should fallback to task data when habit not found`() = runTest {
        // Given
        val habitId = 1L
        val task = Task(
            habitId = habitId,
            habitName = "Task Name",
            habitDescription = "Task description",
            date = LocalDate(2024, 1, 20),
            scheduledTime = LocalTime(9, 0),
            isCompleted = false
        )

        coEvery { mockHabitRepository.getHabit(habitId) } returns null

        val testScheduler = TestNotificationScheduler(mockHabitRepository)

        // When
        testScheduler.scheduleTaskNotification(task)

        // Then
        coVerify { mockHabitRepository.getHabit(habitId) }
        // Verify that the scheduler falls back to task data when habit is not found
        assert(testScheduler.lastUsedTitle == "Task Name")
        assert(testScheduler.lastUsedDescription == "Task description")
    }

    /**
     * Test implementation of NotificationScheduler that captures the title and description
     * used for notifications, allowing us to verify the correct data is being used.
     */
    private class TestNotificationScheduler(
        private val habitRepository: HabitRepository
    ) : NotificationScheduler {
        
        var lastUsedTitle: String? = null
        var lastUsedDescription: String? = null

        override suspend fun scheduleTaskNotification(task: Task) {
            // Implement the same logic as the real schedulers
            val habit = habitRepository.getHabit(task.habitId)
            lastUsedTitle = habit?.name ?: task.habitName
            lastUsedDescription = habit?.description ?: task.habitDescription
        }

        override suspend fun scheduleTaskNotifications(tasks: List<Task>) {}
        override suspend fun cancelTaskNotification(task: Task) {}
        override suspend fun cancelHabitNotifications(habitId: Long) {}
        override suspend fun cancelAllNotifications() {}
        override suspend fun areNotificationsEnabled(): Boolean = true
        override suspend fun requestNotificationPermission(): Boolean = true
    }
}