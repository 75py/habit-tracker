package com.nagopy.kmp.habittracker.presentation.habitlist

import com.nagopy.kmp.habittracker.domain.model.Habit
import com.nagopy.kmp.habittracker.domain.model.FrequencyType
import com.nagopy.kmp.habittracker.domain.model.HabitDetail
import com.nagopy.kmp.habittracker.domain.usecase.GetAllHabitsUseCase
import com.nagopy.kmp.habittracker.domain.usecase.DeleteHabitUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
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
class HabitListViewModelTest {

    private val mockGetAllHabitsUseCase = mockk<GetAllHabitsUseCase>()
    private val mockDeleteHabitUseCase = mockk<DeleteHabitUseCase>()
    private lateinit var viewModel: HabitListViewModel
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init should load habits successfully`() = runTest {
        // Given
        val mockHabits = listOf(
            Habit(
                id = 1,
                name = "Morning Exercise",
                description = "30 minutes workout",
                color = "#FF5722",
                isActive = true,
                createdAt = LocalDate.parse("2024-01-01"),
                detail = HabitDetail.OnceDailyHabitDetail(
                    scheduledTimes = listOf(LocalTime(7, 0))
                )
            ),
            Habit(
                id = 2,
                name = "Read Book",
                description = "Read for 20 minutes",
                color = "#2196F3",
                isActive = true,
                createdAt = LocalDate.parse("2024-01-02"),
                detail = HabitDetail.OnceDailyHabitDetail(
                    scheduledTimes = listOf(LocalTime(20, 0))
                )
            )
        )
        every { mockGetAllHabitsUseCase() } returns flowOf(mockHabits)

        // When
        viewModel = HabitListViewModel(mockGetAllHabitsUseCase, mockDeleteHabitUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.value
        assertTrue(uiState is HabitListUiState.Content)
        assertEquals(mockHabits, uiState.habits)
    }

    @Test
    fun `init should handle error when loading habits fails`() = runTest {
        // Given
        val errorMessage = "Database connection failed"
        val errorFlow = kotlinx.coroutines.flow.flow<List<Habit>> {
            throw Exception(errorMessage)
        }
        every { mockGetAllHabitsUseCase() } returns errorFlow

        // When
        viewModel = HabitListViewModel(mockGetAllHabitsUseCase, mockDeleteHabitUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.value
        assertTrue(uiState is HabitListUiState.Error)
        assertEquals(errorMessage, uiState.message)
    }

    @Test
    fun `refresh should reload habits`() = runTest {
        // Given
        val initialHabits = listOf(
            Habit(
                id = 1,
                name = "Initial Habit",
                description = "",
                color = "#FF5722",
                isActive = true,
                createdAt = LocalDate.parse("2024-01-01"),
                detail = HabitDetail.OnceDailyHabitDetail(
                    scheduledTimes = listOf(LocalTime(9, 0))
                )
            )
        )
        val refreshedHabits = listOf(
            Habit(
                id = 1,
                name = "Initial Habit",
                description = "",
                color = "#FF5722",
                isActive = true,
                createdAt = LocalDate.parse("2024-01-01"),
                detail = HabitDetail.OnceDailyHabitDetail(
                    scheduledTimes = listOf(LocalTime(9, 0))
                )
            ),
            Habit(
                id = 2,
                name = "New Habit",
                description = "Added after refresh",
                color = "#2196F3",
                isActive = true,
                createdAt = LocalDate.parse("2024-01-02"),
                detail = HabitDetail.HourlyHabitDetail(
                    startTime = LocalTime(10, 0)
                )
            )
        )

        every { mockGetAllHabitsUseCase() } returnsMany listOf(
            flowOf(initialHabits),
            flowOf(refreshedHabits)
        )

        viewModel = HabitListViewModel(mockGetAllHabitsUseCase, mockDeleteHabitUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.refresh()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.value
        assertTrue(uiState is HabitListUiState.Content)
        assertEquals(refreshedHabits, uiState.habits)
    }

    @Test
    fun `init should handle empty habits list`() = runTest {
        // Given
        every { mockGetAllHabitsUseCase() } returns flowOf(emptyList())

        // When
        viewModel = HabitListViewModel(mockGetAllHabitsUseCase, mockDeleteHabitUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.value
        assertTrue(uiState is HabitListUiState.Empty)
    }

    @Test
    fun `loading state should be managed correctly`() = runTest {
        // Given
        val mockHabits = listOf(
            Habit(
                id = 1,
                name = "Test Habit",
                description = "",
                color = "#FF5722",
                isActive = true,
                createdAt = LocalDate.parse("2024-01-01"),
                detail = HabitDetail.OnceDailyHabitDetail(
                    scheduledTimes = listOf(LocalTime(9, 0))
                )
            )
        )
        every { mockGetAllHabitsUseCase() } returns flowOf(mockHabits)

        // When
        viewModel = HabitListViewModel(mockGetAllHabitsUseCase, mockDeleteHabitUseCase)

        // Then - After advancing the coroutines, loading should be complete
        testDispatcher.scheduler.advanceUntilIdle()
        val finalState = viewModel.uiState.value
        assertTrue(finalState is HabitListUiState.Content)
        assertEquals(mockHabits, finalState.habits)
    }
}