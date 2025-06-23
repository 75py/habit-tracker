package com.nagopy.kmp.habittracker.integration

import com.nagopy.kmp.habittracker.domain.model.Habit
import com.nagopy.kmp.habittracker.domain.model.FrequencyType
import com.nagopy.kmp.habittracker.domain.model.HabitIntervalValidator
import com.nagopy.kmp.habittracker.domain.usecase.AddHabitUseCase
import com.nagopy.kmp.habittracker.domain.usecase.UpdateHabitUseCase
import com.nagopy.kmp.habittracker.domain.usecase.GetHabitUseCase
import com.nagopy.kmp.habittracker.domain.usecase.ManageNotificationsUseCase
import com.nagopy.kmp.habittracker.domain.usecase.ScheduleNextNotificationUseCase
import com.nagopy.kmp.habittracker.presentation.habitedit.HabitEditViewModel
import com.nagopy.kmp.habittracker.presentation.habitedit.HabitEditUiState
import com.nagopy.kmp.habittracker.presentation.habitedit.TimeUnit
import io.mockk.mockk
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import com.nagopy.kmp.habittracker.domain.model.frequencyType
import com.nagopy.kmp.habittracker.domain.model.intervalMinutes
import com.nagopy.kmp.habittracker.domain.model.scheduledTimes
import com.nagopy.kmp.habittracker.domain.model.startTime
import com.nagopy.kmp.habittracker.domain.model.endTime

class IntervalValidationIntegrationTest {

    @Test
    fun `complete workflow - INTERVAL type should be restricted to valid divisors of 60`() {
        // Verify all expected valid divisors
        val expectedValidValues = listOf(1, 2, 3, 4, 5, 6, 10, 12, 15, 20, 30, 60)
        assertEquals(expectedValidValues, HabitIntervalValidator.VALID_SUB_HOUR_INTERVAL_MINUTES)
        
        // Test domain model validation
        expectedValidValues.forEach { validValue ->
            val habit = Habit(
                id = 1L,
                name = "Test Habit",
                description = "Test",
                createdAt = LocalDate.parse("2024-01-01"),
                frequencyType = FrequencyType.INTERVAL,
                intervalMinutes = validValue
            )
            assertEquals(validValue, habit.intervalMinutes)
        }
        
        // Test domain model rejection of invalid values
        val invalidValues = listOf(7, 8, 9, 11, 13, 14, 16, 17, 18, 19, 21, 25, 45, 90, 120)
        invalidValues.forEach { invalidValue ->
            assertFailsWith<IllegalArgumentException> {
                Habit(
                    id = 1L,
                    name = "Test Habit",
                    description = "Test",
                    createdAt = LocalDate.parse("2024-01-01"),
                    frequencyType = FrequencyType.INTERVAL,
                    intervalMinutes = invalidValue
                )
            }
        }
    }

