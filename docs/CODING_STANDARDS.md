# Coding Standards

## Logging Guidelines

All new code in this project must follow these logging standards:

### 1. Debug Information
Use `Logger.d()` for general debug information to help with development and troubleshooting:

```kotlin
Logger.d("User navigated to screen: $screenName", tag = "Navigation")
Logger.d("Processing ${items.size} items", tag = "DataProcessing")
```

### 2. Exception Logging
**All caught exceptions must be logged using `Logger.e(exception)`** inside any catch block:

```kotlin
try {
    // Some operation that might fail
    performOperation()
} catch (e: Exception) {
    Logger.e(e, "Failed to perform operation", tag = "OperationName")
    // Handle the exception appropriately
}
```

### 3. Logger Import
Always import the project's Logger utility:
```kotlin
import com.nagopy.kmp.habittracker.util.Logger
```

### 4. Tagging
Use meaningful tags to categorize log messages by feature or component:
- Use PascalCase for consistency
- Examples: "HabitEdit", "Navigation", "Database", "NetworkSync"

### 5. Log Levels
- **Debug (`Logger.d()`)**: Development information, user actions, state changes
- **Info (`Logger.i()`)**: Important application events
- **Warning (`Logger.w()`)**: Potential issues that don't break functionality
- **Error (`Logger.e()`)**: Exceptions and errors that need attention

### 6. Production Considerations
- Debug logs are automatically disabled in release builds
- No sensitive information should be logged
- Exception messages should be user-friendly when appropriate

## Compose Preview Guidelines

All Composable functions should include appropriate `@Preview` functions to support UI development and design review:

### 1. Preview Requirements
**All major UI components must include preview functions:**
- Screen-level Composables should have previews for different states (loading, error, empty, content)
- Individual components should have previews showcasing different variations
- Use meaningful sample data that represents real usage

### 2. Preview Structure
```kotlin
@Preview
@Composable
private fun ComponentNamePreview() {
    MaterialTheme {
        Surface {  // Use Surface for individual components
            ComponentName(
                // Provide meaningful sample data
            )
        }
    }
}
```

### 3. Preview Naming
- Use descriptive names ending with "Preview"
- For variations, include the state: `ComponentLoadingPreview`, `ComponentErrorPreview`
- Keep preview functions private

### 4. Sample Data
- Create realistic sample data that showcases the component properly
- Use current date/time for date-dependent components
- Include edge cases (long text, empty states, etc.)

### 5. Stateless Components for Previews
- Extract stateless versions of screen components when ViewModels are involved
- Create preview-specific data classes to avoid ViewModel dependencies
- Use preview helper functions for complex state setup

### Examples

#### Good Preview Practice
```kotlin
@Preview
@Composable
private fun HabitItemPreview() {
    val sampleHabit = Habit(
        id = 1L,
        name = "Drink Water",
        description = "Stay hydrated throughout the day",
        color = "#2196F3",
        isActive = true,
        createdAt = Clock.System.todayIn(TimeZone.currentSystemDefault())
    )
    
    MaterialTheme {
        Surface {
            HabitItem(
                habit = sampleHabit,
                onEdit = {},
                onDelete = {}
            )
        }
    }
}

@Preview
@Composable
private fun HabitListEmptyStatePreview() {
    MaterialTheme {
        HabitListContentPreview(
            uiState = PreviewHabitListUiState(
                habits = emptyList(),
                isLoading = false,
                error = null
            )
        )
    }
}
```

#### Avoid
```kotlin
// Missing preview functions for important components
@Composable
fun ImportantComponent() { /* ... */ }

// No sample data or unrealistic data
@Preview
@Composable
private fun ComponentPreview() {
    MaterialTheme {
        Component(item = null) // Poor sample data
    }
}
```

## Logging Guidelines (continued)

### Examples

#### Good Logging Practice
```kotlin
// Debug information with meaningful tag
Logger.d("Creating new habit: ${habit.name}", tag = "HabitCreation")

// Exception logging with context
try {
    habitRepository.saveHabit(habit)
} catch (e: DatabaseException) {
    Logger.e(e, "Failed to save habit to database", tag = "Database")
    throw SaveHabitException("Unable to save habit", e)
}
```

#### Avoid
```kotlin
// No logging in catch blocks (violates standard)
try {
    riskyOperation()
} catch (e: Exception) {
    // Missing Logger.e(e) call
}

// Generic or missing tags
Logger.d("Something happened") // No tag
Logger.d("Debug info", tag = "Debug") // Too generic
```