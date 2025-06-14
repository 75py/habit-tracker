package com.nagopy.kmp.habittracker.presentation.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nagopy.kmp.habittracker.domain.model.Task
import com.nagopy.kmp.habittracker.domain.usecase.GetTodayTasksUseCase
import com.nagopy.kmp.habittracker.domain.usecase.CompleteTaskUseCase
import com.nagopy.kmp.habittracker.domain.usecase.ManageNotificationsUseCase
import com.nagopy.kmp.habittracker.domain.usecase.ScheduleNextNotificationUseCase
import habittracker.composeapp.generated.resources.Res
import habittracker.composeapp.generated.resources.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString

/**
 * ViewModel for the Today screen.
 * Manages the state of today's tasks and handles task completion and notifications.
 */
class TodayViewModel(
    private val getTodayTasksUseCase: GetTodayTasksUseCase,
    private val completeTaskUseCase: CompleteTaskUseCase,
    private val manageNotificationsUseCase: ManageNotificationsUseCase,
    private val scheduleNextNotificationUseCase: ScheduleNextNotificationUseCase
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
                .collect { tasksFromDatabase ->
                    // Merge database state with UI completion state
                    val currentCompletedKeys = _uiState.value.completedTaskKeys
                    val mergedTasks = tasksFromDatabase.map { task ->
                        val taskKey = createTaskKey(task)
                        val isCompletedInUI = currentCompletedKeys.contains(taskKey)
                        task.copy(isCompleted = task.isCompleted || isCompletedInUI)
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        tasks = mergedTasks,
                        isLoading = false,
                        error = null
                    )
                }
        }
    }

    fun completeTask(task: Task) {
        viewModelScope.launch {
            try {
                // Immediately update UI state to show task as completed
                val taskKey = createTaskKey(task)
                val updatedTasks = _uiState.value.tasks.map { existingTask ->
                    if (createTaskKey(existingTask) == taskKey) {
                        existingTask.copy(isCompleted = true)
                    } else {
                        existingTask
                    }
                }
                
                _uiState.value = _uiState.value.copy(
                    tasks = updatedTasks,
                    completedTaskKeys = _uiState.value.completedTaskKeys + taskKey
                )
                
                // Complete the task in the backend
                completeTaskUseCase(task.habitId, task.date, task.scheduledTime)
                
                // Cancel the notification for the completed task
                manageNotificationsUseCase.cancelTaskNotification(task)
                
            } catch (exception: Exception) {
                // Revert the UI state change if backend call failed
                val taskKey = createTaskKey(task)
                val revertedTasks = _uiState.value.tasks.map { existingTask ->
                    if (createTaskKey(existingTask) == taskKey) {
                        existingTask.copy(isCompleted = false)
                    } else {
                        existingTask
                    }
                }
                
                _uiState.value = _uiState.value.copy(
                    tasks = revertedTasks,
                    completedTaskKeys = _uiState.value.completedTaskKeys - taskKey,
                    error = getString(Res.string.failed_to_complete_task, exception.message ?: "Unknown error")
                )
            }
        }
    }

    fun refresh() {
        // Clear UI completion state when refreshing to start fresh
        _uiState.value = _uiState.value.copy(completedTaskKeys = emptySet())
        loadTasks()
        scheduleNotifications()
    }

    private fun scheduleNotifications() {
        viewModelScope.launch {
            try {
                // Ensure notifications are enabled first
                val notificationsEnabled = manageNotificationsUseCase.ensureNotificationsEnabled()
                if (notificationsEnabled) {
                    // Use sequential scheduling instead of batch scheduling
                    scheduleNextNotificationUseCase.rescheduleAllHabitNotifications()
                }
            } catch (exception: Exception) {
                // Notifications are not critical, so we don't show an error to the user
                // In a production app, you might want to log this
            }
        }
    }
    
    /**
     * Creates a unique key for a task instance to track individual completion status
     */
    private fun createTaskKey(task: Task): String {
        return "${task.habitId}_${task.date}_${task.scheduledTime}"
    }
}

/**
 * UI state for the Today screen
 */
data class TodayUiState(
    val tasks: List<Task> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val completedTaskKeys: Set<String> = emptySet()
)