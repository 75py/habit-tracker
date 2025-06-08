package com.nagopy.kmp.habittracker.presentation.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nagopy.kmp.habittracker.domain.model.Task
import com.nagopy.kmp.habittracker.domain.usecase.GetTodayTasksUseCase
import com.nagopy.kmp.habittracker.domain.usecase.CompleteTaskUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * ViewModel for the Today screen.
 * Manages the state of today's tasks and handles task completion.
 */
class TodayViewModel(
    private val getTodayTasksUseCase: GetTodayTasksUseCase,
    private val completeTaskUseCase: CompleteTaskUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TodayUiState())
    val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()

    init {
        loadTasks()
    }

    private fun loadTasks() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            getTodayTasksUseCase()
                .catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Unknown error occurred"
                    )
                }
                .collect { tasks ->
                    _uiState.value = _uiState.value.copy(
                        tasks = tasks,
                        isLoading = false,
                        error = null
                    )
                }
        }
    }

    fun completeTask(task: Task) {
        viewModelScope.launch {
            try {
                completeTaskUseCase(task.habitId, task.date, task.scheduledTime)
                // Refresh tasks to show updated completion status
                loadTasks()
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to complete task: ${exception.message}"
                )
            }
        }
    }

    fun refresh() {
        loadTasks()
    }
}

/**
 * UI state for the Today screen
 */
data class TodayUiState(
    val tasks: List<Task> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)