package com.nagopy.kmp.habittracker.notification

import com.nagopy.kmp.habittracker.domain.model.FrequencyType
import com.nagopy.kmp.habittracker.domain.model.Habit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Test for iOS notification 64-limit prioritization logic
 */
class IOSNotification64LimitTest {

    @Test
    fun testNotificationPrioritizationByTimeDistance() {
        val currentTime = LocalTime(10, 0) // 10:00 AM
        
        // Create habits that would generate many notifications
        val habits = listOf(
            // Hourly habit - generates 24 notifications (every hour at :30)
            Habit(
                id = 1L,
                name = "Hourly Habit",
                createdAt = LocalDate(2024, 1, 1),
                frequencyType = FrequencyType.HOURLY,
                intervalMinutes = 60,
                scheduledTimes = listOf(LocalTime(0, 30)) // Every hour at :30
            ),
            // 15-minute interval habit - generates 96 notifications (4 per hour * 24 hours)
            Habit(
                id = 2L,
                name = "15-min Interval Habit",
                createdAt = LocalDate(2024, 1, 1),
                frequencyType = FrequencyType.INTERVAL,
                intervalMinutes = 15,
                scheduledTimes = listOf(LocalTime(0, 0)) // 0, 15, 30, 45 minutes
            )
        )
        
        val allNotifications = mutableListOf<TestNotification>()
        
        habits.forEach { habit ->
            allNotifications.addAll(generateTestNotificationsForHabit(habit, currentTime))
        }
        
        // Should generate more than 64 notifications
        assertTrue(allNotifications.size > 64, "Should generate more than 64 notifications, got ${allNotifications.size}")
        
        // Sort by distance and take 64
        val prioritized = allNotifications
            .sortedBy { it.distanceFromNow }
            .take(64)
        
        assertEquals(64, prioritized.size, "Should limit to exactly 64 notifications")
        
        // Verify notifications are properly prioritized (closest times first)
        val firstNotification = prioritized.first()
        val lastNotification = prioritized.last()
        
        assertTrue(
            firstNotification.distanceFromNow <= lastNotification.distanceFromNow,
            "First notification (${firstNotification.distanceFromNow}min) should be closer than last (${lastNotification.distanceFromNow}min)"
        )
        
        // The interval habit has notifications at 10:00, 10:15, 10:30, etc.
        // Since currentTime is 10:00, the closest notification should be at 10:00 (0 minutes away)
        assertEquals(0, firstNotification.distanceFromNow, "Closest notification should be at 10:00 (0 minutes away)")
        assertTrue(firstNotification.time == LocalTime(10, 0), "Closest notification should be at 10:00")
    }

    @Test
    fun testTimeDistanceCalculation() {
        val currentTime = LocalTime(10, 0) // 10:00 AM
        
        // Test same day times
        assertEquals(30, calculateTestTimeDistance(currentTime, LocalTime(10, 30))) // 30 minutes later
        assertEquals(60, calculateTestTimeDistance(currentTime, LocalTime(11, 0))) // 1 hour later
        
        // Test next day times (times that have passed today)
        assertEquals(1380, calculateTestTimeDistance(currentTime, LocalTime(9, 0))) // 9:00 AM tomorrow (23 hours)
        assertEquals(1320, calculateTestTimeDistance(currentTime, LocalTime(8, 0))) // 8:00 AM tomorrow (22 hours)
    }

    @Test
    fun testSingleHabitUnder64Limit() {
        val currentTime = LocalTime(10, 0)
        
        // Create a simple daily habit - should only generate 1 notification
        val habit = Habit(
            id = 1L,
            name = "Daily Habit",
            createdAt = LocalDate(2024, 1, 1),
            frequencyType = FrequencyType.ONCE_DAILY,
            intervalMinutes = 1440,
            scheduledTimes = listOf(LocalTime(9, 0))
        )
        
        val notifications = generateTestNotificationsForHabit(habit, currentTime)
        
        assertEquals(1, notifications.size, "Daily habit should generate exactly 1 notification")
        assertEquals(1380, notifications.first().distanceFromNow, "Should be 23 hours away (next day)")
    }


