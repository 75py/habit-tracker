package com.nagopy.kmp.habittracker.data

import com.nagopy.kmp.habittracker.data.local.HabitDao
import com.nagopy.kmp.habittracker.data.mapper.toDomainModel
import com.nagopy.kmp.habittracker.data.mapper.toEntity
import com.nagopy.kmp.habittracker.domain.model.Habit
import com.nagopy.kmp.habittracker.domain.model.HabitLog
import com.nagopy.kmp.habittracker.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

/**
 * Implementation of [HabitRepository] using Room database.
 */
class HabitRepositoryImpl(
    private val habitDao: HabitDao
) : HabitRepository {

    override suspend fun createHabit(habit: Habit): Long {
        return habitDao.insertHabit(habit.toEntity())
    }

    override suspend fun updateHabit(habit: Habit) {
        habitDao.updateHabit(habit.toEntity())
    }

    override suspend fun deleteHabit(habitId: Long) {
        habitDao.deleteHabitById(habitId)
    }

    override suspend fun getHabit(habitId: Long): Habit? {
        return habitDao.getHabitById(habitId)?.toDomainModel()
    }

    override fun getAllHabits(): Flow<List<Habit>> {
        return habitDao.getAllHabits().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getActiveHabits(): Flow<List<Habit>> {
        return habitDao.getActiveHabits().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun doesHabitExistByName(name: String): Boolean {
        return habitDao.doesHabitExistByName(name)
    }

    override suspend fun addHabitLog(habitLog: HabitLog): Long {
        return habitDao.insertHabitLog(habitLog.toEntity())
    }

    override suspend fun getHabitLog(habitId: Long, date: LocalDate): HabitLog? {
        return habitDao.getHabitLog(habitId, date.toString())?.toDomainModel()
    }
}