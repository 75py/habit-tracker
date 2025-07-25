package com.nagopy.kmp.habittracker.integration

import com.nagopy.kmp.habittracker.domain.model.Habit
import com.nagopy.kmp.habittracker.domain.model.FrequencyType
import com.nagopy.kmp.habittracker.domain.model.HabitDetail
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
import kotlinx.datetime.LocalTime

class IntervalValidationIntegrationTest {

    @Test
    fun `complete workflow - INTERVAL type should be restricted to valid divisors of 60`() {
        // Verify all expected valid divisors
        val expectedValidValues = listOf(1, 2, 3, 4, 5, 6, 10, 12, 15, 20, 30, 60, 120, 180, 240, 300, 360, 420, 480, 540, 600, 660, 720)
        assertEquals(expectedValidValues, HabitIntervalValidator.getAllValidIntervalMinutes())
        
        // Test domain model validation
        expectedValidValues.forEach { validValue ->
            val habit = Habit(
                id = 1L,
                name = "Test Habit",
                description = "Test",
                createdAt = LocalDate.parse("2024-01-01"),
                detail = HabitDetail.IntervalHabitDetail(
                    intervalMinutes = validValue
                )
            )

            assertTrue(habit.detail is HabitDetail.IntervalHabitDetail)
            assertEquals(validValue, habit.detail.intervalMinutes)
        }
        
        // Test domain model rejection of invalid values
        val invalidValues = listOf(7, 8, 9, 11, 13, 14, 16, 17, 18, 19, 21, 25, 45, 150, 210, 270)
        invalidValues.forEach { invalidValue ->
            assertFailsWith<IllegalArgumentException> {
                Habit(
                    id = 1L,
                    name = "Test Habit",
                    description = "Test",
                    createdAt = LocalDate.parse("2024-01-01"),
                    detail = HabitDetail.IntervalHabitDetail(
                        intervalMinutes = invalidValue
                    )
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
        
        // Test INTERVAL frequency with 60-minute intervals (previously HOURLY)
        viewModel.updateFrequencyType(FrequencyType.INTERVAL)
        viewModel.updateIntervalValue(45, TimeUnit.MINUTES) // Should be corrected to a valid divisor
        val intervalState60 = viewModel.uiState.value
        assertTrue(intervalState60 is HabitEditUiState.Content)
        assertTrue(
            HabitIntervalValidator.isValidIntervalMinutes(FrequencyType.INTERVAL, intervalState60.intervalMinutes),
            "Expected ${intervalState60.intervalMinutes} to be a valid divisor of 60"
        )
        // 45 should be corrected to 30 or 60 (nearest valid divisor)
        
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
            detail = HabitDetail.OnceDailyHabitDetail()
        )
        assertEquals(FrequencyType.ONCE_DAILY, habit1.frequencyType)
        assertTrue(habit1.detail is HabitDetail.OnceDailyHabitDetail)

        // Test INTERVAL - 60 minutes is a valid divisor
        val habit2 = Habit(
            id = 2L,
            name = "Test Interval 60",
            description = "Test",
            createdAt = LocalDate.parse("2024-01-01"),
            detail = HabitDetail.IntervalHabitDetail(
                intervalMinutes = 60, // Valid for INTERVAL (divisor of 60)
                startTime = LocalTime(9, 0)
            )
        )
        assertEquals(FrequencyType.INTERVAL, habit2.frequencyType)
        assertTrue(habit2.detail is HabitDetail.IntervalHabitDetail)
        assertEquals(60, habit2.detail.intervalMinutes)
        
        // Test INTERVAL - divisors of 60 are valid
        val habit3 = Habit(
            id = 3L,
            name = "Test Interval",
            description = "Test",
            createdAt = LocalDate.parse("2024-01-01"),
            detail = HabitDetail.IntervalHabitDetail(
                intervalMinutes = 30 // Valid for INTERVAL
            )
        )
        assertEquals(FrequencyType.INTERVAL, habit3.frequencyType)
        assertTrue(habit3.detail is HabitDetail.IntervalHabitDetail)
        assertEquals(30, habit3.detail.intervalMinutes)
        
        // Test invalid values
        assertFailsWith<IllegalArgumentException> {
            Habit(
                id = 6L,
                name = "Invalid Interval",
                description = "Test",
                createdAt = LocalDate.parse("2024-01-01"),
                detail = HabitDetail.IntervalHabitDetail(
                    intervalMinutes = 7 // Invalid for INTERVAL (not divisor of 60)
                )
            )
        }
    }
}