package com.nagopy.kmp.habittracker.presentation.habitedit

import com.nagopy.kmp.habittracker.domain.usecase.AddHabitUseCase
import com.nagopy.kmp.habittracker.domain.usecase.UpdateHabitUseCase
import com.nagopy.kmp.habittracker.domain.usecase.GetHabitUseCase
import com.nagopy.kmp.habittracker.domain.usecase.ManageNotificationsUseCase
import io.mockk.mockk
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
        val mockAddHabitUseCase = mockk<AddHabitUseCase>(relaxed = true)
        val mockUpdateHabitUseCase = mockk<UpdateHabitUseCase>(relaxed = true)
        val mockGetHabitUseCase = mockk<GetHabitUseCase>(relaxed = true)
        val mockManageNotificationsUseCase = mockk<ManageNotificationsUseCase>(relaxed = true)
        
        val viewModel = HabitEditViewModel(
            addHabitUseCase = mockAddHabitUseCase,
            updateHabitUseCase = mockUpdateHabitUseCase,
            getHabitUseCase = mockGetHabitUseCase,
            manageNotificationsUseCase = mockManageNotificationsUseCase
        )
        
        viewModel.updateIntervalValue(45, TimeUnit.MINUTES)
        
        val state = viewModel.uiState.value
        assertEquals(45, state.intervalMinutes)
        assertEquals(TimeUnit.MINUTES, state.intervalUnit)
        assertEquals(45, state.intervalValue)
    }

    @Test
    fun `updateIntervalValue should convert hours to minutes correctly`() {
        val mockAddHabitUseCase = mockk<AddHabitUseCase>(relaxed = true)
        val mockUpdateHabitUseCase = mockk<UpdateHabitUseCase>(relaxed = true)
        val mockGetHabitUseCase = mockk<GetHabitUseCase>(relaxed = true)
        val mockManageNotificationsUseCase = mockk<ManageNotificationsUseCase>(relaxed = true)
        
        val viewModel = HabitEditViewModel(
            addHabitUseCase = mockAddHabitUseCase,
            updateHabitUseCase = mockUpdateHabitUseCase,
            getHabitUseCase = mockGetHabitUseCase,
            manageNotificationsUseCase = mockManageNotificationsUseCase
        )
        
        viewModel.updateIntervalValue(3, TimeUnit.HOURS)
        
        val state = viewModel.uiState.value
        assertEquals(180, state.intervalMinutes) // 3 hours = 180 minutes
        assertEquals(TimeUnit.HOURS, state.intervalUnit)
        assertEquals(3, state.intervalValue)
    }
}