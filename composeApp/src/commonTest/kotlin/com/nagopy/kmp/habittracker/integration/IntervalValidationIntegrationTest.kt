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
import com.nagopy.kmp.habittracker.presentation.habitedit.TimeUnit
import io.mockk.mockk
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class IntervalValidationIntegrationTest {

    @Test
    fun `complete workflow - INTERVAL type should be restricted to valid divisors of 60`() {
        // Verify all expected valid divisors
        val expectedValidValues = listOf(1, 2, 3, 4, 5, 6, 10, 12, 15, 20, 30, 60)
        assertEquals(expectedValidValues, HabitIntervalValidator.VALID_INTERVAL_MINUTES)
        
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
    fun `ViewModel should auto-correct invalid INTERVAL values`() {
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
        
        // Set frequency type to INTERVAL first
        viewModel.updateFrequencyType(FrequencyType.INTERVAL)
        
        // Test that invalid values get auto-corrected to closest valid values
        viewModel.updateIntervalValue(7, TimeUnit.MINUTES)
        val state1 = viewModel.uiState.value
        assertTrue(
            HabitIntervalValidator.isValidIntervalMinutes(state1.intervalMinutes),
            "Expected ${state1.intervalMinutes} to be a valid divisor of 60"
        )
        
        viewModel.updateIntervalValue(25, TimeUnit.MINUTES)
        val state2 = viewModel.uiState.value
        assertTrue(
            HabitIntervalValidator.isValidIntervalMinutes(state2.intervalMinutes),
            "Expected ${state2.intervalMinutes} to be a valid divisor of 60"
        )
        
        viewModel.updateIntervalValue(90, TimeUnit.MINUTES)
        val state3 = viewModel.uiState.value
        assertTrue(
            HabitIntervalValidator.isValidIntervalMinutes(state3.intervalMinutes),
            "Expected ${state3.intervalMinutes} to be a valid divisor of 60"
        )
        assertEquals(60, state3.intervalMinutes) // 90 should be corrected to 60
    }

    @Test
    fun `non-INTERVAL frequency types should not be restricted`() {
        // ONCE_DAILY and HOURLY should allow any reasonable values
        val habit1 = Habit(
            id = 1L,
            name = "Test Daily",
            description = "Test",
            createdAt = LocalDate.parse("2024-01-01"),
            frequencyType = FrequencyType.ONCE_DAILY,
            intervalMinutes = 90 // This would be invalid for INTERVAL, but OK for ONCE_DAILY
        )
        assertEquals(90, habit1.intervalMinutes)

        val habit2 = Habit(
            id = 2L,
            name = "Test Hourly",
            description = "Test",
            createdAt = LocalDate.parse("2024-01-01"),
            frequencyType = FrequencyType.HOURLY,
            intervalMinutes = 180 // This would be invalid for INTERVAL, but OK for HOURLY
        )
        assertEquals(180, habit2.intervalMinutes)
    }
}