    @Test
    fun testIntervalHabitWithEndTime() {
        val currentTime = LocalTime(10, 0)
        
        // 30-minute interval habit with end time at 12:00
        val habit = Habit(
            id = 1L,
            name = "Limited Interval Habit",
            createdAt = LocalDate(2024, 1, 1),
            frequencyType = FrequencyType.INTERVAL,
            intervalMinutes = 30,
            scheduledTimes = listOf(LocalTime(9, 0)),
            endTime = LocalTime(12, 0)
        )
        
        val notifications = generateTestNotificationsForHabit(habit, currentTime)
        
        // Debug: print all notification times
        val times = notifications.map { it.time }.sorted()
        println("Generated notification times: $times")
        
        // The habit starts at 9:00 and has 30-minute intervals until 12:00
        // Expected times: 9:00, 9:30, 10:00, 10:30, 11:00, 11:30, 12:00 = 7 notifications
        assertEquals(7, notifications.size, "Should generate 7 notifications until end time")
        
        val expectedTimes = listOf(
            LocalTime(9, 0), LocalTime(9, 30), LocalTime(10, 0), 
            LocalTime(10, 30), LocalTime(11, 0), LocalTime(11, 30), LocalTime(12, 0)
        )
        assertEquals(expectedTimes, times, "Should match expected notification times")
    }

    // Helper classes and functions for testing
    private data class TestNotification(
        val habit: Habit,
        val time: LocalTime,
        val identifier: String,
        val distanceFromNow: Long
    )

    private fun generateTestNotificationsForHabit(habit: Habit, currentTime: LocalTime): List<TestNotification> {
        val notifications = mutableListOf<TestNotification>()
        
        when (habit.frequencyType) {
            FrequencyType.ONCE_DAILY -> {
                habit.scheduledTimes.forEachIndexed { index, scheduledTime ->
                    val identifier = "habit_${habit.id}_daily_$index"
                    val distance = calculateTestTimeDistance(currentTime, scheduledTime)
                    notifications.add(
                        TestNotification(habit, scheduledTime, identifier, distance)
                    )
                }
            }
            FrequencyType.HOURLY -> {
                val startTime = habit.scheduledTimes.firstOrNull() ?: return notifications
                val endTime = habit.endTime ?: LocalTime(23, 59)
                val minute = startTime.minute
                
                var currentHour = startTime.hour
                var triggerIndex = 0
                
                while (true) {
                    val time = LocalTime(currentHour, minute)
                    if (time > endTime) break
                    
                    val identifier = "habit_${habit.id}_hourly_$triggerIndex"
                    val distance = calculateTestTimeDistance(currentTime, time)
                    notifications.add(
                        TestNotification(habit, time, identifier, distance)
                    )
                    
                    currentHour++
                    if (currentHour > 23) currentHour = 0
                    triggerIndex++
                    if (triggerIndex >= 24) break
                }
            }
            FrequencyType.INTERVAL -> {
                val intervalMinutes = habit.intervalMinutes
                val endTime = habit.endTime ?: LocalTime(23, 59)
                
                val notificationTimes = mutableSetOf<LocalTime>()
                
                habit.scheduledTimes.forEach { startTime ->
                    if (startTime <= endTime) {
                        var time = startTime
                        
                        while (time <= endTime) {
                            notificationTimes.add(time)
                            
                            val totalMinutes = time.hour * 60 + time.minute + intervalMinutes
                            val newHour = (totalMinutes / 60) % 24
                            val newMinute = totalMinutes % 60
                            time = LocalTime(newHour, newMinute)
                            
                            if (totalMinutes >= 24 * 60) break
                        }
                    }
                }
                
                notificationTimes.forEachIndexed { index, time ->
                    val identifier = "habit_${habit.id}_interval_${time.hour}_${time.minute}"
                    val distance = calculateTestTimeDistance(currentTime, time)
                    notifications.add(
                        TestNotification(habit, time, identifier, distance)
                    )
                }
            }
        }
        
        return notifications
    }

    private fun calculateTestTimeDistance(currentTime: LocalTime, targetTime: LocalTime): Long {
        val currentMinutes = currentTime.hour * 60 + currentTime.minute
        val targetMinutes = targetTime.hour * 60 + targetTime.minute
        
        return if (targetMinutes >= currentMinutes) {
            // Today
            (targetMinutes - currentMinutes).toLong()
        } else {
            // Tomorrow
            (24 * 60 - currentMinutes + targetMinutes).toLong()
        }
    }
}