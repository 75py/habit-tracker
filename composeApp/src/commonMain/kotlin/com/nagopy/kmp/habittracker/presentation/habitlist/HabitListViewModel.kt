package com.nagopy.kmp.habittracker.presentation.habitlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nagopy.kmp.habittracker.domain.model.Habit
import com.nagopy.kmp.habittracker.domain.usecase.GetAllHabitsUseCase
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
    private val getAllHabitsUseCase: GetAllHabitsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HabitListUiState())
    val uiState: StateFlow<HabitListUiState> = _uiState.asStateFlow()

    init {
        loadHabits()
    }

    private fun loadHabits() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            getAllHabitsUseCase()
                .catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Unknown error occurred"
                    )
                }
                .collect { habits ->
                    _uiState.value = _uiState.value.copy(
                        habits = habits,
                        isLoading = false,
                        error = null
                    )
                }
        }
    }

    fun refresh() {
        loadHabits()
    }
}

/**
 * UI state for the Habit List screen
 */
data class HabitListUiState(
    val habits: List<Habit> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)