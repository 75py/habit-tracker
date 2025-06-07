package com.nagopy.kmp.habittracker.data.local

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest

/**
 * Unit tests for [HabitDao] to verify database operations work correctly.
 */
class HabitDaoTest {

    // Note: In a real KMP project, we'd need to create the database differently
    // for each platform. For now, this provides the test structure.
    
    private fun createTestDatabase(): AppDatabase {
        // This is a placeholder - actual implementation would vary by platform
        return Room.inMemoryDatabaseBuilder<AppDatabase>()
            .build()
    }

    @Test
    fun insertAndGetHabit_shouldReturnSameHabit() = runTest {
        val database = createTestDatabase()
        val habitDao = database.habitDao()
        
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
        assertEquals(testHabit.name, retrievedHabit.name)
        assertEquals(testHabit.description, retrievedHabit.description)
        assertEquals(testHabit.color, retrievedHabit.color)
        assertEquals(testHabit.isActive, retrievedHabit.isActive)
        assertEquals(testHabit.createdAt, retrievedHabit.createdAt)
        
        database.close()
    }

    @Test
    fun updateHabit_shouldUpdateCorrectly() = runTest {
        val database = createTestDatabase()
        val habitDao = database.habitDao()
        
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
        assertEquals("Updated Name", retrievedHabit.name)
        assertEquals("Updated Description", retrievedHabit.description)
        assertEquals(originalHabit.color, retrievedHabit.color)
        
        database.close()
    }

    @Test
    fun deleteHabit_shouldRemoveHabit() = runTest {
        val database = createTestDatabase()
        val habitDao = database.habitDao()
        
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
        
        database.close()
    }

    @Test
    fun getAllHabits_shouldReturnAllHabits() = runTest {
        val database = createTestDatabase()
        val habitDao = database.habitDao()
        
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
        assertEquals(2, allHabits.size)
        
        database.close()
    }

    @Test
    fun getActiveHabits_shouldReturnOnlyActiveHabits() = runTest {
        val database = createTestDatabase()
        val habitDao = database.habitDao()
        
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
        
        database.close()
    }

    @Test
    fun insertAndGetHabitLog_shouldWork() = runTest {
        val database = createTestDatabase()
        val habitDao = database.habitDao()
        
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
        assertEquals(habitId, retrievedLog.habitId)
        assertEquals("2024-01-01", retrievedLog.date)
        assertTrue(retrievedLog.isCompleted)
        
        database.close()
    }

    @Test
    fun getHabitLogsForHabit_shouldReturnCorrectLogs() = runTest {
        val database = createTestDatabase()
        val habitDao = database.habitDao()
        
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
        
        database.close()
    }

    @Test
    fun deleteHabitLog_shouldRemoveLog() = runTest {
        val database = createTestDatabase()
        val habitDao = database.habitDao()
        
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
        
        database.close()
    }
}