package com.nagopy.kmp.habittracker.data.mapper

import com.nagopy.kmp.habittracker.data.local.HabitEntity
import com.nagopy.kmp.habittracker.data.local.LogEntity
import com.nagopy.kmp.habittracker.domain.model.Habit
import com.nagopy.kmp.habittracker.domain.model.HabitLog
import com.nagopy.kmp.habittracker.domain.model.FrequencyType
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
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
            frequencyType = "ONCE_DAILY",
            intervalHours = 24,
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
        assertEquals(FrequencyType.ONCE_DAILY, domain.frequencyType)
        assertEquals(24, domain.intervalHours)
        assertEquals(2, domain.scheduledTimes.size)
        assertEquals(LocalTime(7, 0), domain.scheduledTimes[0])
        assertEquals(LocalTime(19, 0), domain.scheduledTimes[1])
    }

    @Test
    fun `HabitEntity toDomainModel should handle invalid frequency type`() {
        // Given
        val entity = HabitEntity(
            id = 1,
            name = "Exercise",
            description = "Daily workout",
            color = "#FF5722",
            isActive = true,
            createdAt = "2024-01-01",
            frequencyType = "INVALID_FREQUENCY",
            intervalHours = 24,
            scheduledTimes = "07:00"
        )

        // When
        val domain = entity.toDomainModel()

        // Then
        assertEquals(FrequencyType.ONCE_DAILY, domain.frequencyType) // Should default to ONCE_DAILY
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
            frequencyType = "ONCE_DAILY",
            intervalHours = 24,
            scheduledTimes = ""
        )

        // When
        val domain = entity.toDomainModel()

        // Then
        assertEquals(1, domain.scheduledTimes.size)
        assertEquals(LocalTime(9, 0), domain.scheduledTimes[0]) // Should default to 9:00
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
            frequencyType = "ONCE_DAILY",
            intervalHours = 24,
            scheduledTimes = "invalid,07:00,another_invalid"
        )

        // When
        val domain = entity.toDomainModel()

        // Then
        assertEquals(1, domain.scheduledTimes.size)
        assertEquals(LocalTime(7, 0), domain.scheduledTimes[0]) // Should only parse valid time
    }

    @Test
    fun `Habit toEntity should map correctly`() {
        // Given
        val domain = Habit(
            id = 1,
            name = "Exercise",
            description = "Daily workout",
            color = "#FF5722",
            isActive = true,
            createdAt = LocalDate.parse("2024-01-01"),
            frequencyType = FrequencyType.HOURLY,
            intervalHours = 2,
            scheduledTimes = listOf(LocalTime(7, 0), LocalTime(19, 30))
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
        assertEquals("HOURLY", entity.frequencyType)
        assertEquals(2, entity.intervalHours)
        assertEquals("07:00,19:30", entity.scheduledTimes)
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
            frequencyType = "ONCE_DAILY",
            intervalHours = 24,
            scheduledTimes = "7:5,12:30"
        )

        // When
        val domain = entity.toDomainModel()

        // Then
        assertEquals(2, domain.scheduledTimes.size)
        assertEquals(LocalTime(7, 5), domain.scheduledTimes[0])
        assertEquals(LocalTime(12, 30), domain.scheduledTimes[1])
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
            frequencyType = FrequencyType.ONCE_DAILY,
            intervalHours = 24,
            scheduledTimes = listOf(LocalTime(7, 5), LocalTime(12, 30))
        )

        // When
        val entity = domain.toEntity()

        // Then
        assertEquals("07:05,12:30", entity.scheduledTimes)
    }
}