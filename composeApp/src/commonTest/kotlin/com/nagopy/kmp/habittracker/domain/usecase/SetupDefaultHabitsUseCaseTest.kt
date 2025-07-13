package com.nagopy.kmp.habittracker.domain.usecase

import com.nagopy.kmp.habittracker.domain.model.Habit
import com.nagopy.kmp.habittracker.domain.model.HabitDetail
import com.nagopy.kmp.habittracker.domain.repository.HabitRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SetupDefaultHabitsUseCaseTest {

    @Test
    fun `invoke should create default habit when it does not exist`() = runTest {
        // Given
        val mockRepository = mockk<HabitRepository>()
        val habitSlot = slot<Habit>()
        val defaultHabitName = "水を飲む"
        
        coEvery { mockRepository.doesHabitExistByName(defaultHabitName) } returns false
        coEvery { mockRepository.createHabit(capture(habitSlot)) } returns 1L
        
        val useCase = SetupDefaultHabitsUseCase(mockRepository)

        // When
        useCase()

        // Then
        coVerify(exactly = 1) { mockRepository.doesHabitExistByName(defaultHabitName) }
        coVerify(exactly = 1) { mockRepository.createHabit(any()) }
        
        val createdHabit = habitSlot.captured
        assertEquals(defaultHabitName, createdHabit.name)
        assertEquals("", createdHabit.description)
        assertEquals("#2196F3", createdHabit.color)
        assertTrue(createdHabit.isActive)
        
        // Check habit detail
        assertTrue(createdHabit.detail is HabitDetail.IntervalHabitDetail)
        val detail = createdHabit.detail as HabitDetail.IntervalHabitDetail
        assertEquals(30, detail.intervalMinutes)
        assertEquals(LocalTime(9, 5), detail.startTime)
        assertEquals(LocalTime(17, 35), detail.endTime)
    }

    @Test
    fun `invoke should not create default habit when it already exists`() = runTest {
        // Given
        val mockRepository = mockk<HabitRepository>()
        val defaultHabitName = "水を飲む"
        
        coEvery { mockRepository.doesHabitExistByName(defaultHabitName) } returns true
        
        val useCase = SetupDefaultHabitsUseCase(mockRepository)

        // When
        useCase()

        // Then
        coVerify(exactly = 1) { mockRepository.doesHabitExistByName(defaultHabitName) }
        coVerify(exactly = 0) { mockRepository.createHabit(any()) }
    }

    @Test
    fun `invoke should not throw exception when repository operations fail`() = runTest {
        // Given
        val mockRepository = mockk<HabitRepository>()
        val defaultHabitName = "水を飲む"
        
        coEvery { mockRepository.doesHabitExistByName(defaultHabitName) } throws RuntimeException("Database error")
        
        val useCase = SetupDefaultHabitsUseCase(mockRepository)

        // When & Then (should not throw)
        useCase()
        
        // Verify the check was attempted
        coVerify(exactly = 1) { mockRepository.doesHabitExistByName(defaultHabitName) }
    }
}