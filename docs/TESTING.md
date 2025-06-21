# Testing Guidelines

This document outlines the testing strategy, standards, and practices for the Habit Tracker project.

## Testing Philosophy

The project follows a comprehensive testing approach with emphasis on:
- **Isolation**: Each component tested independently with mocked dependencies
- **Coverage**: Aim for high test coverage across all layers
- **Maintainability**: Tests should be clear, concise, and easy to update
- **Speed**: Unit tests should run quickly to encourage frequent execution

## Testing Stack

### Core Testing Libraries
- **kotlin.test**: Cross-platform test framework for assertions
- **MockK**: Kotlin-native mocking library with coroutine support
- **kotlinx-coroutines-test**: Testing utilities for coroutines and Flow
- **JUnit**: Test runner and lifecycle management
- **Room Testing**: In-memory database for DAO testing

## Testing by Layer

### Domain Layer Testing

**What to Test:**
- All use case business logic
- Entity validation rules
- Domain service operations

**Example:**
```kotlin
class GetTodayTasksUseCaseTest {
    private val mockRepository = mockk<HabitRepository>()
    private val useCase = GetTodayTasksUseCase(mockRepository)
    
    @Test
    fun `should generate tasks for active habits`() = runTest {
        // Given
        val activeHabits = listOf(createTestHabit(isActive = true))
        every { mockRepository.getActiveHabits() } returns flowOf(activeHabits)
        
        // When
        val tasks = useCase().first()
        
        // Then
        assertTrue(tasks.isNotEmpty())
        verify { mockRepository.getActiveHabits() }
    }
}
```

### Data Layer Testing

**What to Test:**
- Repository implementations
- Data mappers
- Database operations (with in-memory database)
- Network responses (when applicable)

**Example:**
```kotlin
class HabitRepositoryImplTest {
    private val mockDao = mockk<HabitDao>()
    private val repository = HabitRepositoryImpl(mockDao)
    
    @Test
    fun `should map entities to domain models`() = runTest {
        // Given
        val entities = listOf(createTestHabitEntity())
        coEvery { mockDao.getAllHabits() } returns flowOf(entities)
        
        // When
        val habits = repository.getAllHabits().first()
        
        // Then
        assertEquals(entities.size, habits.size)
        assertEquals(entities[0].name, habits[0].name)
    }
}
```

### Presentation Layer Testing

**What to Test:**
- ViewModel state management
- User interaction handling
- Navigation logic
- UI state transformations

**Example:**
```kotlin
class HabitListViewModelTest {
    private val mockUseCase = mockk<GetAllHabitsUseCase>()
    private val viewModel = HabitListViewModel(mockUseCase)
    
    @Test
    fun `should update UI state when loading habits`() = runTest {
        // Given
        val habits = listOf(createTestHabit())
        coEvery { mockUseCase() } returns flowOf(habits)
        
        // When
        viewModel.loadHabits()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(habits, state.habits)
        assertNull(state.error)
    }
}
```

## Testing Standards

### Naming Conventions

Use descriptive test names with backticks:
```kotlin
@Test
fun `should complete task when user taps checkbox`() { }

@Test
fun `should show error when network request fails`() { }
```

### Test Structure

Follow the Given-When-Then pattern:
```kotlin
@Test
fun `test description`() {
    // Given - Setup test data and mocks
    val testData = createTestData()
    
    // When - Execute the action
    val result = systemUnderTest.performAction(testData)
    
    // Then - Verify the outcome
    assertEquals(expected, result)
}
```

### Mock Usage

1. **Use MockK for Kotlin code** - Better support for coroutines and Kotlin features
2. **Mock external dependencies only** - Don't mock data classes or simple objects
3. **Verify interactions when appropriate** - But focus on state verification over interaction verification

### Test Data

Create test data builders for common objects:
```kotlin
fun createTestHabit(
    id: Long = 1L,
    name: String = "Test Habit",
    isActive: Boolean = true
) = Habit(
    id = id,
    name = name,
    description = "Test description",
    color = "#2196F3",
    isActive = isActive,
    createdAt = Clock.System.todayIn(TimeZone.currentSystemDefault())
)
```

## Running Tests

### Command Line
```bash
# Run all tests
./gradlew test

# Run tests with coverage
./gradlew testDebugUnitTest

# Run specific module tests
./gradlew :shared:test

# Run Android instrumented tests
./gradlew connectedAndroidTest

# Run iOS tests
./gradlew iosSimulatorArm64Test
```

### Test Reports
- HTML reports: `build/reports/tests/`
- Coverage reports: `build/reports/coverage/`
- XML results: `build/test-results/`

## Continuous Integration

Tests are automatically run on:
- Every push to feature branches
- Pull request creation and updates
- Main branch commits

Required checks:
- All unit tests must pass
- Code coverage threshold: 70% (aim for 80%+)
- No test flakiness allowed

## Best Practices

### Do's
- ✅ Write tests before or alongside implementation (TDD/BDD)
- ✅ Keep tests focused on one behavior
- ✅ Use meaningful test data
- ✅ Test edge cases and error scenarios
- ✅ Mock external dependencies
- ✅ Use coroutine test utilities for suspend functions

### Don'ts
- ❌ Don't test implementation details
- ❌ Don't write overly complex test setups
- ❌ Don't ignore flaky tests
- ❌ Don't test framework code
- ❌ Don't use real network calls or databases in unit tests

## Platform-Specific Testing

### Android
- Use `@RunWith(AndroidJUnit4::class)` for instrumented tests
- Test with different API levels in CI
- Verify UI with Espresso when needed

### iOS
- Tests run on iOS simulator
- Use `iosSimulatorArm64Test` task
- Platform-specific implementations tested separately

## Debugging Tests

### Common Issues

**"Mockk not initialized"**
```kotlin
@BeforeTest
fun setup() {
    MockKAnnotations.init(this)
}
```

**Coroutine timing issues**
```kotlin
@Test
fun testWithCoroutines() = runTest {
    // Use runTest for proper coroutine handling
}
```

**Flow collection hanging**
```kotlin
// Use first() or take(1) for single emissions
val result = flow.first()
```

## Update History
- 2025-06-21: Initial testing documentation created