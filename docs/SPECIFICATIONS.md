# Screen Specifications

This document describes the specifications for the habit tracking screens in the application.

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
- Touch targets meet minimum size requirements