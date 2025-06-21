# Development Commands

This document contains essential commands for building, testing, and maintaining the Habit Tracker project.

## Build and Run

### Android
```bash
# Clean and build everything
./gradlew clean build

# Build debug APK
./gradlew assembleDebug

# Install debug APK to connected device
./gradlew installDebug

# Build and install in one command
./gradlew assembleDebug installDebug
```

### iOS
```bash
# Open Xcode project
open iosApp/iosApp.xcodeproj

# Build from command line (requires Xcode)
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Debug
```

## Testing

### Unit Tests
```bash
# Run unit tests (same as CI)
./gradlew testDebugUnitTest

# Run all tests with coverage report
./gradlew test

# Run tests for specific module
./gradlew :shared:test
./gradlew :androidApp:test
```

### Platform-Specific Tests
```bash
# iOS Simulator tests
./gradlew iosSimulatorArm64Test

# Android instrumented tests
./gradlew connectedAndroidTest
```

### Test Reports
Test reports are generated in:
- `build/reports/tests/` - HTML test reports
- `build/test-results/` - XML test results for CI

## Code Quality

### Lint Analysis
```bash
# Run lint analysis
./gradlew lint

# Fix lint issues automatically
./gradlew lintFix

# Check specific module
./gradlew :androidApp:lint
```

### Code Formatting
```bash
# Format Kotlin code
./gradlew ktlintFormat

# Check code formatting
./gradlew ktlintCheck
```

## Dependency Management

```bash
# Show dependency tree
./gradlew dependencies

# Check for dependency updates
./gradlew dependencyUpdates
```

## Clean and Reset

```bash
# Clean build artifacts
./gradlew clean

# Clean and invalidate caches
./gradlew clean --rerun-tasks

# Deep clean (including .gradle folder)
rm -rf .gradle build */build
```

## CI/CD Commands

```bash
# Run the same checks as CI
./gradlew clean testDebugUnitTest lint

# Generate release build
./gradlew assembleRelease
```

## Update History
- 2025-06-21: Initial documentation created from CLAUDE.md