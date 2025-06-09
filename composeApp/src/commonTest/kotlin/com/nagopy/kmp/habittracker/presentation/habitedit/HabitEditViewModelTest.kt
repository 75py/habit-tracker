package com.nagopy.kmp.habittracker.presentation.habitedit

import com.nagopy.kmp.habittracker.domain.model.Habit
import com.nagopy.kmp.habittracker.domain.model.FrequencyType
import com.nagopy.kmp.habittracker.domain.usecase.AddHabitUseCase
import com.nagopy.kmp.habittracker.domain.usecase.UpdateHabitUseCase
import com.nagopy.kmp.habittracker.domain.usecase.GetHabitUseCase
import com.nagopy.kmp.habittracker.domain.usecase.ManageNotificationsUseCase
import com.nagopy.kmp.habittracker.domain.usecase.ScheduleNextNotificationUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import kotlinx.datetime.LocalTime
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class HabitEditViewModelTest {

    private val mockAddHabitUseCase = mockk<AddHabitUseCase>()
    private val mockUpdateHabitUseCase = mockk<UpdateHabitUseCase>()
    private val mockGetHabitUseCase = mockk<GetHabitUseCase>()
    private val mockManageNotificationsUseCase = mockk<ManageNotificationsUseCase>(relaxed = true)
    private val mockScheduleNextNotificationUseCase = mockk<ScheduleNextNotificationUseCase>(relaxed = true)
    private lateinit var viewModel: HabitEditViewModel
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = HabitEditViewModel(mockAddHabitUseCase, mockUpdateHabitUseCase, mockGetHabitUseCase, mockManageNotificationsUseCase, mockScheduleNextNotificationUseCase)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should have default values`() {
        // Then
        val uiState = viewModel.uiState.value
        assertEquals("", uiState.name)
        assertEquals("", uiState.description)
        assertEquals("#2196F3", uiState.color)
        assertTrue(uiState.isActive)
        assertEquals(FrequencyType.ONCE_DAILY, uiState.frequencyType)
        assertEquals(1440, uiState.intervalMinutes) // 24 hours = 1440 minutes
        assertEquals(listOf(LocalTime(9, 0)), uiState.scheduledTimes)
        assertNull(uiState.nameError)
        assertNull(uiState.saveError)
        assertFalse(uiState.isSaving)
    }

    @Test
    fun `updateName should update name and clear error when valid`() {
        // When
        viewModel.updateName("Morning Exercise")

        // Then
        val uiState = viewModel.uiState.value
        assertEquals("Morning Exercise", uiState.name)
        assertNull(uiState.nameError)
    }

    @Test
    fun `updateName should set error when name is blank`() {
        // When
        viewModel.updateName("")

        // Then
        val uiState = viewModel.uiState.value
        assertEquals("", uiState.name)
        assertEquals("Name is required", uiState.nameError)
    }

    @Test
    fun `updateDescription should update description`() {
        // When
        viewModel.updateDescription("30 minutes of exercise")

        // Then
        val uiState = viewModel.uiState.value
        assertEquals("30 minutes of exercise", uiState.description)
    }

    @Test
    fun `updateColor should update color`() {
        // When
        viewModel.updateColor("#FF5722")

        // Then
        val uiState = viewModel.uiState.value
        assertEquals("#FF5722", uiState.color)
    }

    @Test
    fun `updateIsActive should update isActive status`() {
        // When
        viewModel.updateIsActive(false)

        // Then
        val uiState = viewModel.uiState.value
        assertFalse(uiState.isActive)
    }

    @Test
    fun `updateFrequencyType should update frequency type`() {
        // When
        viewModel.updateFrequencyType(FrequencyType.HOURLY)

        // Then
        val uiState = viewModel.uiState.value
        assertEquals(FrequencyType.HOURLY, uiState.frequencyType)
    }

    @Test
    fun `updateFrequencyType should set intervalMinutes to 60 when HOURLY is selected`() {
        // Given - start with a different interval
        viewModel.updateIntervalMinutes(240) // 4 hours
        assertEquals(240, viewModel.uiState.value.intervalMinutes)

        // When
        viewModel.updateFrequencyType(FrequencyType.HOURLY)

        // Then
        val uiState = viewModel.uiState.value
        assertEquals(FrequencyType.HOURLY, uiState.frequencyType)
        assertEquals(60, uiState.intervalMinutes) // 1 hour = 60 minutes
    }

    @Test
    fun `updateFrequencyType should not change intervalMinutes when non-HOURLY is selected`() {
        // Given - start with a custom interval
        viewModel.updateIntervalMinutes(180) // 3 hours
        assertEquals(180, viewModel.uiState.value.intervalMinutes)

        // When
        viewModel.updateFrequencyType(FrequencyType.INTERVAL)

        // Then
        val uiState = viewModel.uiState.value
        assertEquals(FrequencyType.INTERVAL, uiState.frequencyType)
        assertEquals(180, uiState.intervalMinutes) // Should preserve the existing value
    }

    @Test
    fun `updateIntervalMinutes should update interval minutes`() {
        // When
        viewModel.updateIntervalMinutes(120) // 2 hours

        // Then
        val uiState = viewModel.uiState.value
        assertEquals(120, uiState.intervalMinutes)
    }

    @Test
    fun `updateScheduledTimes should update scheduled times`() {
        // Given
        val newTimes = listOf(LocalTime(8, 0), LocalTime(12, 0), LocalTime(18, 0))

        // When
        viewModel.updateScheduledTimes(newTimes)

        // Then
        val uiState = viewModel.uiState.value
        assertEquals(newTimes, uiState.scheduledTimes)
    }

    @Test
    fun `saveHabit should fail when name is blank`() = runTest {
        // Given
        var successCalled = false
        var errorCalled = false

        // When
        viewModel.saveHabit(
            onSuccess = { successCalled = true },
            onError = { errorCalled = true }
        )

        // Then
        assertFalse(successCalled)
        assertFalse(errorCalled)
        assertEquals("Name is required", viewModel.uiState.value.nameError)
    }

    @Test
    fun `saveHabit should succeed with valid data`() = runTest {
        // Given
        val expectedHabitId = 123L
        coEvery { mockAddHabitUseCase(any()) } returns expectedHabitId
        
        viewModel.updateName("Morning Exercise")
        viewModel.updateDescription("30 minutes workout")
        viewModel.updateColor("#FF5722")
        viewModel.updateIsActive(true)

        var successResult: Long? = null
        var errorMessage: String? = null

        // When
        viewModel.saveHabit(
            onSuccess = { successResult = it },
            onError = { errorMessage = it }
        )
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(expectedHabitId, successResult)
        assertNull(errorMessage)
        assertFalse(viewModel.uiState.value.isSaving)
        
        coVerify(exactly = 1) {
            mockAddHabitUseCase(match { habit ->
                habit.name == "Morning Exercise" &&
                habit.description == "30 minutes workout" &&
                habit.color == "#FF5722" &&
                habit.isActive == true
            })
        }
    }

    @Test
    fun `saveHabit should handle errors from use case`() = runTest {
        // Given
        val errorMessage = "Database error"
        coEvery { mockAddHabitUseCase(any()) } throws Exception(errorMessage)
        
        viewModel.updateName("Valid Name")

        var successResult: Long? = null
        var errorResult: String? = null

        // When
        viewModel.saveHabit(
            onSuccess = { successResult = it },
            onError = { errorResult = it }
        )
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertNull(successResult)
        assertEquals(errorMessage, errorResult)
        assertEquals(errorMessage, viewModel.uiState.value.saveError)
        assertFalse(viewModel.uiState.value.isSaving)
    }

    @Test
    fun `saveHabit should manage saving state correctly`() = runTest {
        // Given
        coEvery { mockAddHabitUseCase(any()) } returns 1L
        viewModel.updateName("Test Habit")

        // When
        viewModel.saveHabit(
            onSuccess = { },
            onError = { }
        )

        // Then - Initially saving should be true
        assertTrue(viewModel.uiState.value.isSaving)

        // When saving completes
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - Saving should be false
        assertFalse(viewModel.uiState.value.isSaving)
    }

    @Test
    fun `clearErrors should clear all error states`() {
        // Given
        viewModel.updateName("") // This sets nameError
        val habitWithErrors = viewModel.uiState.value.copy(saveError = "Some save error")
        
        // When
        viewModel.clearErrors()

        // Then
        val uiState = viewModel.uiState.value
        assertNull(uiState.nameError)
        assertNull(uiState.saveError)
    }

    @Test
    fun `saveHabit should trim whitespace from inputs`() = runTest {
        // Given
        coEvery { mockAddHabitUseCase(any()) } returns 1L
        
        viewModel.updateName("  Morning Exercise  ")
        viewModel.updateDescription("  30 minutes workout  ")

        // When
        viewModel.saveHabit(
            onSuccess = { },
            onError = { }
        )
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify(exactly = 1) {
            mockAddHabitUseCase(match { habit ->
                habit.name == "Morning Exercise" &&
                habit.description == "30 minutes workout"
            })
        }
    }

    @Test
    fun `saveHabit should complete navigation after notification scheduling without hanging`() = runTest {
        // Given
        val expectedHabitId = 456L
        coEvery { mockAddHabitUseCase(any()) } returns expectedHabitId
        
        viewModel.updateName("Test Habit")

        var successResult: Long? = null
        var navigationCompleted = false

        // When - save habit which should trigger notification scheduling and navigation
        viewModel.saveHabit(
            onSuccess = { habitId -> 
                successResult = habitId
                navigationCompleted = true
            },
            onError = { }
        )
        
        // Advance dispatcher to complete all coroutines
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - navigation should complete indicating the method didn't hang
        assertEquals(expectedHabitId, successResult)
        assertTrue(navigationCompleted, "Navigation should complete after save operation")
        assertFalse(viewModel.uiState.value.isSaving, "isSaving should be false after completion")
        
        // Verify notification scheduling was called
        coVerify(exactly = 1) { mockManageNotificationsUseCase.scheduleNotificationsForTodayTasks() }
    }
}