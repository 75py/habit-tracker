package com.nagopy.kmp.habittracker.domain.usecase

import com.nagopy.kmp.habittracker.domain.repository.HabitRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class DeleteHabitUseCaseTest {

    @Test
    fun `invoke should cancel notifications and delete habit from repository`() = runTest {
        // Given
        val mockRepository = mockk<HabitRepository>()
        val mockManageNotificationsUseCase = mockk<ManageNotificationsUseCase>()
        val habitId = 1L
        coEvery { mockRepository.deleteHabit(habitId) } just runs
        coEvery { mockManageNotificationsUseCase.cancelHabitNotifications(habitId) } just runs
        val useCase = DeleteHabitUseCase(mockRepository, mockManageNotificationsUseCase)

        // When
        useCase(habitId)

        // Then
        coVerify(exactly = 1) { mockManageNotificationsUseCase.cancelHabitNotifications(habitId) }
        coVerify(exactly = 1) { mockRepository.deleteHabit(habitId) }
    }

    @Test
    fun `invoke should call notifications cancellation before repository deletion`() = runTest {
        // Given
        val mockRepository = mockk<HabitRepository>()
        val mockManageNotificationsUseCase = mockk<ManageNotificationsUseCase>()
        val habitId = 42L
        coEvery { mockRepository.deleteHabit(habitId) } just runs
        coEvery { mockManageNotificationsUseCase.cancelHabitNotifications(habitId) } just runs
        val useCase = DeleteHabitUseCase(mockRepository, mockManageNotificationsUseCase)

        // When
        useCase(habitId)

        // Then
        coVerify(exactly = 1) { mockManageNotificationsUseCase.cancelHabitNotifications(habitId) }
        coVerify(exactly = 1) { mockRepository.deleteHabit(habitId) }
    }
}