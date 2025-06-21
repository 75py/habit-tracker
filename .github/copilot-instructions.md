# GitHub Copilot Instructions

This file provides guidance to GitHub Copilot and other coding agents when working with code in this repository.

## 🔨 Critical Rule - Continuous Rule Improvement

When adding code or making changes that establish patterns requiring continuous application:
1. Identify patterns that should become standard practice
2. Add them as rules to this file
3. Apply them consistently thereafter

This enables continuous improvement of project quality and consistency.

## 📖 Essential Documentation

**Always check these documents before generating code:**

- `/docs/README.md` - Documentation index and navigation guide
- `/docs/ARCHITECTURE.md` - Complete system design, three-layer architecture, and platform specifics
- `/docs/CODING_STANDARDS.md` - Logging requirements, communication guidelines, and coding conventions
- `/docs/SPECIFICATIONS.md` - Screen specifications and UI requirements
- `/docs/DEVELOPMENT_COMMANDS.md` - Build, test, and maintenance commands
- `/docs/TESTING.md` - Testing strategy, standards, and best practices

## 🚀 Quick Reference

### Build and Test
```bash
# Build everything
./gradlew clean build

# Run unit tests
./gradlew testDebugUnitTest

# Run lint checks
./gradlew lint
```

See `/docs/DEVELOPMENT_COMMANDS.md` for complete command reference.

### Key Project Information

- **Architecture**: Kotlin Multiplatform with Compose Multiplatform
- **Pattern**: Clean Architecture (Presentation → Domain → Data)
- **Database**: Room with Kotlin Multiplatform support
- **DI**: Koin for dependency injection
- **Logging**: Must use `Logger.e()` in all catch blocks

## 📝 Documentation Update Requirements

When making significant code changes:
1. Update relevant documentation in `/docs` directory
2. Add update date (YYYY-MM-DD) to the Update History section
3. Ensure documentation remains synchronized with code
4. Document new patterns, APIs, or architectural decisions

Documentation updates should be made proactively without requiring explicit instructions.

## ⚠️ Critical Coding Standards

### Logging Requirements
All catch blocks MUST include exception logging:
```kotlin
try {
    // operation
} catch (e: Exception) {
    Logger.e(e, "Failed to perform operation", tag = "ComponentName")
    // handle exception
}
```

Always import: `import com.nagopy.kmp.habittracker.util.Logger`

### Communication Guidelines
- **Japanese** for PR titles, descriptions, and review comments
- **English** for code, code comments, and technical documentation

## 🏗️ Architecture Overview

### Three-Layer Architecture
- **Presentation Layer** (`presentation/`): MVVM with Compose UI
- **Domain Layer** (`domain/`): Business logic and use cases
- **Data Layer** (`data/`): Repository implementations and Room database

### Key Concepts
- **Habit**: Template defining what should be done
- **Task**: Specific instance at a scheduled time
- **Repository Pattern**: Interface in domain, implementation in data
- **Dependency Injection**: Koin modules for each layer

## ✅ Code Generation Guidelines

1. **Check documentation first** - Always reference `/docs` before generating code
2. **Follow existing patterns** - Maintain consistency with current architecture
3. **Update tests** - Add/update tests for new functionality
4. **Handle exceptions** - Log all exceptions with meaningful context
5. **Use meaningful names** - Follow project naming conventions
6. **Document complex logic** - Add comments for non-obvious implementations

## 🔄 When Adding New Features

1. Review relevant documentation in `/docs`
2. Follow the three-layer architecture pattern
3. Create appropriate use cases in domain layer
4. Implement repository methods if needed
5. Add ViewModels and UI components
6. Write comprehensive tests
7. Update documentation with new feature details

## 🚫 Important Constraints

1. Never bypass the clean architecture layers
2. Always log exceptions in catch blocks
3. Follow the established coding standards
4. Maintain platform compatibility (Android/iOS)
5. Keep documentation synchronized with code

## Update History
- 2025-06-21: Updated based on CLAUDE.md for non-interactive GitHub Copilot usage