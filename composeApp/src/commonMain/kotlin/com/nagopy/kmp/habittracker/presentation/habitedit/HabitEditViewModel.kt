package com.nagopy.kmp.habittracker.presentation.habitedit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nagopy.kmp.habittracker.domain.model.Habit
import com.nagopy.kmp.habittracker.domain.model.HabitBase
import com.nagopy.kmp.habittracker.domain.model.DailyHabit
import com.nagopy.kmp.habittracker.domain.model.HourlyHabit
import com.nagopy.kmp.habittracker.domain.model.IntervalHabit
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

    private val _uiState = MutableStateFlow<HabitEditUiState>(HabitEditUiState.Content())
    val uiState: StateFlow<HabitEditUiState> = _uiState.asStateFlow()

    fun loadHabitForEdit(habitId: Long) {
        val currentState = _uiState.value
        if (currentState is HabitEditUiState.Content && currentState.editHabitId == habitId) {
            // Already loaded this habit
            return
        }
        
        viewModelScope.launch {
            _uiState.value = HabitEditUiState.Loading
            
            try {
                val habit = getHabitUseCase(habitId)
                if (habit != null) {
                    // Determine the appropriate time unit based on interval minutes and type
                    val (intervalMinutes, intervalUnit) = when (habit) {
                        is DailyHabit -> {
                            HabitIntervalValidator.VALID_ONCE_DAILY_MINUTES to TimeUnit.HOURS
                        }
                        is HourlyHabit -> {
                            habit.intervalMinutes to TimeUnit.HOURS
                        }
                        is IntervalHabit -> {
                            if (habit.intervalMinutes % 60 == 0) {
                                habit.intervalMinutes to TimeUnit.HOURS
                            } else {
                                habit.intervalMinutes to TimeUnit.MINUTES
                            }
                        }
                    }
                    
                    val (scheduledTimes, startTime, endTime) = when (habit) {
                        is DailyHabit -> Triple(habit.scheduledTimes, null, null)
                        is HourlyHabit -> Triple(emptyList(), habit.startTime, habit.endTime)
                        is IntervalHabit -> Triple(emptyList(), habit.startTime, habit.endTime)
                    }
                    
                    _uiState.value = HabitEditUiState.Content(
                        editHabitId = habitId,
                        name = habit.name,
                        description = habit.description,
                        color = habit.color,
                        isActive = habit.isActive,
                        createdAt = habit.createdAt,
                        frequencyType = habit.frequencyType,
                        intervalMinutes = intervalMinutes,
                        intervalUnit = intervalUnit,
                        scheduledTimes = scheduledTimes,
                        startTime = startTime,
                        endTime = endTime
                    )
                } else {
                    Logger.d("Habit with ID $habitId not found", tag = "HabitEdit")
                    _uiState.value = HabitEditUiState.Error(
                        message = "Habit not found"
                    )
                }
            } catch (exception: Exception) {
                Logger.e(exception, "Failed to load habit for editing", tag = "HabitEdit")
                _uiState.value = HabitEditUiState.Error(
                    message = exception.message ?: "Failed to load habit"
                )
            }
        }
    }

    fun updateName(name: String) {
        val currentState = _uiState.value
        if (currentState is HabitEditUiState.Content) {
            _uiState.value = currentState.copy(
                name = name,
                nameError = if (name.isBlank()) "Name is required" else null
            )
        }
    }

    fun updateDescription(description: String) {
        val currentState = _uiState.value
        if (currentState is HabitEditUiState.Content) {
            _uiState.value = currentState.copy(description = description)
        }
    }

    fun updateColor(color: String) {
        val currentState = _uiState.value
        if (currentState is HabitEditUiState.Content) {
            _uiState.value = currentState.copy(color = color)
        }
    }

    fun updateIsActive(isActive: Boolean) {
        val currentState = _uiState.value
        if (currentState is HabitEditUiState.Content) {
            _uiState.value = currentState.copy(isActive = isActive)
        }
    }

    fun updateFrequencyType(frequencyType: FrequencyType) {
        val currentState = _uiState.value
        if (currentState is HabitEditUiState.Content) {
            val newIntervalMinutes = when (frequencyType) {
                FrequencyType.HOURLY -> 60  // Default to 1 hour
                FrequencyType.ONCE_DAILY -> 1440  // Default to 24 hours
                FrequencyType.INTERVAL -> currentState.intervalMinutes
            }
            
            // When switching frequency types, clear inappropriate data
            val newScheduledTimes = if (frequencyType == FrequencyType.ONCE_DAILY) {
                if (currentState.scheduledTimes.isEmpty()) {
                    listOf(LocalTime(9, 0)) // Default for ONCE_DAILY
                } else {
                    currentState.scheduledTimes
                }
            } else {
                emptyList() // Clear for HOURLY/INTERVAL
            }
            
            val newStartTime = if (frequencyType != FrequencyType.ONCE_DAILY) {
                currentState.startTime ?: LocalTime(9, 0) // Default for HOURLY/INTERVAL
            } else {
                null // Clear for ONCE_DAILY
            }
            
            val newEndTime = if (frequencyType != FrequencyType.ONCE_DAILY) {
                currentState.endTime // Keep current end time for HOURLY/INTERVAL
            } else {
                null // Clear for ONCE_DAILY
            }
            
            _uiState.value = currentState.copy(
                frequencyType = frequencyType,
                intervalMinutes = newIntervalMinutes,
                scheduledTimes = newScheduledTimes,
                startTime = newStartTime,
                endTime = newEndTime
            )
        }
    }

    fun updateIntervalMinutes(intervalMinutes: Int) {
        val currentState = _uiState.value
        if (currentState is HabitEditUiState.Content) {
            _uiState.value = currentState.copy(intervalMinutes = intervalMinutes)
        }
    }
    
    fun updateIntervalValue(value: Int, unit: TimeUnit) {
        val currentState = _uiState.value
        if (currentState is HabitEditUiState.Content) {
            val intervalMinutes = when (unit) {
                TimeUnit.MINUTES -> value
                TimeUnit.HOURS -> value * 60
            }
            
            // Validate and auto-correct interval minutes based on frequency type
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
    }
    
    fun updateIntervalUnit(unit: TimeUnit) {
        val currentState = _uiState.value
        if (currentState is HabitEditUiState.Content) {
            _uiState.value = currentState.copy(intervalUnit = unit)
        }
    }

    fun updateScheduledTimes(scheduledTimes: List<LocalTime>) {
        val currentState = _uiState.value
        if (currentState is HabitEditUiState.Content) {
            _uiState.value = currentState.copy(scheduledTimes = scheduledTimes)
        }
    }

    fun updateStartTime(startTime: LocalTime?) {
        val currentState = _uiState.value
        if (currentState is HabitEditUiState.Content) {
            _uiState.value = currentState.copy(startTime = startTime)
        }
    }

    fun updateEndTime(endTime: LocalTime?) {
        val currentState = _uiState.value
        if (currentState is HabitEditUiState.Content) {
            _uiState.value = currentState.copy(endTime = endTime)
        }
    }

    fun saveHabit(onSuccess: (Long) -> Unit, onError: (String) -> Unit) {
        val currentState = _uiState.value
        if (currentState !is HabitEditUiState.Content) {
            onError("Invalid state for saving")
            return
        }
        
        // Validate form
        if (currentState.name.isBlank()) {
            _uiState.value = currentState.copy(nameError = "Name is required")
            return
        }
        
        // Validate scheduled times based on frequency type
        when (currentState.frequencyType) {
            FrequencyType.ONCE_DAILY -> {
                if (currentState.scheduledTimes.isEmpty()) {
                    _uiState.value = currentState.copy(saveError = "At least one scheduled time is required")
                    return
                }
            }
            FrequencyType.HOURLY, FrequencyType.INTERVAL -> {
                if (currentState.startTime == null) {
                    _uiState.value = currentState.copy(saveError = "Start time is required")
                    return
                }
            }
        }

        _uiState.value = currentState.copy(isSaving = true, saveError = null)

        viewModelScope.launch {
            try {
                val isEditing = currentState.editHabitId != null
                
                if (isEditing) {
                    Logger.d("Updating habit: ${currentState.name}", tag = "HabitEdit")
                    
                    val habitBase = HabitBase(
                        id = currentState.editHabitId!!,
                        name = currentState.name.trim(),
                        description = currentState.description.trim(),
                        color = currentState.color,
                        isActive = currentState.isActive,
                        createdAt = currentState.createdAt ?: Clock.System.todayIn(TimeZone.currentSystemDefault())
                    )
                    
                    val habit: Habit = when (currentState.frequencyType) {
                        FrequencyType.ONCE_DAILY -> DailyHabit(
                            base = habitBase,
                            scheduledTimes = currentState.scheduledTimes
                        )
                        FrequencyType.HOURLY -> HourlyHabit(
                            base = habitBase,
                            intervalMinutes = currentState.intervalMinutes,
                            startTime = currentState.startTime ?: LocalTime(9, 0),
                            endTime = currentState.endTime
                        )
                        FrequencyType.INTERVAL -> IntervalHabit(
                            base = habitBase,
                            intervalMinutes = currentState.intervalMinutes,
                            startTime = currentState.startTime ?: LocalTime(9, 0),
                            endTime = currentState.endTime
                        )
                    }

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
                    
                    val habitBase = HabitBase(
                        name = currentState.name.trim(),
                        description = currentState.description.trim(),
                        color = currentState.color,
                        isActive = currentState.isActive,
                        createdAt = Clock.System.todayIn(TimeZone.currentSystemDefault())
                    )
                    
                    val habit: Habit = when (currentState.frequencyType) {
                        FrequencyType.ONCE_DAILY -> DailyHabit(
                            base = habitBase,
                            scheduledTimes = currentState.scheduledTimes
                        )
                        FrequencyType.HOURLY -> HourlyHabit(
                            base = habitBase,
                            intervalMinutes = currentState.intervalMinutes,
                            startTime = currentState.startTime ?: LocalTime(9, 0),
                            endTime = currentState.endTime
                        )
                        FrequencyType.INTERVAL -> IntervalHabit(
                            base = habitBase,
                            intervalMinutes = currentState.intervalMinutes,
                            startTime = currentState.startTime ?: LocalTime(9, 0),
                            endTime = currentState.endTime
                        )
                    }

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
        val currentState = _uiState.value
        if (currentState is HabitEditUiState.Content) {
            _uiState.value = currentState.copy(
                nameError = null,
                saveError = null
            )
        }
    }
}

/**
 * Time unit for interval specification
 */
enum class TimeUnit {
    MINUTES, HOURS
}

/**
 * UI state for the Habit Edit screen using sealed class pattern
 */
sealed interface HabitEditUiState {
    
    /**
     * Loading state when fetching habit for editing
     */
    data object Loading : HabitEditUiState
    
    /**
     * Error state when habit loading fails
     */
    data class Error(val message: String) : HabitEditUiState
    
    /**
     * Content state when habit data is loaded or being edited
     */
    data class Content(
        val editHabitId: Long? = null, // null for adding, habitId for editing
        val name: String = "",
        val description: String = "",
        val color: String = "#2196F3", // Default blue color
        val isActive: Boolean = true,
        val createdAt: LocalDate? = null, // Original creation date for editing
        val frequencyType: FrequencyType = FrequencyType.ONCE_DAILY,
        val intervalMinutes: Int = 1440, // Default 24 hours = 1440 minutes
        val intervalUnit: TimeUnit = TimeUnit.HOURS, // Default to hours for user convenience
        val scheduledTimes: List<LocalTime> = listOf(LocalTime(9, 0)), // For ONCE_DAILY
        val startTime: LocalTime? = LocalTime(9, 0), // For HOURLY/INTERVAL
        val endTime: LocalTime? = null, // For HOURLY/INTERVAL
        val nameError: String? = null,
        val saveError: String? = null,
        val isSaving: Boolean = false
    ) : HabitEditUiState {
        // Helper property to get the interval value in the selected unit
        val intervalValue: Int
            get() = when (intervalUnit) {
                TimeUnit.MINUTES -> intervalMinutes
                TimeUnit.HOURS -> intervalMinutes / 60
            }
    }
}
