package com.nagopy.kmp.habittracker.domain.usecase

import com.nagopy.kmp.habittracker.domain.model.Habit
import com.nagopy.kmp.habittracker.domain.model.FrequencyType
import com.nagopy.kmp.habittracker.domain.repository.HabitRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.test.Test

class UpdateHabitUseCaseTest {

    @Test
    fun `invoke should update habit in repository when habit remains active`() = runTest {
        // Given
        val mockRepository = mockk<HabitRepository>()
        val mockManageNotificationsUseCase = mockk<ManageNotificationsUseCase>()
        val existingHabit = Habit(
            id = 1L,
            name = "Original Morning Workout",
            description = "30 minutes of exercise",
            color = "#FF5722",
            isActive = true,
            createdAt = LocalDate.parse("2024-01-01"),
            frequencyType = FrequencyType.ONCE_DAILY,
            intervalMinutes = 1440,
            scheduledTimes = listOf(LocalTime(7, 0))
        )
        val updatedHabit = existingHabit.copy(
            name = "Updated Morning Workout",
            description = "45 minutes of exercise",
            scheduledTimes = listOf(LocalTime(7, 30))
        )
        coEvery { mockRepository.getHabit(1L) } returns existingHabit
        coEvery { mockRepository.updateHabit(updatedHabit) } just runs
        val useCase = UpdateHabitUseCase(mockRepository, mockManageNotificationsUseCase)

        // When
        useCase(updatedHabit)

        // Then
        coVerify(exactly = 1) { mockRepository.updateHabit(updatedHabit) }
        coVerify(exactly = 0) { mockManageNotificationsUseCase.cancelHabitNotifications(any()) }
    }

    @Test
    fun `invoke should cancel notifications when habit is deactivated`() = runTest {
        // Given
        val mockRepository = mockk<HabitRepository>()
        val mockManageNotificationsUseCase = mockk<ManageNotificationsUseCase>()
        val existingHabit = Habit(
            id = 2L,
            name = "Reading",
            description = "Read for 30 minutes",
            color = "#2196F3",
            isActive = true,
            createdAt = LocalDate.parse("2024-01-02"),
            frequencyType = FrequencyType.ONCE_DAILY,
            intervalMinutes = 1440,
            scheduledTimes = listOf(LocalTime(20, 0))
        )
        val deactivatedHabit = existingHabit.copy(isActive = false)
        coEvery { mockRepository.getHabit(2L) } returns existingHabit
        coEvery { mockRepository.updateHabit(deactivatedHabit) } just runs
        coEvery { mockManageNotificationsUseCase.cancelHabitNotifications(2L) } just runs
        val useCase = UpdateHabitUseCase(mockRepository, mockManageNotificationsUseCase)

        // When
        useCase(deactivatedHabit)

        // Then
        coVerify(exactly = 1) { mockRepository.updateHabit(deactivatedHabit) }
        coVerify(exactly = 1) { mockManageNotificationsUseCase.cancelHabitNotifications(2L) }
    }

    @Test
    fun `invoke should not cancel notifications when habit was already inactive`() = runTest {
        // Given
        val mockRepository = mockk<HabitRepository>()
        val mockManageNotificationsUseCase = mockk<ManageNotificationsUseCase>()
        val existingHabit = Habit(
            id = 3L,
            name = "Reading",
            description = "Read for 30 minutes",
            color = "#2196F3",
            isActive = false,
            createdAt = LocalDate.parse("2024-01-03"),
            frequencyType = FrequencyType.ONCE_DAILY,
            intervalMinutes = 1440,
            scheduledTimes = listOf(LocalTime(20, 0))
        )
        val updatedHabit = existingHabit.copy(description = "Read for 45 minutes")
        coEvery { mockRepository.getHabit(3L) } returns existingHabit
        coEvery { mockRepository.updateHabit(updatedHabit) } just runs
        val useCase = UpdateHabitUseCase(mockRepository, mockManageNotificationsUseCase)

        // When
        useCase(updatedHabit)

        // Then
        coVerify(exactly = 1) { mockRepository.updateHabit(updatedHabit) }
        coVerify(exactly = 0) { mockManageNotificationsUseCase.cancelHabitNotifications(any()) }
    }

    @Test
    fun `invoke should handle case when existing habit is not found`() = runTest {
        // Given
        val mockRepository = mockk<HabitRepository>()
        val mockManageNotificationsUseCase = mockk<ManageNotificationsUseCase>()
        val habit = Habit(
            id = 999L,
            name = "New Habit",
            description = "Description",
            color = "#2196F3",
            isActive = false,
            createdAt = LocalDate.parse("2024-01-04"),
            frequencyType = FrequencyType.ONCE_DAILY,
            intervalMinutes = 1440,
            scheduledTimes = listOf(LocalTime(20, 0))
        )
        coEvery { mockRepository.getHabit(999L) } returns null
        coEvery { mockRepository.updateHabit(habit) } just runs
        val useCase = UpdateHabitUseCase(mockRepository, mockManageNotificationsUseCase)

        // When
        useCase(habit)

        // Then
        coVerify(exactly = 1) { mockRepository.updateHabit(habit) }
        coVerify(exactly = 0) { mockManageNotificationsUseCase.cancelHabitNotifications(any()) }
    }
}