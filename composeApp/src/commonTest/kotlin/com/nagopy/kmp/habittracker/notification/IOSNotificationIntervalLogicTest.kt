package com.nagopy.kmp.habittracker.notification

import com.nagopy.kmp.habittracker.domain.model.FrequencyType
import com.nagopy.kmp.habittracker.domain.model.Habit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import com.nagopy.kmp.habittracker.domain.model.frequencyType
import com.nagopy.kmp.habittracker.domain.model.intervalMinutes
import com.nagopy.kmp.habittracker.domain.model.scheduledTimes
import com.nagopy.kmp.habittracker.domain.model.startTime
import com.nagopy.kmp.habittracker.domain.model.endTime

/**
 * Test for iOS notification interval logic using the new intervalMinutes constraints.
 */
class IOSNotificationIntervalLogicTest {

    @Test
    fun testIntervalNotificationMinutes_15Minutes() {
        // 15-minute intervals starting at 9:15 should create notifications at: 15, 30, 45, 0
        val habit = Habit(
            id = 1L,
            name = "Test Habit",
            createdAt = LocalDate(2024, 1, 1),
            frequencyType = FrequencyType.INTERVAL,
            intervalMinutes = 15,
            scheduledTimes = listOf(LocalTime(9, 15))
        )
        
        val expectedMinutes = setOf(15, 30, 45, 0)
        val actualMinutes = calculateIntervalMinutes(habit)
        
        assertEquals(expectedMinutes, actualMinutes)
    }

    @Test
    fun testIntervalNotificationMinutes_10Minutes() {
        // 10-minute intervals starting at 9:05 should create notifications at: 5, 15, 25, 35, 45, 55
        val habit = Habit(
            id = 1L,
            name = "Test Habit",
            createdAt = LocalDate(2024, 1, 1),
            frequencyType = FrequencyType.INTERVAL,
            intervalMinutes = 10,
            scheduledTimes = listOf(LocalTime(9, 5))
        )
        
        val expectedMinutes = setOf(5, 15, 25, 35, 45, 55)
        val actualMinutes = calculateIntervalMinutes(habit)
        
        assertEquals(expectedMinutes, actualMinutes)
    }

    @Test
    fun testIntervalNotificationMinutes_30Minutes() {
        // 30-minute intervals starting at 9:00 should create notifications at: 0, 30
        val habit = Habit(
            id = 1L,
            name = "Test Habit",
            createdAt = LocalDate(2024, 1, 1),
            frequencyType = FrequencyType.INTERVAL,
            intervalMinutes = 30,
            scheduledTimes = listOf(LocalTime(9, 0))
        )
        
        val expectedMinutes = setOf(0, 30)
        val actualMinutes = calculateIntervalMinutes(habit)
        
        assertEquals(expectedMinutes, actualMinutes)
    }

    @Test
    fun testIntervalNotificationMinutes_MultipleScheduledTimes() {
        // 20-minute intervals with multiple scheduled times should merge all possible minutes
        val habit = Habit(
            id = 1L,
            name = "Test Habit",
            createdAt = LocalDate(2024, 1, 1),
            frequencyType = FrequencyType.INTERVAL,
            intervalMinutes = 20,
            scheduledTimes = listOf(LocalTime(9, 10), LocalTime(10, 15))
        )
        
        // From 9:10: 10, 30, 50
        // From 10:15: 15, 35, 55
        // Combined: 10, 15, 30, 35, 50, 55
        val expectedMinutes = setOf(10, 15, 30, 35, 50, 55)
        val actualMinutes = calculateIntervalMinutes(habit)
        
        assertEquals(expectedMinutes, actualMinutes)
    }

    @Test
    fun testIntervalNotificationMinutes_AllValidIntervals() {
        val validIntervals = listOf(1, 2, 3, 4, 5, 6, 10, 12, 15, 20, 30, 60)
        
        validIntervals.forEach { intervalMinutes ->
            val habit = Habit(
                id = 1L,
                name = "Test Habit",
                createdAt = LocalDate(2024, 1, 1),
                frequencyType = FrequencyType.INTERVAL,
                intervalMinutes = intervalMinutes,
                scheduledTimes = listOf(LocalTime(9, 0))
            )
            
            val actualMinutes = calculateIntervalMinutes(habit)
            val expectedCount = 60 / intervalMinutes
            
            assertEquals(expectedCount, actualMinutes.size, "Interval $intervalMinutes should have $expectedCount notifications")
            assertTrue(actualMinutes.all { it in 0..59 }, "All minutes should be between 0-59 for interval $intervalMinutes")
        }
    }

