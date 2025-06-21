_# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 🔨 Critical Rule - Interactive Rule Addition Process

When receiving instructions from users that appear to require continuous application:

1. **Always ask**: "Should this be made a standard rule?"
2. **If YES is received**: Add it as an additional rule to CLAUDE.md
3. **Apply it as standard rule thereafter**

This process enables continuous improvement of project rules through active interaction.

## 📚 Interactive Documentation Update System

This project adopts a systematic approach to capture and document development knowledge interactively.

### Reference Documents

Always check these documents at the start of work:

- `/docs/ARCHITECTURE.md` - Three-layer architecture design patterns
- `/docs/CODING_STANDARDS.md` - Logging and exception handling requirements  
- `/docs/SPECIFICATIONS.md` - Screen specifications and UI requirements
- `/docs/FIREBASE_APP_DISTRIBUTION.md` - Firebase App Distribution setup guide
- `/docs/IOS_SWIPE_BACK.md` - iOS swipe-back gesture implementation
- `README.md` - Project overview and setup instructions

### Update Rules

#### When to Propose Updates
Propose documentation updates in these situations:

1. **After resolving errors or problems**
2. **When discovering efficient implementation patterns**
3. **When establishing new API/library usage methods**
4. **When finding outdated/incorrect documentation**
5. **When discovering frequently referenced information**
6. **After completing code review fixes**
7. **When adding new features or components**

#### Proposal Format
```
💡 Documentation update proposal: [situation description]
【Update Content】 [specific additions/modifications]
【Update Candidates】
1. /docs/ARCHITECTURE.md - [reason]
2. /docs/CODING_STANDARDS.md - [reason]  
3. Create new file - [reason]

Which should be updated? (select number or "skip")
```

#### Approval Process
1. User selects update destination
2. Preview actual update content
3. User gives final approval (yes/edit/no)
4. Update file after approval

### Existing Documentation Integration

- Follow existing documentation format and style
- Reference related existing content when applicable
- Include update date (YYYY-MM-DD format) in history

### Important Constraints

1. **Never update files without user approval**
2. **Only add content, never delete or modify existing content**
3. **Never record sensitive information (API keys, passwords, etc.)**
4. **Follow project conventions and style guides**

### Documentation Split Management

To prevent CLAUDE.md from becoming too large:

- **When exceeding 100 lines**: Propose splitting related content to separate files
- **Recommended split structure**:
  - `/docs/UPDATE_SYSTEM.md` - Documentation update system rules
  - `/docs/PROJECT_SPECIFIC.md` - Project-specific settings
  - `/docs/REFERENCE_LIST.md` - List of reference documents
- **Keep only overview and links in CLAUDE.md**: Details go to individual files

## Essential Development Commands

### Build and Run
```bash
# Clean and build everything
./gradlew clean build

# Build and install debug APK
./gradlew assembleDebug
./gradlew installDebug

# iOS development
open iosApp/iosApp.xcodeproj
```

### Testing
```bash
# Run unit tests (same as CI)
./gradlew testDebugUnitTest

# Run all tests with coverage
./gradlew test

# Run tests for specific platforms
./gradlew iosSimulatorArm64Test
./gradlew connectedAndroidTest
```

### Code Quality
```bash
# Run lint analysis
./gradlew lint

# Fix lint issues automatically
./gradlew lintFix
```

## Architecture Overview

This is a **Kotlin Multiplatform** project using **Compose Multiplatform** with a **clean architecture** pattern:

### Three-Layer Architecture
- **Presentation Layer** (`presentation/`): MVVM pattern with Compose UI, ViewModels, and reactive state management
- **Domain Layer** (`domain/`): Pure business logic with Use Cases, Entities (Habit, Task, HabitLog), and Repository interfaces
- **Data Layer** (`data/`): Repository implementations, Room database, and data mappers

### Key Architectural Patterns

**Habit vs Task Distinction:**
- **Habit**: Template/rule defining what should be done (e.g., "Drink water every hour")
- **Task**: Specific instance scheduled for a particular time (e.g., "Drink water at 10:00 AM on 2024-01-20")

**Dependency Injection (Koin):**
- Each layer has its own module: `PresentationModule`, `DomainModule`, `DataModule`
- Platform-specific modules use `expect/actual` pattern for Android/iOS implementations

**Repository Pattern:**
- `HabitRepository` interface in domain layer
- `HabitRepositoryImpl` in data layer using Room database
- Reactive data streams with Kotlin Flow

### Notification System
- **Sequential Scheduling**: One notification scheduled per habit, chain-scheduled after each trigger
- **Platform Implementations**: 
  - Android: `AlarmManager` + `BroadcastReceiver`
  - iOS: `UserNotifications` framework
- **Dynamic Content**: Notifications fetch current habit data at scheduling time

## Critical Coding Standards

### Logging Requirements
**All catch blocks must include exception logging:**
```kotlin
try {
    // operation
} catch (e: Exception) {
    Logger.e(e, "Failed to perform operation", tag = "ComponentName")
    // handle exception
}
```

**Import the project Logger:**
```kotlin
import com.nagopy.kmp.habittracker.util.Logger
```

**Use meaningful tags:**
```kotlin
Logger.d("Creating habit: ${habit.name}", tag = "HabitCreation")
```

### Documentation First Approach
**Always check `/docs` directory before starting work** - It contains comprehensive documentation about architecture, standards, and specifications. Update these documents when making significant changes.

### Communication Guidelines
- **Japanese** for PR titles, descriptions, and review comments
- **English** for code, code comments, and technical documentation

## Key Technology Stack
- **UI**: Compose Multiplatform with Material Design 3
- **Database**: Room with KSP code generation
- **DI**: Koin dependency injection
- **Testing**: JUnit, MockK, kotlin-test, kotlinx-coroutines-test
- **Logging**: Napier (project Logger wrapper)
- **Date/Time**: kotlinx.datetime

## Platform Targets
- **Android**: Min SDK 24, Target SDK 34, JVM 11
- **iOS**: iosX64, iosArm64, iosSimulatorArm64 with static framework_
