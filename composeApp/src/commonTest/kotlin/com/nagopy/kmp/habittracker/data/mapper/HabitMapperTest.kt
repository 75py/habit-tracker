package com.nagopy.kmp.habittracker.data.mapper

import com.nagopy.kmp.habittracker.data.local.HabitEntity
import com.nagopy.kmp.habittracker.data.local.LogEntity
import com.nagopy.kmp.habittracker.domain.model.Habit
import com.nagopy.kmp.habittracker.domain.model.HabitLog
import com.nagopy.kmp.habittracker.domain.model.FrequencyType
import com.nagopy.kmp.habittracker.domain.model.HabitDetail
import com.nagopy.kmp.habittracker.domain.model.frequencyType
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import com.nagopy.kmp.habittracker.util.TestLoggerConfig
import com.nagopy.kmp.habittracker.util.TestAntilog
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HabitMapperTest {

    @Test
    fun `HabitEntity toDomainModel should map correctly`() {
        // Given
        val entity = HabitEntity(
            id = 1,
            name = "Exercise",
            description = "Daily workout",
            color = "#FF5722",
            isActive = true,
            createdAt = "2024-01-01",
            intervalMinutes = 1440,
            scheduledTimes = "07:00,19:00"
        )

        // When
        val domain = entity.toDomainModel()

        // Then
        assertEquals(1, domain.id)
        assertEquals("Exercise", domain.name)
        assertEquals("Daily workout", domain.description)
        assertEquals("#FF5722", domain.color)
        assertEquals(true, domain.isActive)
        assertEquals(LocalDate.parse("2024-01-01"), domain.createdAt)
        assertEquals(FrequencyType.ONCE_DAILY, domain.frequencyType) // 1440 minutes should be ONCE_DAILY
        assertTrue(domain.detail is HabitDetail.OnceDailyHabitDetail)
        domain.detail.let {
            assertEquals(2, it.scheduledTimes.size) // Should have 2 scheduled times
            assertEquals(LocalTime(7, 0), it.scheduledTimes[0])
            assertEquals(LocalTime(19, 0), it.scheduledTimes[1])
        }
    }

    @Test
    fun `HabitEntity toDomainModel should auto-detect ONCE_DAILY for 1440 minutes`() {
        // Given
        val entity = HabitEntity(
            id = 1,
            name = "Exercise",
            description = "Daily workout",
            color = "#FF5722",
            isActive = true,
            createdAt = "2024-01-01",
            intervalMinutes = 1440, // 1440 minutes = 24 hours = ONCE_DAILY
            scheduledTimes = "07:00"
        )

        // When
        val domain = entity.toDomainModel()

        // Then
        assertEquals(FrequencyType.ONCE_DAILY, domain.frequencyType) // Should detect as ONCE_DAILY
    }

    @Test
    fun `HabitEntity toDomainModel should handle empty scheduled times`() {
        // Given
        val entity = HabitEntity(
            id = 1,
            name = "Exercise",
            description = "Daily workout",
            color = "#FF5722",
            isActive = true,
            createdAt = "2024-01-01",
            intervalMinutes = 1440,
            scheduledTimes = ""
        )

        // When
        val domain = entity.toDomainModel()

        // Then
        assertTrue(domain.detail is HabitDetail.OnceDailyHabitDetail)
        domain.detail.let {
            assertEquals(1, it.scheduledTimes.size) // Should default to one scheduled time
            assertEquals(LocalTime(9, 0), it.scheduledTimes[0]) // Default time should be 9:00
        }
    }

    @Test
    fun `HabitEntity toDomainModel should handle malformed scheduled times`() {
        // Given
        val entity = HabitEntity(
            id = 1,
            name = "Exercise",
            description = "Daily workout",
            color = "#FF5722",
            isActive = true,
            createdAt = "2024-01-01",
            intervalMinutes = 1440,
            scheduledTimes = "invalid,07:00,another_invalid"
        )

        // When
        val domain = entity.toDomainModel()

        // Then
        assertTrue(domain.detail is HabitDetail.OnceDailyHabitDetail)
        domain.detail.let {
            assertEquals(1, it.scheduledTimes.size) // Should only parse valid time
            assertEquals(LocalTime(7, 0), it.scheduledTimes[0]) // Should only parse valid time
        }
    }

    @Test
    fun `Habit toEntity should map correctly for INTERVAL frequency with hourly intervals`() {
        // Given
        val domain = Habit(
            id = 1,
            name = "Exercise",
            description = "Daily workout",
            color = "#FF5722",
            isActive = true,
            createdAt = LocalDate.parse("2024-01-01"),
            detail = HabitDetail.IntervalHabitDetail(
                intervalMinutes = 120, // 2 hours = 120 minutes
                startTime = LocalTime(7, 0), // INTERVAL uses startTime
                endTime = LocalTime(19, 30) // INTERVAL can have endTime
            )
        )

        // When
        val entity = domain.toEntity()

        // Then
        assertEquals(1, entity.id)
        assertEquals("Exercise", entity.name)
        assertEquals("Daily workout", entity.description)
        assertEquals("#FF5722", entity.color)
        assertEquals(true, entity.isActive)
        assertEquals("2024-01-01", entity.createdAt)
        assertEquals(120, entity.intervalMinutes) // 2 hours = 120 minutes
        assertEquals("", entity.scheduledTimes) // INTERVAL doesn't store scheduledTimes
        assertEquals("07:00", entity.startTime) // INTERVAL stores startTime
        assertEquals("19:30", entity.endTime) // INTERVAL stores endTime
    }

    @Test
    fun `Habit toEntity should map correctly for ONCE_DAILY frequency`() {
        // Given
        val domain = Habit(
            id = 1,
            name = "Exercise",
            description = "Daily workout",
            color = "#FF5722",
            isActive = true,
            createdAt = LocalDate.parse("2024-01-01"),
            detail = HabitDetail.OnceDailyHabitDetail(
                scheduledTimes = listOf(LocalTime(7, 0), LocalTime(19, 30)) // ONCE_DAILY uses scheduledTimes
            )
        )

        // When
        val entity = domain.toEntity()

        // Then
        assertEquals(1, entity.id)
        assertEquals("Exercise", entity.name)
        assertEquals("Daily workout", entity.description)
        assertEquals("#FF5722", entity.color)
        assertEquals(true, entity.isActive)
        assertEquals("2024-01-01", entity.createdAt)
        assertEquals(1440, entity.intervalMinutes) // 24 hours = 1440 minutes
        assertEquals("07:00,19:30", entity.scheduledTimes) // ONCE_DAILY stores scheduledTimes
        assertEquals(null, entity.startTime) // ONCE_DAILY doesn't store startTime
        assertEquals(null, entity.endTime) // ONCE_DAILY doesn't store endTime
    }

    @Test
    fun `LogEntity toDomainModel should map correctly`() {
        // Given
        val entity = LogEntity(
            id = 1,
            habitId = 2,
            date = "2024-01-01",
            isCompleted = true
        )

        // When
        val domain = entity.toDomainModel()

        // Then
        assertEquals(1, domain.id)
        assertEquals(2, domain.habitId)
        assertEquals(LocalDate.parse("2024-01-01"), domain.date)
        assertEquals(true, domain.isCompleted)
    }

    @Test
    fun `HabitLog toEntity should map correctly`() {
        // Given
        val domain = HabitLog(
            id = 1,
            habitId = 2,
            date = LocalDate.parse("2024-01-01"),
            isCompleted = false
        )

        // When
        val entity = domain.toEntity()

        // Then
        assertEquals(1, entity.id)
        assertEquals(2, entity.habitId)
        assertEquals("2024-01-01", entity.date)
        assertEquals(false, entity.isCompleted)
    }

    @Test
    fun `parseScheduledTimes should handle single digit hours and minutes`() {
        // Given
        val entity = HabitEntity(
            id = 1,
            name = "Exercise",
            description = "Daily workout",
            color = "#FF5722",
            isActive = true,
            createdAt = "2024-01-01",
            intervalMinutes = 1440,
            scheduledTimes = "7:5,12:30"
        )

        // When
        val domain = entity.toDomainModel()

        // Then
        assertTrue(domain.detail is HabitDetail.OnceDailyHabitDetail)
        domain.detail.let {
            assertEquals(2, it.scheduledTimes.size)
            assertEquals(LocalTime(7, 5), it.scheduledTimes[0])
            assertEquals(LocalTime(12, 30), it.scheduledTimes[1])
        }
    }

    @Test
    fun `formatScheduledTimes should pad single digits with zeros`() {
        // Given
        val domain = Habit(
            id = 1,
            name = "Exercise",
            description = "Daily workout",
            color = "#FF5722",
            isActive = true,
            createdAt = LocalDate.parse("2024-01-01"),
            detail = HabitDetail.OnceDailyHabitDetail(
                scheduledTimes = listOf(LocalTime(7, 5), LocalTime(12, 30)) // Times with single digit hours and minutes
            )
        )

        // When
        val entity = domain.toEntity()

        // Then
        assertEquals("07:05,12:30", entity.scheduledTimes)
    }

    @Test
    fun `HabitEntity toDomainModel should auto-detect INTERVAL for 60 minute intervals`() {
        // Given
        val entity = HabitEntity(
            id = 1,
            name = "Water",
            description = "Drink water",
            color = "#2196F3",
            isActive = true,
            createdAt = "2024-01-01",
            intervalMinutes = 60, // 60 minutes = 1 hour = INTERVAL
            scheduledTimes = "", // INTERVAL doesn't use scheduledTimes
            startTime = "09:00", // INTERVAL uses startTime
            endTime = null
        )

        // When
        val domain = entity.toDomainModel()

        // Then
        assertEquals(FrequencyType.INTERVAL, domain.frequencyType) // Should detect as INTERVAL
        assertTrue(domain.detail is HabitDetail.IntervalHabitDetail)
        domain.detail.let {
            assertEquals(60, it.intervalMinutes) // Should have 60 minute interval
            assertEquals(LocalTime(9, 0), it.startTime) // INTERVAL uses startTime
            assertEquals(null, it.endTime) // INTERVAL can have no end time
        }
    }

    @Test
    fun `HabitEntity toDomainModel should auto-detect INTERVAL for 120 minute intervals`() {
        // Given
        val entity = HabitEntity(
            id = 1,
            name = "Stretch",
            description = "Stretch every 2 hours",
            color = "#4CAF50",
            isActive = true,
            createdAt = "2024-01-01",
            intervalMinutes = 120, // 120 minutes = 2 hours = INTERVAL
            scheduledTimes = "", // INTERVAL doesn't use scheduledTimes
            startTime = "09:00", // INTERVAL uses startTime
            endTime = null
        )

        // When
        val domain = entity.toDomainModel()

        // Then
        assertEquals(FrequencyType.INTERVAL, domain.frequencyType) // Should detect as INTERVAL
        assertTrue(domain.detail is HabitDetail.IntervalHabitDetail)
        domain.detail.let {
            assertEquals(120, it.intervalMinutes) // Should have 120 minute interval
            assertEquals(LocalTime(9, 0), it.startTime) // INTERVAL uses startTime
            assertEquals(null, it.endTime) // INTERVAL can have no end time
        }
    }

    @Test
    fun `HabitEntity toDomainModel should auto-detect INTERVAL for non-hour intervals`() {
        // Given
        val entity = HabitEntity(
            id = 1,
            name = "Meditation",
            description = "Meditate every 30 minutes",
            color = "#FF9800",
            isActive = true,
            createdAt = "2024-01-01",
            intervalMinutes = 30, // 30 minutes = custom interval = INTERVAL
            scheduledTimes = "", // INTERVAL doesn't use scheduledTimes
            startTime = "09:00", // INTERVAL uses startTime
            endTime = null
        )

        // When
        val domain = entity.toDomainModel()

        // Then
        assertEquals(FrequencyType.INTERVAL, domain.frequencyType) // Should detect as INTERVAL
        assertTrue(domain.detail is HabitDetail.IntervalHabitDetail)
        domain.detail.let {
            assertEquals(30, it.intervalMinutes) // Should have 30 minute interval
            assertEquals(LocalTime(9, 0), it.startTime) // INTERVAL uses startTime
            assertEquals(null, it.endTime) // INTERVAL can have no end time
        }
    }

    @Test
    fun `HabitEntity toDomainModel should auto-detect INTERVAL for 30 minute intervals`() {
        // Given
        val entity = HabitEntity(
            id = 1,
            name = "Reading",
            description = "Read every 30 minutes",
            color = "#9C27B0",
            isActive = true,
            createdAt = "2024-01-01",
            intervalMinutes = 30, // 30 minutes = valid INTERVAL divisor of 60
            scheduledTimes = "", // INTERVAL doesn't use scheduledTimes
            startTime = "09:00", // INTERVAL uses startTime
            endTime = null
        )

        // When
        val domain = entity.toDomainModel()

        // Then
        assertEquals(FrequencyType.INTERVAL, domain.frequencyType) // Should detect as INTERVAL
        assertTrue(domain.detail is HabitDetail.IntervalHabitDetail)
        domain.detail.let {
            assertEquals(30, it.intervalMinutes) // Should have 30 minute interval
            assertEquals(LocalTime(9, 0), it.startTime) // INTERVAL uses startTime
            assertEquals(null, it.endTime) // INTERVAL can have no end time
        }
    }

    @Test
    fun `parseScheduledTimes should log error on malformed time`() {
        TestLoggerConfig.setupForTests()
        val entity = HabitEntity(
            id = 1,
            name = "Test",
            description = "",
            color = "#FFFFFF",
            isActive = true,
            createdAt = "2024-01-01",
            intervalMinutes = 1440,
            scheduledTimes = "07:xx"
        )

        entity.toDomainModel()

        val logged = TestAntilog.logs.any { it.contains("Failed to parse time") }
        TestLoggerConfig.tearDown()
        assertTrue(logged)
    }

    @Test
    fun `parseTime should log error on malformed start time`() {
        TestLoggerConfig.setupForTests()
        val entity = HabitEntity(
            id = 1,
            name = "Water",
            description = "",
            color = "#FFFFFF",
            isActive = true,
            createdAt = "2024-01-01",
            intervalMinutes = 60,
            scheduledTimes = "",
            startTime = "07:xx",
            endTime = null
        )

        entity.toDomainModel()

        val logged = TestAntilog.logs.any { it.contains("Failed to parse time") }
        TestLoggerConfig.tearDown()
        assertTrue(logged)
    }
}