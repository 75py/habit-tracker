# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 🔨 最重要ルール - 新しいルールの追加プロセス

ユーザーから今回限りではなく常に対応が必要だと思われる指示を受けた場合：

1. 「これを標準のルールにしますか？」と質問する
2. YESの回答を得た場合、CLAUDE.mdに追加ルールとして記載する
3. 以降は標準ルールとして常に適用する

このプロセスにより、プロジェクトのルールを継続的に改善していきます。

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

### Documentation Requirements
**Always read `/docs` directory first** - it contains:
- `ARCHITECTURE.md`: Complete system design and patterns
- `CODING_STANDARDS.md`: Logging and exception handling requirements  
- `SPECIFICATIONS.md`: Screen specifications and UI requirements

**Update documentation when making changes** - keep `/docs` synchronized with code modifications.

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
- **iOS**: iosX64, iosArm64, iosSimulatorArm64 with static framework