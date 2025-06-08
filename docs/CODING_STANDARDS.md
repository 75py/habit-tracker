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