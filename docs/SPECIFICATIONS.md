# Screen Specifications

This document describes the specifications for the habit tracking screens in the application.

## Today's Tasks Screen

### Purpose
The Today's Tasks Screen displays all scheduled task instances for the current day, organized by time. It transforms habit rules into specific, actionable tasks that users can complete throughout the day. This screen supports habits that repeat multiple times per day by showing each scheduled instance as a separate, completable task.

### Key Concepts
- **Habit**: A rule that defines what should be done and when (e.g., "Drink water every hour")
- **Task**: A specific instance of a habit scheduled for a particular time (e.g., "Drink water at 10:00 AM")

### Displayed Items
- **Task List**: All scheduled task instances for today, sorted by time
- **Task Information**: For each task, the following information is shown:
  - Scheduled time (e.g., "10:00") in a prominent format
  - Habit color indicator (circular icon)
  - Habit name (primary text)
  - Habit description (secondary text, if provided)
  - Individual completion checkbox for each task instance
- **Time Organization**: Tasks are sorted chronologically from earliest to latest
- **Multiple Instances**: The same habit can appear multiple times with different scheduled times
- **Completion Status**: Each task can be marked complete independently
- **Empty State**: When no tasks exist for today, displays appropriate message
- **Loading State**: Progress indicator while tasks are being generated
- **Error State**: Error message with retry option if loading fails

### Task Generation Logic
The screen generates task instances based on habit scheduling:

**Once Daily Habits**:
- Creates one task per configured scheduled time
- Example: "Morning Exercise" at 7:00 AM

**Hourly Habits**:
- Creates tasks every hour starting from the configured start time
- Example: "Drink Water" at 9:00, 10:00, 11:00, ..., 23:00

**Interval Habits**:
- Creates tasks every N hours based on the configured interval
- Example: "Take Break" every 4 hours: 8:00, 12:00, 16:00, 20:00

### User Interactions
- **Complete Task**: Checkbox tap marks the specific task instance as completed
- **Refresh**: Pull to refresh or error retry button reloads the task list
- **Navigation**: Top app bar shows "Today's Tasks" title
- **Real-time Updates**: Task completion updates the UI immediately

### UI Components
- `Scaffold` with top app bar
- `LazyColumn` for efficient task list rendering
- `Card` components for individual task items
- `Checkbox` for task completion
- `CircularProgressIndicator` for loading states
- Material Design 3 components throughout

### Behavior
- **Task Completion**: Marking a task complete updates the underlying habit log
- **Automatic Refresh**: Task list refreshes after completion to reflect current state
- **Time Display**: 24-hour format with prominent styling
- **Independent Completion**: Each task instance can be completed independently of other instances of the same habit
- **Persistence**: Completed tasks remain marked when screen is refreshed or revisited

## Habit List Screen

### Purpose
The Habit List Screen displays all habits that the user has created, providing an overview of their tracking activities. It serves as the main entry point for habit management and allows users to navigate to individual habits or add new ones.

