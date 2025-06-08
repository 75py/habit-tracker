package com.nagopy.kmp.habittracker.presentation.habitedit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nagopy.kmp.habittracker.domain.model.Habit
import com.nagopy.kmp.habittracker.domain.usecase.AddHabitUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/**
 * ViewModel for the Habit Edit screen.
 * Manages the state of habit form and handles user interactions for adding/editing habits.
 */
class HabitEditViewModel(
    private val addHabitUseCase: AddHabitUseCase
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
                val habit = Habit(
                    name = currentState.name.trim(),
                    description = currentState.description.trim(),
                    color = currentState.color,
                    isActive = currentState.isActive,
                    createdAt = Clock.System.todayIn(TimeZone.currentSystemDefault())
                )

                val habitId = addHabitUseCase(habit)
                _uiState.value = currentState.copy(isSaving = false)
                onSuccess(habitId)
            } catch (exception: Exception) {
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
    val nameError: String? = null,
    val saveError: String? = null,
    val isSaving: Boolean = false
)