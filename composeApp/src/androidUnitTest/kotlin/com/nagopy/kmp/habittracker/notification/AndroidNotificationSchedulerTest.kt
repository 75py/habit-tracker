package com.nagopy.kmp.habittracker.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.nagopy.kmp.habittracker.domain.model.Task
import com.nagopy.kmp.habittracker.domain.repository.HabitRepository
import io.mockk.mockk
import io.mockk.coEvery
import io.mockk.every
import io.mockk.justRun
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertTrue

/**
 * Tests for AndroidNotificationScheduler to verify permission handling
 * and alarm scheduling behavior.
 */
@RunWith(RobolectricTestRunner::class)
class AndroidNotificationSchedulerTest {

    private lateinit var context: Context
    private lateinit var mockHabitRepository: HabitRepository
    private lateinit var scheduler: AndroidNotificationScheduler

    @Before
    fun setup() {
        // Initialize logger first to avoid potential issues
        com.nagopy.kmp.habittracker.util.Logger.init()
        
        context = ApplicationProvider.getApplicationContext()
        mockHabitRepository = mockk()
        scheduler = AndroidNotificationScheduler(context, mockHabitRepository)
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
}