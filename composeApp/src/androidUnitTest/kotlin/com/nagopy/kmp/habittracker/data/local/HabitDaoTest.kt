package com.nagopy.kmp.habittracker.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests for [HabitDao] to verify database operations work correctly.
 */
@RunWith(RobolectricTestRunner::class)
class HabitDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var habitDao: HabitDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).build()
        habitDao = database.habitDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndGetHabit_shouldReturnSameHabit() = runTest {
        val testHabit = HabitEntity(
            name = "Test Habit",
            description = "Test Description",
            color = "#FF5722",
            isActive = true,
            createdAt = "2024-01-01"
        )
        
        val habitId = habitDao.insertHabit(testHabit)
        val retrievedHabit = habitDao.getHabitById(habitId)
        
        assertNotNull(retrievedHabit)
        assertEquals(testHabit.name, retrievedHabit!!.name)
        assertEquals(testHabit.description, retrievedHabit.description)
        assertEquals(testHabit.color, retrievedHabit.color)
        assertEquals(testHabit.isActive, retrievedHabit.isActive)
        assertEquals(testHabit.createdAt, retrievedHabit.createdAt)
    }

    @Test
    fun updateHabit_shouldUpdateCorrectly() = runTest {
        val originalHabit = HabitEntity(
            name = "Original Name",
            description = "Original Description",
            color = "#FF5722",
            isActive = true,
            createdAt = "2024-01-01"
        )
        
        val habitId = habitDao.insertHabit(originalHabit)
        val updatedHabit = originalHabit.copy(
            id = habitId,
            name = "Updated Name",
            description = "Updated Description"
        )
        
        habitDao.updateHabit(updatedHabit)
        val retrievedHabit = habitDao.getHabitById(habitId)
        
        assertNotNull(retrievedHabit)
        assertEquals("Updated Name", retrievedHabit!!.name)
        assertEquals("Updated Description", retrievedHabit.description)
        assertEquals(originalHabit.color, retrievedHabit.color)
    }

    @Test
    fun deleteHabit_shouldRemoveHabit() = runTest {
        val testHabit = HabitEntity(
            name = "Test Habit",
            description = "Test Description",
            color = "#FF5722",
            isActive = true,
            createdAt = "2024-01-01"
        )
        
        val habitId = habitDao.insertHabit(testHabit)
        assertNotNull(habitDao.getHabitById(habitId))
        
        habitDao.deleteHabitById(habitId)
        assertNull(habitDao.getHabitById(habitId))
    }

    @Test
    fun getAllHabits_shouldReturnAllHabits() = runTest {
        val habit1 = HabitEntity(
            name = "Habit 1",
            description = "Description 1",
            color = "#FF5722",
            isActive = true,
            createdAt = "2024-01-01"
        )
        
        val habit2 = HabitEntity(
            name = "Habit 2",
            description = "Description 2",
            color = "#2196F3",
            isActive = false,
            createdAt = "2024-01-02"
        )
        
        habitDao.insertHabit(habit1)
        habitDao.insertHabit(habit2)
        
        val allHabits = habitDao.getAllHabits().first()
        assertEquals(1, allHabits.size)
    }

    @Test
    fun getActiveHabits_shouldReturnOnlyActiveHabits() = runTest {
        val activeHabit = HabitEntity(
            name = "Active Habit",
            description = "Active Description",
            color = "#FF5722",
            isActive = true,
            createdAt = "2024-01-01"
        )
        
        val inactiveHabit = HabitEntity(
            name = "Inactive Habit",
            description = "Inactive Description",
            color = "#2196F3",
            isActive = false,
            createdAt = "2024-01-02"
        )
        
        habitDao.insertHabit(activeHabit)
        habitDao.insertHabit(inactiveHabit)
        
        val activeHabits = habitDao.getActiveHabits().first()
        assertEquals(1, activeHabits.size)
        assertTrue(activeHabits.first().isActive)
    }

    @Test
    fun insertAndGetHabitLog_shouldWork() = runTest {
        // First create a habit
        val habit = HabitEntity(
            name = "Test Habit",
            description = "Test Description",
            color = "#FF5722",
            isActive = true,
            createdAt = "2024-01-01"
        )
        val habitId = habitDao.insertHabit(habit)
        
        // Then create a log entry
        val habitLog = LogEntity(
            habitId = habitId,
            date = "2024-01-01",
            isCompleted = true
        )
        
        val logId = habitDao.insertHabitLog(habitLog)
        val retrievedLog = habitDao.getHabitLog(habitId, "2024-01-01")
        
        assertNotNull(retrievedLog)
        assertEquals(habitId, retrievedLog!!.habitId)
        assertEquals("2024-01-01", retrievedLog.date)
        assertTrue(retrievedLog.isCompleted)
    }

    @Test
    fun getHabitLogsForHabit_shouldReturnCorrectLogs() = runTest {
        // Create a habit
        val habit = HabitEntity(
            name = "Test Habit",
            description = "Test Description", 
            color = "#FF5722",
            isActive = true,
            createdAt = "2024-01-01"
        )
        val habitId = habitDao.insertHabit(habit)
        
        // Create multiple log entries
        val log1 = LogEntity(habitId = habitId, date = "2024-01-01", isCompleted = true)
        val log2 = LogEntity(habitId = habitId, date = "2024-01-02", isCompleted = false)
        val log3 = LogEntity(habitId = habitId, date = "2024-01-03", isCompleted = true)
        
        habitDao.insertHabitLog(log1)
        habitDao.insertHabitLog(log2)
        habitDao.insertHabitLog(log3)
        
        val logs = habitDao.getHabitLogsForHabit(habitId).first()
        assertEquals(3, logs.size)
    }

    @Test
    fun deleteHabitLog_shouldRemoveLog() = runTest {
        // Create a habit
        val habit = HabitEntity(
            name = "Test Habit",
            description = "Test Description",
            color = "#FF5722", 
            isActive = true,
            createdAt = "2024-01-01"
        )
        val habitId = habitDao.insertHabit(habit)
        
        // Create a log entry
        val habitLog = LogEntity(
            habitId = habitId,
            date = "2024-01-01",
            isCompleted = true
        )
        
        habitDao.insertHabitLog(habitLog)
        assertNotNull(habitDao.getHabitLog(habitId, "2024-01-01"))
        
        habitDao.deleteHabitLog(habitId, "2024-01-01")
        assertNull(habitDao.getHabitLog(habitId, "2024-01-01"))
    }
}