package com.nagopy.kmp.habittracker.data.mapper

import com.nagopy.kmp.habittracker.data.local.HabitEntity
import com.nagopy.kmp.habittracker.data.local.LogEntity
import com.nagopy.kmp.habittracker.domain.model.Habit
import com.nagopy.kmp.habittracker.domain.model.HabitLog
import kotlinx.datetime.LocalDate

/**
 * Mappers to convert between domain models and data entities.
 */

// Habit mappers
fun HabitEntity.toDomainModel(): Habit {
    return Habit(
        id = id,
        name = name,
        description = description,
        color = color,
        isActive = isActive,
        createdAt = LocalDate.parse(createdAt)
    )
}

fun Habit.toEntity(): HabitEntity {
    return HabitEntity(
        id = id,
        name = name,
        description = description,
        color = color,
        isActive = isActive,
        createdAt = createdAt.toString()
    )
}

// HabitLog mappers
fun LogEntity.toDomainModel(): HabitLog {
    return HabitLog(
        id = id,
        habitId = habitId,
        date = LocalDate.parse(date),
        isCompleted = isCompleted
    )
}

fun HabitLog.toEntity(): LogEntity {
    return LogEntity(
        id = id,
        habitId = habitId,
        date = date.toString(),
        isCompleted = isCompleted
    )
}