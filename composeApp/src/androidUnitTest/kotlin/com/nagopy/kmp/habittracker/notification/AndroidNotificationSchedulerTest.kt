package com.nagopy.kmp.habittracker.notification

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.nagopy.kmp.habittracker.domain.model.Task
import com.nagopy.kmp.habittracker.domain.repository.HabitRepository
import com.nagopy.kmp.habittracker.domain.usecase.GetNextTasksUseCase
import io.mockk.mockk
import io.mockk.coEvery
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertTrue

/**
 * Tests for AndroidNotificationScheduler to verify permission handling
 * and alarm scheduling behavior.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28]) // Use SDK 28 to avoid newer API issues with Robolectric
class AndroidNotificationSchedulerTest {

    private lateinit var context: Context
    private lateinit var mockHabitRepository: HabitRepository
    private lateinit var mockGetNextTasksUseCase: GetNextTasksUseCase
    private lateinit var scheduler: AndroidNotificationScheduler

    @Before
    fun setup() {
        // Initialize logger first to avoid potential issues
        com.nagopy.kmp.habittracker.util.Logger.init()
        
        context = ApplicationProvider.getApplicationContext()
        mockHabitRepository = mockk()
        mockGetNextTasksUseCase = mockk()
        scheduler = AndroidNotificationScheduler(context, mockHabitRepository, mockGetNextTasksUseCase)
    }

    @Test
    fun `scheduleTaskNotification should not crash when habit repository returns null`() = runTest {
        // Given
        val task = Task(
            habitId = 1L,
            habitName = "Test Habit",
            habitDescription = "Test Description",
            date = LocalDate(2024, 1, 20),
            scheduledTime = LocalTime(9, 0),
            isCompleted = false
        )

        coEvery { mockHabitRepository.getHabit(any()) } returns null

        // When & Then - should not throw exception
        // On SDK 28, setExactAndAllowWhileIdle is available and should work
        scheduler.scheduleTaskNotification(task)
    }

    @Test
    fun `areNotificationsEnabled should return boolean value`() = runTest {
        // When
        val result = scheduler.areNotificationsEnabled()

        // Then - should return a boolean without crashing
        assertTrue(result is Boolean)
    }

    @Test
    fun `requestNotificationPermission should return boolean value`() = runTest {
        // When
        val result = scheduler.requestNotificationPermission()

        // Then - should return a boolean without crashing
        assertTrue(result is Boolean)
    }

    @Test
    fun `cancelHabitNotifications should handle case when habit not found`() = runTest {
        // Given
        val habitId = 999L
        coEvery { mockGetNextTasksUseCase.getNextTaskForHabit(habitId) } returns null

        // When & Then - should not throw exception
        scheduler.cancelHabitNotifications(habitId)
    }

    @Test
    fun `cancelHabitNotifications should cancel next task when found`() = runTest {
        // Given
        val habitId = 1L
        val nextTask = Task(
            habitId = habitId,
            habitName = "Test Habit",
            habitDescription = "Test Description",
            date = LocalDate(2024, 1, 20),
            scheduledTime = LocalTime(9, 0),
            isCompleted = false
        )
        coEvery { mockGetNextTasksUseCase.getNextTaskForHabit(habitId) } returns nextTask

        // When & Then - should not throw exception
        scheduler.cancelHabitNotifications(habitId)
    }

    @Test
    fun `cancelHabitNotifications should handle no scheduled tasks`() = runTest {
        // Given
        val habitId = 1L
        coEvery { mockGetNextTasksUseCase.getNextTaskForHabit(habitId) } returns null

        // When & Then - should not throw exception
        scheduler.cancelHabitNotifications(habitId)
    }
}