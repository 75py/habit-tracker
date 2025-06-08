package com.nagopy.kmp.habittracker.presentation.habitedit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nagopy.kmp.habittracker.domain.model.Habit
import com.nagopy.kmp.habittracker.domain.model.FrequencyType
import com.nagopy.kmp.habittracker.domain.usecase.AddHabitUseCase
import com.nagopy.kmp.habittracker.domain.usecase.ManageNotificationsUseCase
import com.nagopy.kmp.habittracker.util.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/**
 * ViewModel for the Habit Edit screen.
 * Manages the state of habit form and handles user interactions for adding/editing habits.
 */
class HabitEditViewModel(
    private val addHabitUseCase: AddHabitUseCase,
    private val manageNotificationsUseCase: ManageNotificationsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HabitEditUiState())
    val uiState: StateFlow<HabitEditUiState> = _uiState.asStateFlow()

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
        _uiState.value = _uiState.value.copy(frequencyType = frequencyType)
    }

    fun updateIntervalHours(intervalHours: Int) {
        _uiState.value = _uiState.value.copy(intervalHours = intervalHours)
    }

    fun updateScheduledTimes(scheduledTimes: List<LocalTime>) {
        _uiState.value = _uiState.value.copy(scheduledTimes = scheduledTimes)
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
                Logger.d("Creating new habit: ${currentState.name}", tag = "HabitEdit")
                
                val habit = Habit(
                    name = currentState.name.trim(),
                    description = currentState.description.trim(),
                    color = currentState.color,
                    isActive = currentState.isActive,
                    createdAt = Clock.System.todayIn(TimeZone.currentSystemDefault()),
                    frequencyType = currentState.frequencyType,
                    intervalHours = currentState.intervalHours,
                    scheduledTimes = currentState.scheduledTimes
                )

                val habitId = addHabitUseCase(habit)
                
                // Schedule notifications for the new habit
                try {
                    manageNotificationsUseCase.scheduleNotificationsForTodayTasks()
                } catch (notificationException: Exception) {
                    // Notifications are not critical, so we don't fail the save operation
                    Logger.e(notificationException, "Failed to schedule notifications for new habit", tag = "HabitEdit")
                }
                
                _uiState.value = currentState.copy(isSaving = false)
                onSuccess(habitId)
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
 * UI state for the Habit Edit screen
 */
data class HabitEditUiState(
    val name: String = "",
    val description: String = "",
    val color: String = "#2196F3", // Default blue color
    val isActive: Boolean = true,
    val frequencyType: FrequencyType = FrequencyType.ONCE_DAILY,
    val intervalHours: Int = 24,
    val scheduledTimes: List<LocalTime> = listOf(LocalTime(9, 0)),
    val nameError: String? = null,
    val saveError: String? = null,
    val isSaving: Boolean = false
)