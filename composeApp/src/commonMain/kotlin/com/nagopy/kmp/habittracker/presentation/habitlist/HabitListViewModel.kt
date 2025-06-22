package com.nagopy.kmp.habittracker.presentation.habitlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nagopy.kmp.habittracker.domain.model.Habit
import com.nagopy.kmp.habittracker.domain.usecase.GetAllHabitsUseCase
import com.nagopy.kmp.habittracker.domain.usecase.DeleteHabitUseCase
import com.nagopy.kmp.habittracker.util.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * ViewModel for the Habit List screen.
 * Manages the state of the habits list and handles user interactions.
 */
class HabitListViewModel(
    private val getAllHabitsUseCase: GetAllHabitsUseCase,
    private val deleteHabitUseCase: DeleteHabitUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<HabitListUiState>(HabitListUiState.Loading)
    val uiState: StateFlow<HabitListUiState> = _uiState.asStateFlow()

    init {
        loadHabits()
    }

    private fun loadHabits() {
        viewModelScope.launch {
            _uiState.value = HabitListUiState.Loading
            
            getAllHabitsUseCase()
                .catch { exception ->
                    _uiState.value = HabitListUiState.Error(
                        message = exception.message ?: "Unknown error occurred"
                    )
                }
                .collect { habits ->
                    if (habits.isEmpty()) {
                        _uiState.value = HabitListUiState.Empty
                    } else {
                        _uiState.value = HabitListUiState.Content(
                            habits = habits
                        )
                    }
                }
        }
    }

    fun refresh() {
        loadHabits()
    }

    fun deleteHabit(habitId: Long) {
        viewModelScope.launch {
            try {
                Logger.d("Deleting habit with ID: $habitId", tag = "HabitList")
                deleteHabitUseCase(habitId)
                Logger.d("Successfully deleted habit with ID: $habitId", tag = "HabitList")
            } catch (exception: Exception) {
                Logger.e(exception, "Failed to delete habit with ID: $habitId", tag = "HabitList")
                _uiState.value = HabitListUiState.Error(
                    message = exception.message ?: "Failed to delete habit"
                )
            }
        }
    }
}

/**
 * UI state for the Habit List screen using sealed class pattern
 */
sealed interface HabitListUiState {
    
    /**
     * Loading state when fetching habits
     */
    data object Loading : HabitListUiState
    
    /**
     * Error state when habit loading fails
     */
    data class Error(val message: String) : HabitListUiState
    
    /**
     * Empty state when no habits exist
     */
    data object Empty : HabitListUiState
    
    /**
     * Content state when habits are successfully loaded
     */
    data class Content(
        val habits: List<Habit>
    ) : HabitListUiState
}