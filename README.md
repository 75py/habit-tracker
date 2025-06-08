# Habit Tracker

[![CI](https://github.com/75py/habit-tracker/actions/workflows/ci.yml/badge.svg)](https://github.com/75py/habit-tracker/actions/workflows/ci.yml)

A cross-platform habit tracking application built with Kotlin Multiplatform and Compose Multiplatform.

## Project Overview

Habit Tracker is a modern mobile application that helps users build and maintain positive habits. The app allows users to create custom habits, track their progress, and visualize their streaks across different time periods.

## Key Technologies Used

- **Kotlin Multiplatform (KMP)**: Enables sharing business logic across Android and iOS platforms
- **Compose Multiplatform**: Provides a unified UI framework for creating native user interfaces
- **Room Database**: Handles local data persistence with a type-safe database layer
- **Koin**: Lightweight dependency injection framework for managing application dependencies
- **kotlinx.datetime**: Cross-platform date and time handling
- **Napier**: Cross-platform logging library for consistent debugging and error tracking

## Project Structure

This is a Kotlin Multiplatform project targeting Android and iOS.

* `/composeApp` contains the shared code for your Compose Multiplatform applications.
  It contains several subfolders:
  - `commonMain` is for code that's common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple's CoreCrypto for the iOS part of your Kotlin app,
    `iosMain` would be the right folder for such calls.

* `/iosApp` contains iOS applications. Even if you're sharing your UI with Compose Multiplatform, 
  you need this entry point for your iOS app. This is also where you should add SwiftUI code for your project.

## Architecture

The application follows a layered architecture pattern with clear separation of concerns. For more detailed information about the architecture, see [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

## Getting Started

### Prerequisites

- Android Studio Giraffe or later
- Xcode 15+ (for iOS development)
- Kotlin 1.9+
- JDK 17 or later

### Building the Project

1. **Clone the repository**
   ```bash
   git clone https://github.com/75py/habit-tracker.git
   cd habit-tracker
   ```

2. **Android Development**
   - Open the project in Android Studio
   - Build and run the Android app directly from the IDE
   - Or use the command line:
     ```bash
     ./gradlew assembleDebug
     ./gradlew installDebug
     ```

3. **iOS Development**
   - Open the `iosApp` folder in Xcode
   - Run the project from Xcode
   - Ensure you have iOS 14.0+ deployment target

### Running Tests

- **All Android Tests**: `./gradlew testDebugUnitTest`
- **Common Tests**: `./gradlew cleanTest test`
- **Database Tests**: Tests are automatically run with the above commands

### Screenshots

![App Screenshots](docs/images/screenshots-placeholder.png)
*Screenshots showing the main features of the habit tracker app will be added here*

**Key Features Shown:**
- Today's Tasks Screen with time-based task list
- Habit Creation with custom colors and scheduling
- Task completion with real-time updates

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html).