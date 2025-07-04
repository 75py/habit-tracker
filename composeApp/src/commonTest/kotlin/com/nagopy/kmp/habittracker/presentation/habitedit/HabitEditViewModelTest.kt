package com.nagopy.kmp.habittracker.presentation.habitedit

import com.nagopy.kmp.habittracker.domain.model.Habit
import com.nagopy.kmp.habittracker.domain.model.FrequencyType
import com.nagopy.kmp.habittracker.domain.model.HabitDetail
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
import kotlinx.datetime.LocalDate
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
        assertTrue(uiState is HabitEditUiState.Content)
        assertEquals("", uiState.name)
        assertEquals("", uiState.description)
        assertEquals("#2196F3", uiState.color)
        assertTrue(uiState.isActive)
        assertEquals(FrequencyType.ONCE_DAILY, uiState.frequencyType)
        assertEquals(60, uiState.intervalMinutes) // 1 hour = 60 minutes
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
        assertTrue(uiState is HabitEditUiState.Content)
        assertEquals("Morning Exercise", uiState.name)
        assertNull(uiState.nameError)
    }

    @Test
    fun `updateName should set error when name is blank`() {
        // When
        viewModel.updateName("")

        // Then
        val uiState = viewModel.uiState.value
        assertTrue(uiState is HabitEditUiState.Content)
        assertEquals("", uiState.name)
        assertEquals("Name is required", uiState.nameError)
    }

    @Test
    fun `updateDescription should update description`() {
        // When
        viewModel.updateDescription("30 minutes of exercise")

        // Then
        val uiState = viewModel.uiState.value
        assertTrue(uiState is HabitEditUiState.Content)
        assertEquals("30 minutes of exercise", uiState.description)
    }

    @Test
    fun `updateColor should update color`() {
        // When
        viewModel.updateColor("#FF5722")

        // Then
        val uiState = viewModel.uiState.value
        assertTrue(uiState is HabitEditUiState.Content)
        assertEquals("#FF5722", uiState.color)
    }

    @Test
    fun `updateIsActive should update isActive status`() {
        // When
        viewModel.updateIsActive(false)

        // Then
        val uiState = viewModel.uiState.value
        assertTrue(uiState is HabitEditUiState.Content)
        assertFalse(uiState.isActive)
    }

    @Test
    fun `updateFrequencyType should update frequency type`() {
        // When
        viewModel.updateFrequencyType(FrequencyType.INTERVAL)

        // Then
        val uiState = viewModel.uiState.value
        assertTrue(uiState is HabitEditUiState.Content)
        assertEquals(FrequencyType.INTERVAL, uiState.frequencyType)
    }


    @Test
    fun `updateFrequencyType should not change intervalMinutes when INTERVAL is selected`() {
        // Given - start with a custom interval (30 minutes, valid divisor of 60)
        viewModel.updateIntervalMinutes(30) // 30 minutes, valid for INTERVAL
        val initialState = viewModel.uiState.value
        assertTrue(initialState is HabitEditUiState.Content)
        assertEquals(30, initialState.intervalMinutes)

        // When
        viewModel.updateFrequencyType(FrequencyType.INTERVAL)

        // Then
        val uiState = viewModel.uiState.value
        assertTrue(uiState is HabitEditUiState.Content)
        assertEquals(FrequencyType.INTERVAL, uiState.frequencyType)
        assertEquals(30, uiState.intervalMinutes) // Should preserve the existing value
    }

    @Test
    fun `updateIntervalMinutes should update interval minutes`() {
        // When
        viewModel.updateIntervalMinutes(120) // 2 hours

        // Then
        val uiState = viewModel.uiState.value
        assertTrue(uiState is HabitEditUiState.Content)
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
        assertTrue(uiState is HabitEditUiState.Content)
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
        val uiState = viewModel.uiState.value
        assertTrue(uiState is HabitEditUiState.Content)
        assertEquals("Name is required", uiState.nameError)
    }

    @Test
    fun `saveHabit should fail when scheduled times is empty`() = runTest {
        // Given
        viewModel.updateName("Morning Exercise")
        viewModel.updateScheduledTimes(emptyList()) // Empty scheduled times

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
        val uiState = viewModel.uiState.value
        assertTrue(uiState is HabitEditUiState.Content)
        assertEquals("At least one scheduled time is required", uiState.saveError)
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
        val finalState = viewModel.uiState.value
        assertTrue(finalState is HabitEditUiState.Content)
        assertFalse(finalState.isSaving)
        
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
        val finalState = viewModel.uiState.value
        assertTrue(finalState is HabitEditUiState.Content)
        assertEquals(errorMessage, finalState.saveError)
        assertFalse(finalState.isSaving)
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
        val savingState = viewModel.uiState.value
        assertTrue(savingState is HabitEditUiState.Content)
        assertTrue(savingState.isSaving)

        // When saving completes
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - Saving should be false
        val completedState = viewModel.uiState.value
        assertTrue(completedState is HabitEditUiState.Content)
        assertFalse(completedState.isSaving)
    }

    @Test
    fun `clearErrors should clear all error states`() {
        // Given
        viewModel.updateName("") // This sets nameError
        
        // When
        viewModel.clearErrors()

        // Then
        val uiState = viewModel.uiState.value
        assertTrue(uiState is HabitEditUiState.Content)
        assertNull(uiState.nameError)
        assertNull(uiState.saveError)
    }

    @Test
    fun `saveHabit should not work in Error state`() = runTest {
        // Given - force the ViewModel into Error state
        coEvery { mockGetHabitUseCase(1L) } throws Exception("Database error")
        viewModel.loadHabitForEdit(1L)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Verify we're in Error state
        assertTrue(viewModel.uiState.value is HabitEditUiState.Error)

        var successCalled = false
        var errorMessage: String? = null

        // When - try to save
        viewModel.saveHabit(
            onSuccess = { successCalled = true },
            onError = { errorMessage = it }
        )

        // Then - should call onError with invalid state message
        assertFalse(successCalled)
        assertEquals("Invalid state for saving", errorMessage)
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
        val finalState = viewModel.uiState.value
        assertTrue(finalState is HabitEditUiState.Content)
        assertFalse(finalState.isSaving, "isSaving should be false after completion")
        
        // Verify notification scheduling was called for the new habit
        coVerify(exactly = 1) { mockScheduleNextNotificationUseCase.scheduleNextNotificationForHabit(expectedHabitId) }
    }

    @Test
    fun `loadHabitForEdit should set intervalUnit to MINUTES for non-60-multiple intervals`() = runTest {
        // Given - a habit with 6-minute interval (valid divisor of 60)
        val habit = Habit(
            id = 1L,
            name = "Test Habit",
            description = "Test description",
            color = "#FF5722",
            isActive = true,
            createdAt = LocalDate.parse("2024-01-01"),
            detail = HabitDetail.IntervalHabitDetail(
                intervalMinutes = 6, // 6 minutes - valid divisor of 60
                startTime = LocalTime(9, 0)
            )
        )
        
        coEvery { mockGetHabitUseCase(1L) } returns habit

        // When
        viewModel.loadHabitForEdit(1L)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.value
        assertTrue(uiState is HabitEditUiState.Content)
        assertEquals(6, uiState.intervalMinutes)
        assertEquals(TimeUnit.MINUTES, uiState.intervalUnit) // Should be MINUTES for 6-minute interval
        assertEquals(6, uiState.intervalValue) // Should display 6, not 0
    }

    @Test
    fun `loadHabitForEdit should set intervalUnit to HOURS for 60-multiple intervals`() = runTest {
        // Given - a habit with 60-minute interval (1 hour)
        val habit = Habit(
            id = 1L,
            name = "Test Habit",
            description = "Test description",
            color = "#FF5722",
            isActive = true,
            createdAt = LocalDate.parse("2024-01-01"),
            detail = HabitDetail.IntervalHabitDetail(
                intervalMinutes = 60, // 60 minutes - valid divisor of 60
                startTime = LocalTime(9, 0)
            )
        )
        
        coEvery { mockGetHabitUseCase(1L) } returns habit

        // When
        viewModel.loadHabitForEdit(1L)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.value
        assertTrue(uiState is HabitEditUiState.Content)
        assertEquals(60, uiState.intervalMinutes)
        assertEquals(TimeUnit.HOURS, uiState.intervalUnit) // Should be HOURS for 60-minute interval
        assertEquals(1, uiState.intervalValue) // Should display 1 hour
    }

    @Test
    fun `loadHabitForEdit should set intervalUnit to HOURS for 60-minute interval`() = runTest {
        // Given - a habit with exactly 60-minute interval
        val habit = Habit(
            id = 1L,
            name = "Test Habit",
            description = "Test description",
            color = "#FF5722",
            isActive = true,
            createdAt = LocalDate.parse("2024-01-01"),
            detail = HabitDetail.IntervalHabitDetail(
                intervalMinutes = 60, // 60 minutes
                startTime = LocalTime(9, 0)
            )
        )
        
        coEvery { mockGetHabitUseCase(1L) } returns habit

        // When
        viewModel.loadHabitForEdit(1L)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.value
        assertTrue(uiState is HabitEditUiState.Content)
        assertEquals(60, uiState.intervalMinutes)
        assertEquals(TimeUnit.HOURS, uiState.intervalUnit) // Should be HOURS for 60-minute interval
        assertEquals(1, uiState.intervalValue) // Should display 1 hour
    }

    @Test
    fun `loadHabitForEdit should reproduce original issue scenario - 10 minute interval`() = runTest {
        // Given - reproducing the exact scenario described in issue #75
        // A habit with 10-minute custom interval (valid divisor of 60)
        val habit = Habit(
            id = 1L,
            name = "カスタム間隔テスト",
            description = "10分間隔のテスト",
            color = "#2196F3",
            isActive = true,
            createdAt = LocalDate.parse("2024-01-01"),
            detail = HabitDetail.IntervalHabitDetail(
            intervalMinutes = 10, // 10 minutes - valid divisor of 60
                startTime = LocalTime(9, 0)
            )
        )
        
        coEvery { mockGetHabitUseCase(1L) } returns habit

        // When
        viewModel.loadHabitForEdit(1L)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - Should now show correctly as "10分" instead of "0時間"
        val uiState = viewModel.uiState.value
        assertTrue(uiState is HabitEditUiState.Content)
        assertEquals(10, uiState.intervalMinutes)
        assertEquals(TimeUnit.MINUTES, uiState.intervalUnit) // Should be MINUTES, not HOURS
        assertEquals(10, uiState.intervalValue) // Should display 10, not 0
        
        // Verify the fix: This would have been 0 before the fix (10 / 60 = 0 with integer division)
        // Now it correctly shows 10 with MINUTES unit
    }

    @Test
    fun `loadHabitForEdit should handle non-existent habit`() = runTest {
        // Given
        coEvery { mockGetHabitUseCase(999L) } returns null

        // When
        viewModel.loadHabitForEdit(999L)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.value
        assertTrue(uiState is HabitEditUiState.Error)
        assertEquals("Habit not found", uiState.message)
    }

    @Test
    fun `loadHabitForEdit should handle database error`() = runTest {
        // Given
        val errorMessage = "Database connection failed"
        coEvery { mockGetHabitUseCase(1L) } throws Exception(errorMessage)

        // When
        viewModel.loadHabitForEdit(1L)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.value
        assertTrue(uiState is HabitEditUiState.Error)
        assertEquals(errorMessage, uiState.message)
    }

    @Test
    fun `updateIntervalValue should handle unit changes from IntervalPickerDialog correctly`() {
        // Set frequency type to INTERVAL to allow divisor-based intervals
        viewModel.updateFrequencyType(FrequencyType.INTERVAL)
        
        // Given - initial state with valid INTERVAL value (30 minutes)
        viewModel.updateIntervalValue(30, TimeUnit.MINUTES)
        val initialState = viewModel.uiState.value
        assertTrue(initialState is HabitEditUiState.Content)
        assertEquals(30, initialState.intervalMinutes) // 30 minutes is valid for INTERVAL
        assertEquals(TimeUnit.MINUTES, initialState.intervalUnit)
        assertEquals(30, initialState.intervalValue)

        // When - user changes to 1 hour via dialog (60 minutes is valid divisor of 60)
        viewModel.updateIntervalValue(1, TimeUnit.HOURS)

        // Then - should update both value and unit correctly
        val state = viewModel.uiState.value
        assertTrue(state is HabitEditUiState.Content)
        assertEquals(60, state.intervalMinutes)
        assertEquals(TimeUnit.HOURS, state.intervalUnit)
        assertEquals(1, state.intervalValue)
    }
}