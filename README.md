# Habit Tracker

[![CI](https://github.com/75py/habit-tracker/actions/workflows/ci.yml/badge.svg)](https://github.com/75py/habit-tracker/actions/workflows/ci.yml)

A cross-platform habit tracking application built with Kotlin Multiplatform and Compose Multiplatform.

## Project Overview

Habit Tracker is a modern mobile application that helps users build and maintain positive habits. The app allows users to create custom habits, track their progress, and visualize their streaks across different time periods.

## Key Technologies Used

- **Kotlin Multiplatform (KMP)** 2.1.0: Enables sharing business logic across Android and iOS platforms
- **Compose Multiplatform** 1.8.1: Provides a unified UI framework for creating native user interfaces
- **Room Database** 2.7.1: Handles local data persistence with Kotlin Multiplatform support
- **Koin** 4.0.4: Lightweight dependency injection framework for managing application dependencies
- **kotlinx.datetime** 0.6.1: Cross-platform date and time handling
- **Napier** 2.7.1: Cross-platform logging library for consistent debugging and error tracking
- **AndroidX Navigation Compose**: Navigation framework for Compose UI with cross-platform support

## Project Structure

This is a Kotlin Multiplatform project targeting Android and iOS, organized with a clean three-layer architecture.

### Main Directories

* **`/composeApp`** - Contains the shared Kotlin Multiplatform code with Compose Multiplatform UI:
  - `commonMain` - Shared code for all platforms (business logic, UI, data models)
  - `androidMain` - Android-specific implementations (notifications, platform services)
  - `iosMain` - iOS-specific implementations (notifications, platform services)
  - `commonTest` - Shared unit tests
  - `androidUnitTest` - Android-specific unit tests

* **`/iosApp`** - iOS application entry point and iOS-specific code (SwiftUI wrappers, configuration)

* **`/docs`** - Comprehensive project documentation:
  - [`ARCHITECTURE.md`](docs/ARCHITECTURE.md) - Detailed three-layer architecture design
  - [`DEVELOPMENT_COMMANDS.md`](docs/DEVELOPMENT_COMMANDS.md) - Build, test, and maintenance commands
  - [`FIREBASE_APP_DISTRIBUTION.md`](docs/FIREBASE_APP_DISTRIBUTION.md) - Complete Firebase setup and distribution guide
  - [`CODING_STANDARDS.md`](docs/CODING_STANDARDS.md) - Code style and logging guidelines
  - [`TESTING.md`](docs/TESTING.md) - Testing strategy and best practices

* **`/scripts`** - Automation scripts for Firebase App Distribution and other tasks

* **`/fastlane`** - Fastlane configuration for automated app distribution

## Architecture

The application follows a **three-layer clean architecture** pattern with clear separation of concerns:

- **Presentation Layer**: Compose UI, ViewModels, and navigation with MVVM pattern
- **Domain Layer**: Business logic, use cases, and domain entities (pure Kotlin, platform-independent)
- **Data Layer**: Repository implementations, Room database, and data sources

This architecture ensures maintainability, testability, and platform independence of business logic. For comprehensive details including dependency injection patterns, testing strategies, and layer responsibilities, see [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

## Getting Started

### Prerequisites

- **Android Studio** Giraffe or later (2023.3.1+)
- **Xcode 15+** (for iOS development)
- **Kotlin** 2.1.0+ (managed via Gradle)
- **JDK 17** or later
- **macOS** (required for iOS development and distribution)

### Android Target Configuration
- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 35 (Android 15)
- **Compile SDK**: 35

### iOS Target Configuration  
- **iOS Deployment Target**: 18.2+
- **Supported Architectures**: arm64, x86_64 (simulator), arm64 (simulator)
- **Framework Type**: Static framework for better performance

### Building the Project

1. **Clone the repository**
   ```bash
   git clone https://github.com/75py/habit-tracker.git
   cd habit-tracker
   ```

2. **Android Development**
   ```bash
   # Clean and build all modules
   ./gradlew clean build
   
   # Build and install debug APK
   ./gradlew assembleDebug installDebug
   
   # Open in Android Studio for development
   # File → Open → Select the project root directory
   ```

3. **iOS Development**
   ```bash
   # Open Xcode project
   open iosApp/iosApp.xcodeproj
   
   # Build from command line (requires Xcode)
   xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Debug
   ```
   
   **Note**: iOS deployment target is set to 18.2+. Ensure your iOS simulator or device meets this requirement.

### Running Tests

```bash
# Run all unit tests (recommended for CI/local development)
./gradlew testDebugUnitTest

# Run all tests with detailed output
./gradlew test

# Run tests for specific platforms
./gradlew :composeApp:testDebugUnitTest  # Android unit tests
./gradlew :composeApp:iosSimulatorArm64Test  # iOS simulator tests

# Run with coverage reports
./gradlew test --info
```

**Test Reports**: Generated in `build/reports/tests/` for HTML reports and `build/test-results/` for CI integration.

For comprehensive testing guidelines and strategies, see [`docs/TESTING.md`](docs/TESTING.md).

### Firebase App Distribution

Firebase App Distributionを使用してテストアプリ（Android/iOS）を配信できます。

#### セットアップと使用方法
- **詳細ガイド**: [`docs/FIREBASE_APP_DISTRIBUTION.md`](docs/FIREBASE_APP_DISTRIBUTION.md) - 完全なセットアップ手順、トラブルシューティング、セキュリティガイドライン
- **環境設定**: `.env.firebase.template` をコピーして設定ファイルを作成
- **GitHub Actions**: 手動ワークフローでAndroidテストアプリを配信（プラットフォーム選択可能）
- **ローカル実行**: `./scripts/firebase-distribute.sh [platform]` でAndroid/iOSアプリを配信
- **実装方式**: fastlaneベースでアプリ本体のGradle設定に影響しない外部実装

#### 配信方法の違い
- **Android**: GitHub ActionsとローカルPCの両方で配信可能
- **iOS**: macOS環境でのローカル実行のみ（AdHoc配信、Provisioning Profile必須）

#### 必要な前提条件
- Firebaseプロジェクト作成とApp Distribution有効化
- サービスアカウントキーの生成
- テスターグループの設定
- **iOS配信**: macOS、Xcode、配布証明書、Provisioning Profile

## Development

### Common Development Commands

```bash
# Build and Development
./gradlew clean build              # Clean and build everything
./gradlew assembleDebug           # Build debug APK
./gradlew installDebug            # Install to connected device

# Testing and Quality
./gradlew testDebugUnitTest       # Run unit tests (CI-equivalent)
./gradlew lint                    # Run lint analysis
./gradlew ktlintFormat           # Format code (if ktlint is configured)

# Dependency Management
./gradlew dependencies            # Show dependency tree
./gradlew dependencyUpdates      # Check for dependency updates
```

For comprehensive build, test, and maintenance commands, see [`docs/DEVELOPMENT_COMMANDS.md`](docs/DEVELOPMENT_COMMANDS.md).

### Code Quality and Standards

The project follows strict coding standards including mandatory exception logging and cross-platform consistency. See [`docs/CODING_STANDARDS.md`](docs/CODING_STANDARDS.md) for detailed guidelines.

### Testing Strategy

The application uses comprehensive testing with MockK for mocking, Room testing utilities for database tests, and platform-specific test configurations. See [`docs/TESTING.md`](docs/TESTING.md) for testing best practices and coverage requirements.

### Screenshots

![App Screenshots](docs/images/screenshots-placeholder.png)

*Note: Screenshots showing the main features of the habit tracker app will be added in future updates*

**Planned Features to be Demonstrated:**
- Today's Tasks Screen with time-based task list and completion tracking
- Habit Creation interface with custom colors and flexible scheduling options  
- Task completion workflow with real-time updates and progress visualization
- Cross-platform UI consistency between Android and iOS implementations

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html).