    @Test
    fun testIntervalNotificationTimes_WithEndTime() {
        // 30-minute intervals starting at 9:00 with end time at 10:30
        // Should create notifications at: 9:00, 9:30, 10:00, 10:30
        val habit = Habit(
            id = 1L,
            name = "Test Habit",
            createdAt = LocalDate(2024, 1, 1),
            frequencyType = FrequencyType.INTERVAL,
            intervalMinutes = 30,
            scheduledTimes = listOf(LocalTime(9, 0)),
            endTime = LocalTime(10, 30)
        )
        
        val expectedTimes = setOf(
            LocalTime(9, 0),
            LocalTime(9, 30),
            LocalTime(10, 0),
            LocalTime(10, 30)
        )
        val actualTimes = calculateIntervalTimes(habit)
        
        assertEquals(expectedTimes, actualTimes)
    }

    @Test
    fun testIntervalNotificationTimes_WithEndTimeBeforeSecondInterval() {
        // 30-minute intervals starting at 9:00 with end time at 9:15
        // Should create only one notification at: 9:00
        val habit = Habit(
            id = 1L,
            name = "Test Habit",
            createdAt = LocalDate(2024, 1, 1),
            frequencyType = FrequencyType.INTERVAL,
            intervalMinutes = 30,
            scheduledTimes = listOf(LocalTime(9, 0)),
            endTime = LocalTime(9, 15)
        )
        
        val expectedTimes = setOf(LocalTime(9, 0))
        val actualTimes = calculateIntervalTimes(habit)
        
        assertEquals(expectedTimes, actualTimes)
    }

    @Test
    fun testIntervalNotificationTimes_NoEndTime() {
        // 30-minute intervals starting at 9:00 with no end time
        // Should create notifications throughout the day
        val habit = Habit(
            id = 1L,
            name = "Test Habit",
            createdAt = LocalDate(2024, 1, 1),
            frequencyType = FrequencyType.INTERVAL,
            intervalMinutes = 30,
            scheduledTimes = listOf(LocalTime(9, 0)),
            endTime = null
        )
        
        val actualTimes = calculateIntervalTimes(habit)
        
        // Should have many notifications throughout the day
        assertTrue(actualTimes.size > 10, "Should have many notifications throughout the day")
        assertTrue(actualTimes.contains(LocalTime(9, 0)), "Should include start time")
        assertTrue(actualTimes.any { it.hour == 23 }, "Should include notifications in the evening")
    }

    /**
     * Helper function that replicates the logic from IOSNotificationScheduler.createIntervalTriggers
     */
    private fun calculateIntervalMinutes(habit: Habit): Set<Int> {
        val intervalMinutes = habit.intervalMinutes
        val notificationMinutes = mutableSetOf<Int>()
        
        habit.scheduledTimes.forEach { scheduledTime ->
            val startMinute = scheduledTime.minute
            
            var currentMinute = startMinute
            repeat(60 / intervalMinutes) {
                notificationMinutes.add(currentMinute)
                currentMinute = (currentMinute + intervalMinutes) % 60
            }
        }
        
        return notificationMinutes
    }

    /**
     * Helper function that replicates the endTime logic from IOSNotificationScheduler.createIntervalTriggers
     */
    private fun calculateIntervalTimes(habit: Habit): Set<LocalTime> {
        val intervalMinutes = habit.intervalMinutes
        val endTime = habit.endTime ?: LocalTime(23, 59)
        
        val notificationTimes = mutableSetOf<LocalTime>()
        
        habit.scheduledTimes.forEach { startTime ->
            if (startTime <= endTime) {
                var currentTime = startTime
                
                while (currentTime <= endTime) {
                    notificationTimes.add(currentTime)
                    
                    val totalMinutes = currentTime.hour * 60 + currentTime.minute + intervalMinutes
                    val newHour = (totalMinutes / 60) % 24
                    val newMinute = totalMinutes % 60
                    currentTime = LocalTime(newHour, newMinute)
                    
                    if (totalMinutes >= 24 * 60) break
                }
            }
        }
        
        return notificationTimes
    }
}