package com.nagopy.kmp.habittracker.presentation.habitedit

import com.nagopy.kmp.habittracker.domain.usecase.AddHabitUseCase
import com.nagopy.kmp.habittracker.domain.usecase.UpdateHabitUseCase
import com.nagopy.kmp.habittracker.domain.usecase.GetHabitUseCase
import com.nagopy.kmp.habittracker.domain.usecase.ManageNotificationsUseCase
import com.nagopy.kmp.habittracker.domain.usecase.ScheduleNextNotificationUseCase
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TimeUnitConversionTest {

    @Test
    fun `intervalValue should return correct value for minutes unit`() {
        val state = HabitEditUiState.Content(
            intervalMinutes = 30,
            intervalUnit = TimeUnit.MINUTES
        )
        assertEquals(30, state.intervalValue)
    }

    @Test
    fun `intervalValue should return correct value for hours unit`() {
        val state = HabitEditUiState.Content(
            intervalMinutes = 120, // 2 hours
            intervalUnit = TimeUnit.HOURS
        )
        assertEquals(2, state.intervalValue)
    }

    @Test
    fun `updateIntervalValue should convert minutes to minutes correctly`() {
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
        
        // Set frequency type to INTERVAL to allow 45 minutes (invalid for ONCE_DAILY)
        viewModel.updateFrequencyType(com.nagopy.kmp.habittracker.domain.model.FrequencyType.INTERVAL)
        viewModel.updateIntervalValue(45, TimeUnit.MINUTES)
        
        val state = viewModel.uiState.value
        // 45 minutes is invalid for INTERVAL, so it should be corrected to 30 (closest valid)
        assertTrue(state is HabitEditUiState.Content)
        assertEquals(30, state.intervalMinutes)
        assertEquals(TimeUnit.MINUTES, state.intervalUnit)
        assertEquals(30, state.intervalValue)
    }

    @Test
    fun `updateIntervalValue should convert hours to minutes correctly`() {
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
        
        // Set frequency type to HOURLY to allow hour-based intervals
        viewModel.updateFrequencyType(com.nagopy.kmp.habittracker.domain.model.FrequencyType.HOURLY)
        viewModel.updateIntervalValue(3, TimeUnit.HOURS)
        
        val state = viewModel.uiState.value
        assertTrue(state is HabitEditUiState.Content)
        assertEquals(180, state.intervalMinutes) // 3 hours = 180 minutes
        assertEquals(TimeUnit.HOURS, state.intervalUnit)
        assertEquals(3, state.intervalValue)
    }
}