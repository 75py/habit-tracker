# GitHub Copilot Instructions

## Essential First Step

Before answering any questions about this project or generating code, **always read the documentation in the `/docs` directory first**. This directory contains comprehensive and up-to-date information about:

- **Project Architecture** (`/docs/ARCHITECTURE.md`): Complete system design, layer responsibilities, dependency injection patterns, and technical implementation details
- **Coding Standards** (`/docs/CODING_STANDARDS.md`): Logging guidelines, exception handling requirements, and code quality standards
- **Feature Specifications** (`/docs/SPECIFICATIONS.md`): Detailed screen specifications, UI components, user interactions, and business logic

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

## Project Context

This is a cross-platform habit tracking application built with Kotlin Multiplatform and Compose Multiplatform using a **clean architecture** pattern:

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

### Technology Stack
- **UI**: Compose Multiplatform with Material Design 3
- **Database**: Room with KSP code generation
- **DI**: Koin dependency injection
- **Testing**: JUnit, MockK, kotlin-test, kotlinx-coroutines-test
- **Logging**: Napier (project Logger wrapper)
- **Date/Time**: kotlinx.datetime

### Platform Targets
- **Android**: Min SDK 24, Target SDK 34, JVM 11
- **iOS**: iosX64, iosArm64, iosSimulatorArm64 with static framework

All project-specific information including detailed technical specifications is maintained in the `/docs` directory to ensure accuracy and consistency.

## 🔨 最重要ルール - ルールの継続的改善

常に対応が必要と思われる指示を受けたり、そう考えられる差分を追加した場合は、copilot-instructions.mdにルールを追加すること。

プロジェクトのルールを継続的に改善し、開発ガイドラインを持続的に向上させるため：

1. 一度限りではなく継続的に適用すべき指示や要件を特定
2. copilot-instructions.mdの適切なセクションに追加ルールとして記載
3. 以降は標準ルールとして常に適用

このプロセスにより、プロジェクトの品質と一貫性を継続的に向上させていきます。

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

## Guidelines

1. **Always reference `/docs` first** before providing any code suggestions or architectural advice
2. **Follow the established patterns** documented in the architecture and coding standards
3. **Maintain consistency** with existing code structure and naming conventions as documented in the coding standards
4. **Refer to specifications** when implementing UI components or business logic
5. **Update documentation proactively** when making code changes - always consider and update relevant documentation in `/docs` to keep it synchronized with code modifications, even without explicit instructions

## Communication Guidelines

When creating PR titles, descriptions, and comments for reviewers:

- **Use Japanese** for all PR-related content (titles, descriptions, review comments) to ensure clear communication with project maintainers
- **Use English** for code, comments within code, and technical documentation
- **Follow established terminology** and conventions used in existing Japanese documentation

The `/docs` directory serves as the single source of truth for all project information.