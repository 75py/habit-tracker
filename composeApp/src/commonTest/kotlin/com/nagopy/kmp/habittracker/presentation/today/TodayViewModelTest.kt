package com.nagopy.kmp.habittracker.presentation.today

import com.nagopy.kmp.habittracker.domain.model.Task
import com.nagopy.kmp.habittracker.domain.usecase.GetTodayTasksUseCase
import com.nagopy.kmp.habittracker.domain.usecase.CompleteTaskUseCase
import com.nagopy.kmp.habittracker.domain.usecase.ManageNotificationsUseCase
import com.nagopy.kmp.habittracker.domain.usecase.ScheduleNextNotificationUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
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
class TodayViewModelTest {

    private lateinit var testDispatcher: TestDispatcher
    private lateinit var getTodayTasksUseCase: GetTodayTasksUseCase
    private lateinit var completeTaskUseCase: CompleteTaskUseCase
    private lateinit var manageNotificationsUseCase: ManageNotificationsUseCase
    private lateinit var scheduleNextNotificationUseCase: ScheduleNextNotificationUseCase
    private lateinit var viewModel: TodayViewModel

    @BeforeTest
    fun setup() {
        testDispatcher = UnconfinedTestDispatcher()
        Dispatchers.setMain(testDispatcher)
        
        getTodayTasksUseCase = mockk<GetTodayTasksUseCase>()
        completeTaskUseCase = mockk<CompleteTaskUseCase>()
        manageNotificationsUseCase = mockk<ManageNotificationsUseCase>(relaxed = true)
        scheduleNextNotificationUseCase = mockk<ScheduleNextNotificationUseCase>(relaxed = true)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should load tasks successfully`() = runTest {
        // Given
        val tasks = listOf(
            Task(
                habitId = 1L,
                habitName = "Exercise",
                habitDescription = "Daily workout",
                habitColor = "#FF5722",
                date = LocalDate.parse("2024-01-20"),
                scheduledTime = LocalTime(7, 0),
                isCompleted = false
            ),
            Task(
                habitId = 2L,
                habitName = "Meditate",
                habitDescription = "Morning meditation",
                habitColor = "#4CAF50",
                date = LocalDate.parse("2024-01-20"),
                scheduledTime = LocalTime(8, 0),
                isCompleted = true
            )
        )
        
        every { getTodayTasksUseCase() } returns flowOf(tasks)
        
        // When
        viewModel = TodayViewModel(getTodayTasksUseCase, completeTaskUseCase, manageNotificationsUseCase, scheduleNextNotificationUseCase)
        
        // Then
        val uiState = viewModel.uiState.first()
        assertTrue(uiState is TodayUiState.Content)
        assertEquals(2, uiState.tasks.size)
        assertEquals("Exercise", uiState.tasks[0].habitName)
        assertEquals("Meditate", uiState.tasks[1].habitName)
    }

    @Test
    fun `should handle empty task list`() = runTest {
        // Given
        every { getTodayTasksUseCase() } returns flowOf(emptyList())
        
        // When
        viewModel = TodayViewModel(getTodayTasksUseCase, completeTaskUseCase, manageNotificationsUseCase, scheduleNextNotificationUseCase)
        
        // Then
        val uiState = viewModel.uiState.first()
        assertTrue(uiState is TodayUiState.Empty)
    }

    @Test
    fun `should handle error when loading tasks fails`() = runTest {
        // Given
        val exception = RuntimeException("Network error")
        every { getTodayTasksUseCase() } returns kotlinx.coroutines.flow.flow { 
            throw exception 
        }
        
        // When
        viewModel = TodayViewModel(getTodayTasksUseCase, completeTaskUseCase, manageNotificationsUseCase, scheduleNextNotificationUseCase)
        
        // Then
        val uiState = viewModel.uiState.first()
        assertTrue(uiState is TodayUiState.Error)
        assertEquals("Network error", uiState.message)
    }

    @Test
    fun `completeTask should call use case and update task completion in UI`() = runTest {
        // Given
        val task = Task(
            habitId = 1L,
            habitName = "Exercise",
            habitDescription = "Daily workout",
            habitColor = "#FF5722",
            date = LocalDate.parse("2024-01-20"),
            scheduledTime = LocalTime(7, 0),
            isCompleted = false
        )
        
        every { getTodayTasksUseCase() } returns flowOf(listOf(task))
        coEvery { completeTaskUseCase(any(), any(), any()) } returns 1L
        
        viewModel = TodayViewModel(getTodayTasksUseCase, completeTaskUseCase, manageNotificationsUseCase, scheduleNextNotificationUseCase)
        
        // Verify initial state - task should not be completed
        val initialState = viewModel.uiState.first()
        assertTrue(initialState is TodayUiState.Content)
        assertFalse(initialState.tasks.first().isCompleted)
        assertTrue(initialState.completedTaskKeys.isEmpty())
        
        // When
        viewModel.completeTask(task)
        
        // Then
        coVerify { completeTaskUseCase(1L, LocalDate.parse("2024-01-20"), LocalTime(7, 0)) }
        
        // Verify the UI state is updated immediately - both tasks list and completed keys
        val updatedState = viewModel.uiState.first()
        assertTrue(updatedState is TodayUiState.Content)
        assertTrue(updatedState.tasks.first().isCompleted, "Task should be marked as completed in tasks list")
        assertTrue(updatedState.completedTaskKeys.contains("1_2024-01-20_07:00"), "Task key should be in completed set")
    }

    @Test
    fun `completeTask should handle errors`() = runTest {
        // Given
        val task = Task(
            habitId = 1L,
            habitName = "Exercise",
            habitDescription = "Daily workout",
            habitColor = "#FF5722",
            date = LocalDate.parse("2024-01-20"),
            scheduledTime = LocalTime(7, 0),
            isCompleted = false
        )
        
        every { getTodayTasksUseCase() } returns flowOf(listOf(task))
        coEvery { completeTaskUseCase(any(), any(), any()) } throws RuntimeException("Failed to complete")
        
        viewModel = TodayViewModel(getTodayTasksUseCase, completeTaskUseCase, manageNotificationsUseCase, scheduleNextNotificationUseCase)
        
        // When
        viewModel.completeTask(task)
        
        // Then
        val uiState = viewModel.uiState.first()
        assertTrue(uiState is TodayUiState.Error)
        assertEquals("Failed to complete task: Failed to complete", uiState.message)
    }
}