### Displayed Items
- **Habit List**: All user-created habits displayed in a scrollable list
- **Habit Information**: For each habit, the following information is shown:
  - Habit name (primary text)
  - Habit description (secondary text, if provided)
  - Color indicator (circular icon showing the habit's assigned color)
  - Status indicator (shows "Inactive" label for inactive habits)
- **Empty State**: When no habits exist, displays a message encouraging users to add their first habit
- **Loading State**: Progress indicator while habits are being loaded
- **Error State**: Error message with retry option if loading fails

### User Interactions
- **Add Habit**: Floating action button (+) navigates to the Habit Add/Edit Screen
- **View Habit Details**: Tapping on any habit item (future feature for navigation to habit details)
- **Pull to Refresh**: Refreshing the habit list by pulling down (handled by refresh button in error state)
- **Navigation**: Top app bar shows the screen title "My Habits"

### UI Components
- `Scaffold` with top app bar and floating action button
- `LazyColumn` for efficient list rendering
- `Card` components for individual habit items
- `CircularProgressIndicator` for loading states
- Material Design 3 components throughout

## Habit Add/Edit Screen

### Purpose
The Habit Add/Edit Screen allows users to create new habits or modify existing ones. It provides a form interface for entering all necessary habit information with validation and user feedback.

### Form Fields
1. **Habit Name** (Required)
   - Text input field
   - Required validation with error message
   - Maximum length: Unlimited (but encouraged to be concise)
   - Placeholder: "Habit Name *"

2. **Description** (Optional)
   - Multi-line text input field
   - 3-5 lines visible
   - Placeholder: "Description (Optional)"

3. **Color Selection** (Required)
   - Horizontal scrollable list of predefined colors
   - 10 color options available
   - Visual selection with border highlight
   - Default: Blue (#2196F3)

4. **Active Status** (Required)
   - Toggle switch
   - Default: Enabled (true)
   - Explanation text: "Enable tracking for this habit"

### Validation Rules
- **Name Field**: 
  - Must not be empty or contain only whitespace
  - Error message: "Name is required"
- **Description Field**: 
  - No validation (optional field)
- **Color Selection**: 
  - Must select one of the predefined colors
  - Default value provided, so always valid
- **Active Status**: 
  - Boolean field, always valid

### User Interactions
- **Cancel**: Navigation icon in top app bar returns to previous screen without saving
- **Save**: Action button in top app bar saves the habit and returns to previous screen
- **Form Input**: All fields support standard text input interactions
- **Color Selection**: Tap to select from color palette
- **Active Toggle**: Switch component for enabling/disabling habit

### Behavior
- **Input Trimming**: Name and description fields automatically trim leading/trailing whitespace
- **Saving State**: Save button shows loading indicator during save operation
- **Error Handling**: Form validation errors shown inline with fields
- **Success**: Successful save navigates back to previous screen
- **Save Errors**: Server/database errors shown as error cards above form

### UI Components
- `Scaffold` with top app bar and action buttons
- `OutlinedTextField` for text inputs
- `LazyRow` for color selection
- `Switch` for active status
- `Card` for error display
- Material Design 3 components throughout

### Navigation
- **Entry Points**: 
  - Floating action button from Habit List Screen (Add mode)
  - Future: Habit item tap from Habit List Screen (Edit mode)
- **Exit Points**:
  - Cancel button returns without saving
  - Successful save returns to previous screen
  - System back button cancels operation

### Color Palette
The following colors are available for selection:
- Blue: #2196F3 (default)
- Green: #4CAF50
- Orange: #FF9800
- Purple: #9C27B0
- Red: #F44336
- Cyan: #00BCD4
- Yellow: #FFEB3B
- Brown: #795548
- Blue Grey: #607D8B
- Pink: #E91E63

## Technical Notes

### State Management
Both screens use ViewModels following the MVVM pattern:
- **HabitListViewModel**: Manages habit list state, loading, and error states
- **HabitEditViewModel**: Manages form state, validation, and save operations

### Dependency Injection
ViewModels are provided through Koin dependency injection, requiring the following use cases:
- **HabitListViewModel**: `GetAllHabitsUseCase`
- **HabitEditViewModel**: `AddHabitUseCase`

### Platform Support
Both screens are implemented using Compose Multiplatform and support:
- Android
- iOS (when iOS target is enabled)

### Accessibility
- All interactive elements have appropriate content descriptions
- Form fields have clear labels and error messages
- Color selection is supplemented with visual indicators beyond color alone

## Notification System Specifications

### Purpose
The notification system provides timely reminders for scheduled habit tasks, helping users maintain consistent habit execution throughout the day. Notifications are platform-native and support interactive completion actions.

### Notification Content Strategy

**Dynamic Content Updates:**
- Notifications always display current habit information at the time of scheduling
- Content is fetched fresh from the database to ensure accuracy
- Habit name and description changes are immediately reflected in new notifications

**Content Sources (Priority Order):**
1. **Primary**: Current habit data from `HabitRepository.getHabit()`
2. **Fallback**: Task data (for robustness if habit lookup fails)

**Content Structure:**
- **Title**: Current habit name
- **Body**: Current habit description (or default reminder text if empty)
- **Actions**: Platform-specific completion actions

### Scheduling Behavior

**Task-Based Scheduling:**
- Notifications are scheduled for each individual task instance
- Multiple notifications can exist for the same habit at different times
- Scheduling occurs when tasks are generated (typically daily)

**Timing:**
- Notifications trigger at the exact scheduled time for each task
- Platform-specific precision (AlarmManager on Android, UserNotifications on iOS)
- Handles device sleep states and background execution

### Platform-Specific Implementation

**Android:**
- Uses `AlarmManager` for precise timing
- `NotificationManager` for display and management
- `BroadcastReceiver` for handling completion actions
- Respects Android notification channels and permission model

**iOS:**
- Uses `UserNotifications` framework
- Native iOS notification experience and permission handling
- `UNNotificationRequest` for scheduling
- `UNNotificationCategory` for interactive actions

### Interactive Features

**Completion Actions:**
- "Complete" action button in notification
- Tapping action marks the specific task as completed
- Automatic notification dismissal after completion
- Background execution without opening the app

**Permission Management:**
- Platform-appropriate permission requests
- Graceful handling of denied permissions
- Notifications only scheduled when permissions are granted

### Error Handling

**Robust Fallback:**
- Failed habit lookups fall back to task data
- Missing descriptions use default reminder text
- Scheduling failures are logged but don't crash the app

**Data Consistency:**
- Repository errors don't prevent notification scheduling
- Defensive programming ensures notifications are always attempted
- Clean error recovery maintains user experience

### Performance Considerations

**Database Access:**
- Minimal database calls during scheduling
- Suspend function support for proper coroutine handling
- No UI blocking during notification setup

**Resource Management:**
- Automatic cleanup of completed/cancelled notifications
- Platform-appropriate memory and battery usage
- Efficient notification ID generation and management
- Touch targets meet minimum size requirements