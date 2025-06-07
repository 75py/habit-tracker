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

### Building the Project

1. Clone the repository
2. Open the project in Android Studio
3. Build and run the Android app directly from the IDE
4. For iOS, open the `iosApp` folder in Xcode and run the project

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…