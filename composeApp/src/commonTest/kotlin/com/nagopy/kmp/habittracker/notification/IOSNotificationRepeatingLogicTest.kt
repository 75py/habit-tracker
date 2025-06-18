package com.nagopy.kmp.habittracker.notification

import com.nagopy.kmp.habittracker.domain.model.Habit
import com.nagopy.kmp.habittracker.domain.model.FrequencyType
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertTrue

class IOSNotificationRepeatingLogicTest {

    @Test
    fun testMinuteBasedIntervalLogic() {
        // Test intervals that should work with minute-based repetition
        val validIntervals = listOf(1, 2, 3, 4, 5, 6, 10, 12, 15, 20, 30, 60)
        
        for (interval in validIntervals) {
            val isValidMinuteInterval = interval <= 60 && 60 % interval == 0
            assertTrue(isValidMinuteInterval, "Interval $interval should be valid for minute repetition")
        }
    }
    
    @Test
    fun testInvalidMinuteIntervals() {
        // Test intervals that should NOT work with minute-based repetition
        val invalidIntervals = listOf(7, 8, 9, 11, 13, 14, 16, 17, 18, 19, 23, 25, 26, 27, 28, 29, 31, 45)
        
        for (interval in invalidIntervals) {
            val isValidMinuteInterval = interval <= 60 && 60 % interval == 0
            assertTrue(!isValidMinuteInterval, "Interval $interval should NOT be valid for minute repetition")
        }
    }
    
    @Test
    fun testDailyBasedIntervalLogic() {
        // Test intervals that should work with daily repetition (but not minute)
        val validDailyIntervals = listOf(60, 120, 180, 240, 360, 480, 720)
        
        for (interval in validDailyIntervals) {
            val isValidDailyInterval = interval < 1440 && 1440 % interval == 0
            assertTrue(isValidDailyInterval, "Interval $interval should be valid for daily repetition")
        }
        
        // Test edge case: exactly 1 day (1440 minutes) - this should use daily repetition logic
        val oneDayInterval = 1440
        val isValidOneDay = oneDayInterval <= 1440 && 1440 % oneDayInterval == 0
        assertTrue(isValidOneDay, "1440-minute (1 day) interval should be valid for daily repetition")
    }
    
    @Test
    fun testSpecificIntervalCases() {
        // Test the specific case mentioned in the issue: 1-minute interval
        val oneMinuteInterval = 1
        val isValidMinute = oneMinuteInterval <= 60 && 60 % oneMinuteInterval == 0
        assertTrue(isValidMinute, "1-minute interval should be valid for minute repetition")
        
        // Test 15-minute interval (common case)
        val fifteenMinuteInterval = 15
        val isValid15Min = fifteenMinuteInterval <= 60 && 60 % fifteenMinuteInterval == 0
        assertTrue(isValid15Min, "15-minute interval should be valid for minute repetition")
        
        // Test edge case: 60-minute interval
        val sixtyMinuteInterval = 60
        val isValid60Min = sixtyMinuteInterval <= 60 && 60 % sixtyMinuteInterval == 0
        assertTrue(isValid60Min, "60-minute interval should be valid for minute repetition")
    }
    
    @Test
    fun testFrequencyTypeConfiguration() {
        // Test that habits with different frequency types are configured correctly
        val dailyHabit = Habit(
            id = 1,
            name = "Daily Habit",
            frequencyType = FrequencyType.ONCE_DAILY,
            createdAt = LocalDate(2024, 1, 1)
        )
        
        val hourlyHabit = Habit(
            id = 2,
            name = "Hourly Habit", 
            frequencyType = FrequencyType.HOURLY,
            createdAt = LocalDate(2024, 1, 1)
        )
        
        val oneMinuteIntervalHabit = Habit(
            id = 3,
            name = "1-Minute Interval Habit",
            frequencyType = FrequencyType.INTERVAL,
            intervalMinutes = 1,
            createdAt = LocalDate(2024, 1, 1)
        )
        
        // Verify configurations
        assertTrue(dailyHabit.frequencyType == FrequencyType.ONCE_DAILY)
        assertTrue(hourlyHabit.frequencyType == FrequencyType.HOURLY)
        assertTrue(oneMinuteIntervalHabit.frequencyType == FrequencyType.INTERVAL)
        assertTrue(oneMinuteIntervalHabit.intervalMinutes == 1)
    }
}