    @Test
    fun `ViewModel should auto-correct invalid values for all frequency types`() {
        val mockAddHabitUseCase = mockk<AddHabitUseCase>(relaxed = true)
        val mockUpdateHabitUseCase = mockk<UpdateHabitUseCase>(relaxed = true)
        val mockGetHabitUseCase = mockk<GetHabitUseCase>(relaxed = true)
        val mockManageNotificationsUseCase = mockk<ManageNotificationsUseCase>(relaxed = true)
        val mockScheduleNextNotificationUseCase = mockk<ScheduleNextNotificationUseCase>(relaxed = true)
        
        val viewModel = HabitEditViewModel(
            addHabitUseCase = mockAddHabitUseCase,
            updateHabitUseCase = mockUpdateHabitUseCase,
            getHabitUseCase = mockGetHabitUseCase,
            manageNotificationsUseCase = mockManageNotificationsUseCase,
            scheduleNextNotificationUseCase = mockScheduleNextNotificationUseCase
        )
        
        // Test INTERVAL frequency auto-correction
        viewModel.updateFrequencyType(FrequencyType.INTERVAL)
        viewModel.updateIntervalValue(7, TimeUnit.MINUTES)
        val intervalState = viewModel.uiState.value
        assertTrue(intervalState is HabitEditUiState.Content)
        assertTrue(
            HabitIntervalValidator.isValidIntervalMinutes(FrequencyType.INTERVAL, intervalState.intervalMinutes),
            "Expected ${intervalState.intervalMinutes} to be a valid divisor of 60"
        )
        
        // Test HOURLY frequency auto-correction
        viewModel.updateFrequencyType(FrequencyType.INTERVAL)
        viewModel.updateIntervalValue(45, TimeUnit.MINUTES) // Should be corrected to 60
        val hourlyState = viewModel.uiState.value
        assertTrue(hourlyState is HabitEditUiState.Content)
        assertTrue(
            HabitIntervalValidator.isValidIntervalMinutes(FrequencyType.INTERVAL, hourlyState.intervalMinutes),
            "Expected ${hourlyState.intervalMinutes} to be a multiple of 60"
        )
        assertEquals(60, hourlyState.intervalMinutes)
        
        // Test ONCE_DAILY frequency auto-correction
        viewModel.updateFrequencyType(FrequencyType.ONCE_DAILY)
        viewModel.updateIntervalValue(12, TimeUnit.HOURS) // Should be corrected to 1440
        val dailyState = viewModel.uiState.value
        assertTrue(dailyState is HabitEditUiState.Content)
        assertTrue(
            HabitIntervalValidator.isValidIntervalMinutes(FrequencyType.ONCE_DAILY, dailyState.intervalMinutes),
            "Expected ${dailyState.intervalMinutes} to be exactly 1440"
        )
        assertEquals(1440, dailyState.intervalMinutes)
    }

    @Test
    fun `frequency type validation should work correctly for all types`() {
        // Test ONCE_DAILY - only 1440 is valid
        val habit1 = Habit(
            id = 1L,
            name = "Test Daily",
            description = "Test",
            createdAt = LocalDate.parse("2024-01-01"),
            frequencyType = FrequencyType.ONCE_DAILY,
            intervalMinutes = 1440 // Valid for ONCE_DAILY
        )
        assertEquals(1440, habit1.intervalMinutes)

        // Test HOURLY - multiples of 60 are valid
        val habit2 = Habit(
            id = 2L,
            name = "Test Hourly",
            description = "Test",
            createdAt = LocalDate.parse("2024-01-01"),
            frequencyType = FrequencyType.INTERVAL,
            intervalMinutes = 120 // Valid for HOURLY (2 hours)
        )
        assertEquals(120, habit2.intervalMinutes)
        
        // Test INTERVAL - divisors of 60 are valid
        val habit3 = Habit(
            id = 3L,
            name = "Test Interval",
            description = "Test",
            createdAt = LocalDate.parse("2024-01-01"),
            frequencyType = FrequencyType.INTERVAL,
            intervalMinutes = 30 // Valid for INTERVAL
        )
        assertEquals(30, habit3.intervalMinutes)
        
        // Test invalid values
        assertFailsWith<IllegalArgumentException> {
            Habit(
                id = 4L,
                name = "Invalid Daily",
                description = "Test",
                createdAt = LocalDate.parse("2024-01-01"),
                frequencyType = FrequencyType.ONCE_DAILY,
                intervalMinutes = 720 // Invalid for ONCE_DAILY
            )
        }
        
        assertFailsWith<IllegalArgumentException> {
            Habit(
                id = 5L,
                name = "Invalid Hourly",
                description = "Test",
                createdAt = LocalDate.parse("2024-01-01"),
                frequencyType = FrequencyType.INTERVAL,
                intervalMinutes = 90 // Invalid for HOURLY (not multiple of 60)
            )
        }
        
        assertFailsWith<IllegalArgumentException> {
            Habit(
                id = 6L,
                name = "Invalid Interval",
                description = "Test",
                createdAt = LocalDate.parse("2024-01-01"),
                frequencyType = FrequencyType.INTERVAL,
                intervalMinutes = 90 // Invalid for INTERVAL (not divisor of 60)
            )
        }
    }
}