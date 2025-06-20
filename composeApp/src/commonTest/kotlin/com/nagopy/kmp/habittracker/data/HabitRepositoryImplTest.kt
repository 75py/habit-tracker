package com.nagopy.kmp.habittracker.data

import com.nagopy.kmp.habittracker.data.local.HabitDao
import com.nagopy.kmp.habittracker.data.local.HabitEntity
import com.nagopy.kmp.habittracker.data.local.LogEntity
import com.nagopy.kmp.habittracker.domain.model.Habit
import com.nagopy.kmp.habittracker.domain.model.HabitLog
import com.nagopy.kmp.habittracker.domain.model.FrequencyType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class HabitRepositoryImplTest {

    private val habitDao = mockk<HabitDao>()
    private val repository = HabitRepositoryImpl(habitDao)

    @Test
    fun `createHabit should call dao insertHabit and return id`() = runTest {
        // Given
        val habit = Habit(
            id = 0,
            name = "Exercise",
            description = "Daily workout",
            color = "#FF5722",
            isActive = true,
            createdAt = LocalDate.parse("2024-01-01"),
            frequencyType = FrequencyType.ONCE_DAILY,
            intervalMinutes = 1440,
            scheduledTimes = listOf(LocalTime(7, 0))
        )
        val expectedId = 1L
        coEvery { habitDao.insertHabit(any()) } returns expectedId

        // When
        val result = repository.createHabit(habit)

        // Then
        assertEquals(expectedId, result)
        coVerify { habitDao.insertHabit(any()) }
    }

    @Test
    fun `getAllHabits should return mapped habits from dao`() = runTest {
        // Given
        val habitEntities = listOf(
            HabitEntity(
                id = 1,
                name = "Exercise",
                description = "Daily workout",
                color = "#FF5722",
                isActive = true,
                createdAt = "2024-01-01",
                intervalMinutes = 1440,
                scheduledTimes = "07:00"
            )
        )
        every { habitDao.getAllHabits() } returns flowOf(habitEntities)

        // When
        val result = repository.getAllHabits().first()

        // Then
        assertEquals(1, result.size)
        assertEquals("Exercise", result[0].name)
        assertEquals(FrequencyType.ONCE_DAILY, result[0].frequencyType)
        verify { habitDao.getAllHabits() }
    }

    @Test
    fun `getActiveHabits should return only active habits`() = runTest {
        // Given
        val activeHabits = listOf(
            HabitEntity(
                id = 1,
                name = "Active Habit",
                description = "Description",
                color = "#FF5722",
                isActive = true,
                createdAt = "2024-01-01",
                intervalMinutes = 1440,
                scheduledTimes = "09:00"
            )
        )
        every { habitDao.getActiveHabits() } returns flowOf(activeHabits)

        // When
        val result = repository.getActiveHabits().first()

        // Then
        assertEquals(1, result.size)
        assertEquals("Active Habit", result[0].name)
        assertEquals(true, result[0].isActive)
        verify { habitDao.getActiveHabits() }
    }

    @Test
    fun `getHabit should return mapped habit when found`() = runTest {
        // Given
        val habitId = 1L
        val habitEntity = HabitEntity(
            id = habitId,
            name = "Exercise",
            description = "Daily workout",
            color = "#FF5722",
            isActive = true,
            createdAt = "2024-01-01",
            intervalMinutes = 1440,
            scheduledTimes = "07:00"
        )
        coEvery { habitDao.getHabitById(habitId) } returns habitEntity

        // When
        val result = repository.getHabit(habitId)

        // Then
        assertEquals("Exercise", result?.name)
        coVerify { habitDao.getHabitById(habitId) }
    }

    @Test
    fun `getHabit should return null when not found`() = runTest {
        // Given
        val habitId = 1L
        coEvery { habitDao.getHabitById(habitId) } returns null

        // When
        val result = repository.getHabit(habitId)

        // Then
        assertNull(result)
        coVerify { habitDao.getHabitById(habitId) }
    }

    @Test
    fun `addHabitLog should call dao insertHabitLog and return id`() = runTest {
        // Given
        val habitLog = HabitLog(
            id = 0,
            habitId = 1L,
            date = LocalDate.parse("2024-01-01"),
            isCompleted = true
        )
        val expectedId = 1L
        coEvery { habitDao.insertHabitLog(any()) } returns expectedId

        // When
        val result = repository.addHabitLog(habitLog)

        // Then
        assertEquals(expectedId, result)
        coVerify { habitDao.insertHabitLog(any()) }
    }

    @Test
    fun `getHabitLog should return mapped log when found`() = runTest {
        // Given
        val habitId = 1L
        val date = LocalDate.parse("2024-01-01")
        val logEntity = LogEntity(
            id = 1,
            habitId = habitId,
            date = date.toString(),
            isCompleted = true
        )
        coEvery { habitDao.getHabitLog(habitId, date.toString()) } returns logEntity

        // When
        val result = repository.getHabitLog(habitId, date)

        // Then
        assertEquals(true, result?.isCompleted)
        assertEquals(habitId, result?.habitId)
        coVerify { habitDao.getHabitLog(habitId, date.toString()) }
    }

    @Test
    fun `getHabitLog should return null when not found`() = runTest {
        // Given
        val habitId = 1L
        val date = LocalDate.parse("2024-01-01")
        coEvery { habitDao.getHabitLog(habitId, date.toString()) } returns null

        // When
        val result = repository.getHabitLog(habitId, date)

        // Then
        assertNull(result)
        coVerify { habitDao.getHabitLog(habitId, date.toString()) }
    }
}