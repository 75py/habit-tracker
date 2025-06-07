# Architecture

This document describes the layered architecture adopted for the Habit Tracker application.

## Overview

The Habit Tracker follows a **three-layer architecture** pattern that ensures clear separation of concerns, maintainability, and testability. The architecture is designed to support both Android and iOS platforms through Kotlin Multiplatform.

```
┌─────────────────────────────────────┐
│          Presentation Layer         │
│     (UI, ViewModels, Navigation)    │
└─────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────┐
│            Domain Layer             │
│   (Business Logic, Use Cases,       │
│    Entities, Repository Interfaces) │
└─────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────┐
│             Data Layer              │
│  (Repositories, Data Sources,       │
│   Database, Network, Mappers)       │
└─────────────────────────────────────┘
```

## Layer Responsibilities

### Presentation Layer
**Package**: `com.nagopy.kmp.habittracker.presentation`

The presentation layer is responsible for handling user interface and user interactions. It contains:

- **UI Components**: Compose UI components and screens
- **ViewModels**: Hold UI state and handle user actions
- **Navigation**: Routing between different screens
- **State Management**: Managing UI state using Compose state APIs

**Key Principles:**
- ViewModels communicate with the domain layer through use cases
- UI components are stateless and reactive
- No direct access to data sources or business logic

### Domain Layer
**Package**: `com.nagopy.kmp.habittracker.domain`

The domain layer contains the core business logic and domain entities. It's completely independent of external frameworks and platforms. It includes:

- **Entities**: Core business models (Habit, HabitEntry, User, etc.)
- **Use Cases**: Encapsulate specific business operations (CreateHabitUseCase, MarkHabitCompleteUseCase, etc.)
- **Repository Interfaces**: Define contracts for data access without implementation details
- **Domain Services**: Complex business logic that doesn't belong to a specific entity

**Key Principles:**
- Pure Kotlin with no platform-specific dependencies
- Contains the application's business rules
- Defines interfaces that outer layers must implement
- Independent and testable

### Data Layer
**Package**: `com.nagopy.kmp.habittracker.data`

The data layer is responsible for data access and storage. It implements the repository interfaces defined in the domain layer. It contains:

- **Repository Implementations**: Concrete implementations of domain repository interfaces
- **Data Sources**: Local (Room database) and remote (API) data sources
- **Database Entities**: Room database entities and DAOs
- **Data Mappers**: Convert between data models and domain entities
- **Network Models**: DTOs for API communication

**Key Principles:**
- Implements domain repository interfaces
- Handles data persistence and caching
- Manages different data sources (local vs remote)
- Maps between data models and domain entities

## Dependency Flow

The dependency flow follows the **Dependency Inversion Principle**:

1. **Presentation** depends on **Domain** (through use cases and repository interfaces)
2. **Domain** has no dependencies on outer layers
3. **Data** depends on **Domain** (implements repository interfaces)

This ensures that:
- Business logic is isolated and testable
- UI changes don't affect business logic
- Data source changes don't affect business logic
- Platform-specific code is kept separate

## Dependency Injection

The application uses **Koin** for dependency injection, which provides:

- Lightweight and easy-to-use DI framework
- Support for Kotlin Multiplatform
- Clear separation of concerns
- Easy testing with mock dependencies

Each layer has its own DI module:
- `PresentationModule`: ViewModels and UI-related dependencies
- `DomainModule`: Use cases and business logic dependencies
- `DataModule`: Repositories, data sources, and database dependencies

## Benefits of This Architecture

1. **Separation of Concerns**: Each layer has a single responsibility
2. **Testability**: Each layer can be tested independently
3. **Maintainability**: Changes in one layer don't affect others
4. **Scalability**: Easy to add new features without breaking existing code
5. **Platform Independence**: Business logic works on both Android and iOS
6. **Clean Dependencies**: Clear dependency flow prevents circular dependencies