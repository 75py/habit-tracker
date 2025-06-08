package com.nagopy.kmp.habittracker.presentation.habitedit

import kotlin.test.Test
import kotlin.test.assertEquals

class TimeUnitConversionTest {

    @Test
    fun `intervalValue should return correct value for minutes unit`() {
        val state = HabitEditUiState(
            intervalMinutes = 30,
            intervalUnit = TimeUnit.MINUTES
        )
        assertEquals(30, state.intervalValue)
    }

    @Test
    fun `intervalValue should return correct value for hours unit`() {
        val state = HabitEditUiState(
            intervalMinutes = 120, // 2 hours
            intervalUnit = TimeUnit.HOURS
        )
        assertEquals(2, state.intervalValue)
    }

    @Test
    fun `updateIntervalValue should convert minutes to minutes correctly`() {
        val viewModel = HabitEditViewModel(
            addHabitUseCase = MockAddHabitUseCase(),
            manageNotificationsUseCase = MockManageNotificationsUseCase()
        )
        
        viewModel.updateIntervalValue(45, TimeUnit.MINUTES)
        
        val state = viewModel.uiState.value
        assertEquals(45, state.intervalMinutes)
        assertEquals(TimeUnit.MINUTES, state.intervalUnit)
        assertEquals(45, state.intervalValue)
    }

    @Test
    fun `updateIntervalValue should convert hours to minutes correctly`() {
        val viewModel = HabitEditViewModel(
            addHabitUseCase = MockAddHabitUseCase(),
            manageNotificationsUseCase = MockManageNotificationsUseCase()
        )
        
        viewModel.updateIntervalValue(3, TimeUnit.HOURS)
        
        val state = viewModel.uiState.value
        assertEquals(180, state.intervalMinutes) // 3 hours = 180 minutes
        assertEquals(TimeUnit.HOURS, state.intervalUnit)
        assertEquals(3, state.intervalValue)
    }
}

// Mock implementations for testing
class MockAddHabitUseCase : com.nagopy.kmp.habittracker.domain.usecase.AddHabitUseCase {
    override suspend fun invoke(habit: com.nagopy.kmp.habittracker.domain.model.Habit): Long = 1L
}

class MockManageNotificationsUseCase : com.nagopy.kmp.habittracker.domain.usecase.ManageNotificationsUseCase {
    override suspend fun scheduleNotificationsForTodayTasks() {}
}