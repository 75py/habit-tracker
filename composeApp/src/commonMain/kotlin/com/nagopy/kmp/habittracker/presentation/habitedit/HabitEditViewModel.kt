package com.nagopy.kmp.habittracker.presentation.habitedit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nagopy.kmp.habittracker.domain.model.Habit
import com.nagopy.kmp.habittracker.domain.model.FrequencyType
import com.nagopy.kmp.habittracker.domain.model.HabitIntervalValidator
import com.nagopy.kmp.habittracker.domain.usecase.AddHabitUseCase
import com.nagopy.kmp.habittracker.domain.usecase.UpdateHabitUseCase
import com.nagopy.kmp.habittracker.domain.usecase.GetHabitUseCase
import com.nagopy.kmp.habittracker.domain.usecase.ManageNotificationsUseCase
import com.nagopy.kmp.habittracker.domain.usecase.ScheduleNextNotificationUseCase
import com.nagopy.kmp.habittracker.util.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/**
 * ViewModel for the Habit Edit screen.
 * Manages the state of habit form and handles user interactions for adding/editing habits.
 */
class HabitEditViewModel(
    private val addHabitUseCase: AddHabitUseCase,
    private val updateHabitUseCase: UpdateHabitUseCase,
    private val getHabitUseCase: GetHabitUseCase,
    private val manageNotificationsUseCase: ManageNotificationsUseCase,
    private val scheduleNextNotificationUseCase: ScheduleNextNotificationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HabitEditUiState())
    val uiState: StateFlow<HabitEditUiState> = _uiState.asStateFlow()

    fun loadHabitForEdit(habitId: Long) {
        if (_uiState.value.editHabitId == habitId) {
            // Already loaded this habit
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, editHabitId = habitId)
            
            try {
                val habit = getHabitUseCase(habitId)
                if (habit != null) {
                    // Determine the appropriate time unit based on interval minutes
                    val intervalUnit = if (habit.intervalMinutes % 60 == 0) {
                        TimeUnit.HOURS
                    } else {
                        TimeUnit.MINUTES
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        editHabitId = habitId,
                        name = habit.name,
                        description = habit.description,
                        color = habit.color,
                        isActive = habit.isActive,
                        createdAt = habit.createdAt,
                        frequencyType = habit.frequencyType,
                        intervalMinutes = habit.intervalMinutes,
                        intervalUnit = intervalUnit,
                        scheduledTimes = habit.scheduledTimes,
                        endTime = habit.endTime,
                        isLoading = false
                    )
                } else {
                    Logger.d("Habit with ID $habitId not found", tag = "HabitEdit")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        saveError = "Habit not found"
                    )
                }
            } catch (exception: Exception) {
                Logger.e(exception, "Failed to load habit for editing", tag = "HabitEdit")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    saveError = exception.message ?: "Failed to load habit"
                )
            }
        }
    }

    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(
            name = name,
            nameError = if (name.isBlank()) "Name is required" else null
        )
    }

    fun updateDescription(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }

    fun updateColor(color: String) {
        _uiState.value = _uiState.value.copy(color = color)
    }

    fun updateIsActive(isActive: Boolean) {
        _uiState.value = _uiState.value.copy(isActive = isActive)
    }

    fun updateFrequencyType(frequencyType: FrequencyType) {
        val currentState = _uiState.value
        val newIntervalMinutes = when (frequencyType) {
            FrequencyType.HOURLY -> 60  // Default to 1 hour
            FrequencyType.ONCE_DAILY -> 1440  // Default to 24 hours
            FrequencyType.INTERVAL -> currentState.intervalMinutes
        }
        
        _uiState.value = currentState.copy(
            frequencyType = frequencyType,
            intervalMinutes = newIntervalMinutes
        )
    }

    fun updateIntervalMinutes(intervalMinutes: Int) {
        _uiState.value = _uiState.value.copy(intervalMinutes = intervalMinutes)
    }
    
    fun updateIntervalValue(value: Int, unit: TimeUnit) {
        val intervalMinutes = when (unit) {
            TimeUnit.MINUTES -> value
            TimeUnit.HOURS -> value * 60
        }
        
        // Validate and auto-correct interval minutes based on frequency type
        val currentState = _uiState.value
        val validatedIntervalMinutes = if (HabitIntervalValidator.isValidIntervalMinutes(currentState.frequencyType, intervalMinutes)) {
            intervalMinutes
        } else {
            // Use the closest valid value for the frequency type
            HabitIntervalValidator.getClosestValidIntervalMinutes(currentState.frequencyType, intervalMinutes)
        }
        
        // Recalculate unit and value based on validated interval minutes
        val finalUnit = if (validatedIntervalMinutes != intervalMinutes) {
            // If we changed the value, determine the best unit to display
            if (validatedIntervalMinutes % 60 == 0) TimeUnit.HOURS else TimeUnit.MINUTES
        } else {
            unit
        }
        
        val finalValue = when (finalUnit) {
            TimeUnit.MINUTES -> validatedIntervalMinutes
            TimeUnit.HOURS -> validatedIntervalMinutes / 60
        }
        
        _uiState.value = currentState.copy(
            intervalMinutes = validatedIntervalMinutes,
            intervalUnit = finalUnit
        )
    }
    
    fun updateIntervalUnit(unit: TimeUnit) {
        _uiState.value = _uiState.value.copy(intervalUnit = unit)
    }

    fun updateScheduledTimes(scheduledTimes: List<LocalTime>) {
        _uiState.value = _uiState.value.copy(scheduledTimes = scheduledTimes)
    }

    fun updateEndTime(endTime: LocalTime?) {
        _uiState.value = _uiState.value.copy(endTime = endTime)
    }

    fun saveHabit(onSuccess: (Long) -> Unit, onError: (String) -> Unit) {
        val currentState = _uiState.value
        
        // Validate form
        if (currentState.name.isBlank()) {
            _uiState.value = currentState.copy(nameError = "Name is required")
            return
        }

        _uiState.value = currentState.copy(isSaving = true, saveError = null)

        viewModelScope.launch {
            try {
                val isEditing = currentState.editHabitId != null
                
                if (isEditing) {
                    Logger.d("Updating habit: ${currentState.name}", tag = "HabitEdit")
                    
                    val habit = Habit(
                        id = currentState.editHabitId!!,
                        name = currentState.name.trim(),
                        description = currentState.description.trim(),
                        color = currentState.color,
                        isActive = currentState.isActive,
                        createdAt = currentState.createdAt ?: Clock.System.todayIn(TimeZone.currentSystemDefault()),
                        frequencyType = currentState.frequencyType,
                        intervalMinutes = currentState.intervalMinutes,
                        scheduledTimes = currentState.scheduledTimes,
                        endTime = currentState.endTime
                    )

                    updateHabitUseCase(habit)
                    val habitId = currentState.editHabitId
                    
                    // Schedule notifications for the updated habit
                    try {
                        scheduleNextNotificationUseCase.scheduleNextNotificationForHabit(habitId)
                    } catch (notificationException: Exception) {
                        // Notifications are not critical, so we don't fail the save operation
                        Logger.e(notificationException, "Failed to schedule notifications for updated habit", tag = "HabitEdit")
                    }
                    
                    _uiState.value = currentState.copy(isSaving = false)
                    onSuccess(habitId)
                } else {
                    Logger.d("Creating new habit: ${currentState.name}", tag = "HabitEdit")
                    
                    val habit = Habit(
                        name = currentState.name.trim(),
                        description = currentState.description.trim(),
                        color = currentState.color,
                        isActive = currentState.isActive,
                        createdAt = Clock.System.todayIn(TimeZone.currentSystemDefault()),
                        frequencyType = currentState.frequencyType,
                        intervalMinutes = currentState.intervalMinutes,
                        scheduledTimes = currentState.scheduledTimes,
                        endTime = currentState.endTime
                    )

                    val habitId = addHabitUseCase(habit)
                    
                    // Schedule notifications for the new habit
                    try {
                        scheduleNextNotificationUseCase.scheduleNextNotificationForHabit(habitId)
                    } catch (notificationException: Exception) {
                        // Notifications are not critical, so we don't fail the save operation
                        Logger.e(notificationException, "Failed to schedule notifications for new habit", tag = "HabitEdit")
                    }
                    
                    _uiState.value = currentState.copy(isSaving = false)
                    onSuccess(habitId)
                }
            } catch (exception: Exception) {
                Logger.e(exception, "Failed to save habit", tag = "HabitEdit")
                val errorMessage = exception.message ?: "Failed to save habit"
                _uiState.value = currentState.copy(
                    isSaving = false,
                    saveError = errorMessage
                )
                onError(errorMessage)
            }
        }
    }

    fun clearErrors() {
        _uiState.value = _uiState.value.copy(
            nameError = null,
            saveError = null
        )
    }
}

/**
 * Time unit for interval specification
 */
enum class TimeUnit {
    MINUTES, HOURS
}

/**
 * UI state for the Habit Edit screen
 */
data class HabitEditUiState(
    val editHabitId: Long? = null, // null for adding, habitId for editing
    val name: String = "",
    val description: String = "",
    val color: String = "#2196F3", // Default blue color
    val isActive: Boolean = true,
    val createdAt: LocalDate? = null, // Original creation date for editing
    val frequencyType: FrequencyType = FrequencyType.ONCE_DAILY,
    val intervalMinutes: Int = 1440, // Default 24 hours = 1440 minutes
    val intervalUnit: TimeUnit = TimeUnit.HOURS, // Default to hours for user convenience
    val scheduledTimes: List<LocalTime> = listOf(LocalTime(9, 0)),
    val endTime: LocalTime? = null, // End time for interval-based habits
    val nameError: String? = null,
    val saveError: String? = null,
    val isSaving: Boolean = false,
    val isLoading: Boolean = false
) {
    // Helper property to get the interval value in the selected unit
    val intervalValue: Int
        get() = when (intervalUnit) {
            TimeUnit.MINUTES -> intervalMinutes
            TimeUnit.HOURS -> intervalMinutes / 60
        }
}
