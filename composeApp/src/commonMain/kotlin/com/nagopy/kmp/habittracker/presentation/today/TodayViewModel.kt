package com.nagopy.kmp.habittracker.presentation.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nagopy.kmp.habittracker.domain.model.Task
import com.nagopy.kmp.habittracker.domain.usecase.GetTodayTasksUseCase
import com.nagopy.kmp.habittracker.domain.usecase.CompleteTaskUseCase
import com.nagopy.kmp.habittracker.domain.usecase.ManageNotificationsUseCase
import com.nagopy.kmp.habittracker.domain.usecase.ScheduleNextNotificationUseCase
import com.nagopy.kmp.habittracker.util.Logger
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
    private val manageNotificationsUseCase: ManageNotificationsUseCase,
    private val scheduleNextNotificationUseCase: ScheduleNextNotificationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<TodayUiState>(TodayUiState.Loading)
    val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()

    init {
        loadTasks()
        scheduleNotifications()
    }

    private fun loadTasks() {
        viewModelScope.launch {
            _uiState.value = TodayUiState.Loading
            
            getTodayTasksUseCase()
                .catch { exception ->
                    _uiState.value = TodayUiState.Error(
                        message = exception.message ?: "Unknown error occurred"
                    )
                }
                .collect { tasksFromDatabase ->
                    if (tasksFromDatabase.isEmpty()) {
                        _uiState.value = TodayUiState.Empty
                    } else {
                        // Merge database state with UI completion state
                        val currentCompletedKeys = when (val currentState = _uiState.value) {
                            is TodayUiState.Content -> currentState.completedTaskKeys
                            else -> emptySet()
                        }
                        
                        val mergedTasks = tasksFromDatabase.map { task ->
                            val taskKey = createTaskKey(task)
                            val isCompletedInUI = currentCompletedKeys.contains(taskKey)
                            task.copy(isCompleted = task.isCompleted || isCompletedInUI)
                        }
                        
                        _uiState.value = TodayUiState.Content(
                            tasks = mergedTasks,
                            completedTaskKeys = currentCompletedKeys
                        )
                    }
                }
        }
    }

    fun completeTask(task: Task) {
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                if (currentState !is TodayUiState.Content) return@launch
                
                // Immediately update UI state to show task as completed
                val taskKey = createTaskKey(task)
                val updatedTasks = currentState.tasks.map { existingTask ->
                    if (createTaskKey(existingTask) == taskKey) {
                        existingTask.copy(isCompleted = true)
                    } else {
                        existingTask
                    }
                }
                
                _uiState.value = TodayUiState.Content(
                    tasks = updatedTasks,
                    completedTaskKeys = currentState.completedTaskKeys + taskKey
                )
                
                // Complete the task in the backend
                completeTaskUseCase(task.habitId, task.date, task.scheduledTime)
                
                // Cancel the notification for the completed task
                manageNotificationsUseCase.cancelTaskNotification(task)
                
            } catch (exception: Exception) {
                Logger.e(exception, "Failed to complete task", tag = "TodayViewModel")
                _uiState.value = TodayUiState.Error(
                    message = "Failed to complete task: ${exception.message}"
                )
            }
        }
    }

    fun refresh() {
        // Clear UI completion state when refreshing to start fresh and reload
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
 * UI state for the Today screen using sealed class pattern
 */
sealed interface TodayUiState {
    
    /**
     * Loading state when fetching tasks
     */
    data object Loading : TodayUiState
    
    /**
     * Error state when task loading fails
     */
    data class Error(val message: String) : TodayUiState
    
    /**
     * Empty state when no tasks are scheduled for today
     */
    data object Empty : TodayUiState
    
    /**
     * Content state when tasks are successfully loaded
     */
    data class Content(
        val tasks: List<Task>,
        val completedTaskKeys: Set<String> = emptySet()
    ) : TodayUiState
}