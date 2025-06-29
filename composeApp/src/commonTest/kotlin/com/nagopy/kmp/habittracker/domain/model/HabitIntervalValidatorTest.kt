package com.nagopy.kmp.habittracker.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HabitIntervalValidatorTest {

    @Test
    fun `isValidIntervalMinutes should work correctly for INTERVAL frequency type`() {
        val validValues = listOf(1, 2, 3, 4, 5, 6, 10, 12, 15, 20, 30, 60, 120, 180, 240, 300, 360, 420, 480, 540, 600, 660, 720)
        val invalidValues = listOf(7, 8, 9, 11, 13, 14, 16, 17, 18, 19, 21, 25, 45, 90, 150, 210, 270, 330, 390, 450, 510, 570, 630, 690, 750)
        
        validValues.forEach { value ->
            assertTrue(
                HabitIntervalValidator.isValidIntervalMinutes(FrequencyType.INTERVAL, value),
                "Expected $value to be valid interval for INTERVAL type"
            )
        }
        
        invalidValues.forEach { value ->
            assertFalse(
                HabitIntervalValidator.isValidIntervalMinutes(FrequencyType.INTERVAL, value),
                "Expected $value to be invalid interval for INTERVAL type"
            )
        }
    }

    @Test
    fun `isValidIntervalMinutes should work correctly for INTERVAL frequency type with hourly values`() {
        val validHourlyValues = listOf(60, 120, 180, 240, 300, 360, 420, 480, 540, 600, 660, 720)
        val validMinuteValues = listOf(1, 2, 3, 4, 5, 6, 10, 12, 15, 20, 30)
        val invalidValues = listOf(7, 8, 9, 11, 13, 14, 16, 40, 50, 70, 90, 110, 130, 150, 170, 190, 210, 230, 250)
        
        (validHourlyValues + validMinuteValues).forEach { value ->
            assertTrue(
                HabitIntervalValidator.isValidIntervalMinutes(FrequencyType.INTERVAL, value),
                "Expected $value to be valid for INTERVAL type"
            )
        }
        
        invalidValues.forEach { value ->
            assertFalse(
                HabitIntervalValidator.isValidIntervalMinutes(FrequencyType.INTERVAL, value),
                "Expected $value to be invalid for INTERVAL type"
            )
        }
    }

    @Test
    fun `isValidIntervalMinutes should work correctly for ONCE_DAILY frequency type`() {
        assertTrue(
            HabitIntervalValidator.isValidIntervalMinutes(FrequencyType.ONCE_DAILY, 1440),
            "Expected 1440 to be valid for ONCE_DAILY type"
        )
        
        val invalidValues = listOf(720, 1200, 1439, 1441, 2880)
        
        invalidValues.forEach { value ->
            assertFalse(
                HabitIntervalValidator.isValidIntervalMinutes(FrequencyType.ONCE_DAILY, value),
                "Expected $value to be invalid for ONCE_DAILY type"
            )
        }
    }

    @Test
    fun `getClosestValidIntervalMinutes should work correctly for INTERVAL frequency type`() {
        assertEquals(1, HabitIntervalValidator.getClosestValidIntervalMinutes(FrequencyType.INTERVAL, 0))
        assertEquals(1, HabitIntervalValidator.getClosestValidIntervalMinutes(FrequencyType.INTERVAL, -5))
        assertEquals(6, HabitIntervalValidator.getClosestValidIntervalMinutes(FrequencyType.INTERVAL, 7))  // Closer to 6 than 5
        assertEquals(6, HabitIntervalValidator.getClosestValidIntervalMinutes(FrequencyType.INTERVAL, 8))  // Closer to 6 than 10
        assertEquals(10, HabitIntervalValidator.getClosestValidIntervalMinutes(FrequencyType.INTERVAL, 11)) // Closer to 10 than 12
        assertEquals(20, HabitIntervalValidator.getClosestValidIntervalMinutes(FrequencyType.INTERVAL, 25)) // Closer to 20 than 30
        assertEquals(30, HabitIntervalValidator.getClosestValidIntervalMinutes(FrequencyType.INTERVAL, 45)) // Closer to 30 than 60
        assertEquals(60, HabitIntervalValidator.getClosestValidIntervalMinutes(FrequencyType.INTERVAL, 90)) // Closer to 60 than 30
    }

    @Test
    fun `getClosestValidIntervalMinutes should work correctly for INTERVAL frequency type with hourly values`() {
        assertEquals(1, HabitIntervalValidator.getClosestValidIntervalMinutes(FrequencyType.INTERVAL, 0))
        assertEquals(1, HabitIntervalValidator.getClosestValidIntervalMinutes(FrequencyType.INTERVAL, -5))
        assertEquals(30, HabitIntervalValidator.getClosestValidIntervalMinutes(FrequencyType.INTERVAL, 35))  // Closer to 30 than 60
        assertEquals(30, HabitIntervalValidator.getClosestValidIntervalMinutes(FrequencyType.INTERVAL, 45))  // Closer to 30 than 60
        assertEquals(60, HabitIntervalValidator.getClosestValidIntervalMinutes(FrequencyType.INTERVAL, 90))  // Closer to 60 than 120
        assertEquals(120, HabitIntervalValidator.getClosestValidIntervalMinutes(FrequencyType.INTERVAL, 150)) // Equidistant to 120 and 180, algorithm picks first
        assertEquals(180, HabitIntervalValidator.getClosestValidIntervalMinutes(FrequencyType.INTERVAL, 210)) // Closer to 180 than 240
    }

    @Test
    fun `getClosestValidIntervalMinutes should work correctly for ONCE_DAILY frequency type`() {
        assertEquals(1440, HabitIntervalValidator.getClosestValidIntervalMinutes(FrequencyType.ONCE_DAILY, 0))
        assertEquals(1440, HabitIntervalValidator.getClosestValidIntervalMinutes(FrequencyType.ONCE_DAILY, 720))
        assertEquals(1440, HabitIntervalValidator.getClosestValidIntervalMinutes(FrequencyType.ONCE_DAILY, 1200))
        assertEquals(1440, HabitIntervalValidator.getClosestValidIntervalMinutes(FrequencyType.ONCE_DAILY, 1440))
        assertEquals(1440, HabitIntervalValidator.getClosestValidIntervalMinutes(FrequencyType.ONCE_DAILY, 2880))
    }


    @Test
    fun `VALID_INTERVAL_MINUTES should contain all divisors of 60`() {
        val expectedDivisors = listOf(1, 2, 3, 4, 5, 6, 10, 12, 15, 20, 30, 60, 120, 180, 240, 300, 360, 420, 480, 540, 600, 660, 720)
        assertEquals(expectedDivisors, HabitIntervalValidator.getAllValidIntervalMinutes())
    }

    @Test
    fun `VALID_ONCE_DAILY_MINUTES should be 1440`() {
        assertEquals(1440, HabitIntervalValidator.VALID_ONCE_DAILY_MINUTES)
    }

    @Test
    fun `all values in VALID_INTERVAL_MINUTES should actually divide 60 evenly`() {
        HabitIntervalValidator.getAllValidIntervalMinutes().forEach { value ->
            // For values <= 60, they should be divisors of 60
            // For values > 60, they should be multiples of 60
            if (value <= 60) {
                assertEquals(0, 60 % value, "60 should be divisible by $value")
            } else {
                assertEquals(0, value % 60, "$value should be divisible by 60")
            }
        }
    }
}