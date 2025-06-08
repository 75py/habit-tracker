package com.nagopy.kmp.habittracker.presentation.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nagopy.kmp.habittracker.domain.model.Task
import com.nagopy.kmp.habittracker.domain.usecase.GetTodayTasksUseCase
import com.nagopy.kmp.habittracker.domain.usecase.CompleteTaskUseCase
import com.nagopy.kmp.habittracker.domain.usecase.ManageNotificationsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * ViewModel for the Today screen.
 * Manages the state of today's tasks and handles task completion and notifications.
 */
class TodayViewModel(
    private val getTodayTasksUseCase: GetTodayTasksUseCase,
    private val completeTaskUseCase: CompleteTaskUseCase,
    private val manageNotificationsUseCase: ManageNotificationsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TodayUiState())
    val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()

    init {
        loadTasks()
        scheduleNotifications()
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
                // Cancel the notification for the completed task
                manageNotificationsUseCase.cancelTaskNotification(task)
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
        scheduleNotifications()
    }

    private fun scheduleNotifications() {
        viewModelScope.launch {
            try {
                // Ensure notifications are enabled first
                val notificationsEnabled = manageNotificationsUseCase.ensureNotificationsEnabled()
                if (notificationsEnabled) {
                    // Schedule notifications for today's tasks
                    manageNotificationsUseCase.scheduleNotificationsForTodayTasks()
                }
            } catch (exception: Exception) {
                // Notifications are not critical, so we don't show an error to the user
                // In a production app, you might want to log this
            }
        }
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