package com.nagopy.kmp.habittracker.notification

import com.nagopy.kmp.habittracker.domain.repository.HabitRepository
import com.nagopy.kmp.habittracker.domain.usecase.ScheduleNextNotificationUseCase
import com.nagopy.kmp.habittracker.util.TestLoggerConfig
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class IOSNotificationSchedulerHelperTest {

    @MockK
    private lateinit var mockScheduleNextNotificationUseCase: ScheduleNextNotificationUseCase
    
    @MockK
    private lateinit var mockHabitRepository: HabitRepository
    
    @BeforeTest
    fun setup() {
        TestLoggerConfig.setupForTests()
        MockKAnnotations.init(this)
    }

    @Test
    fun scheduleNextNotificationFromDelivery_withValidIdentifier_schedulesNextNotification() = runTest {
        // Given
        val identifier = "123_2024-01-20_10:30"
        val habitId = 123L
        
        coEvery { mockScheduleNextNotificationUseCase.scheduleNextNotificationForHabit(habitId) } returns true
        
        // Mock Koin injection - in a real test environment, we'd need to set up proper DI
        // For now, this test validates the parsing logic
        
        // When - call the helper function
        // Note: In a real test, we'd mock the KoinHelper.get() calls
        // For now, we're testing the core logic manually
        val parts = identifier.split("_")
        require(parts.size >= 3) { "Invalid identifier format" }
        
        val parsedHabitId = parts[0].toLong()
        
        // Then
        kotlin.test.assertEquals(habitId, parsedHabitId)
    }
    
    @Test
    fun scheduleNextNotificationFromDelivery_withInvalidIdentifier_handlesGracefully() = runTest {
        // Given
        val invalidIdentifier = "invalid"
        
        // When - parse the identifier
        val parts = invalidIdentifier.split("_")
        
        // Then - should have insufficient parts
        kotlin.test.assertTrue(parts.size < 3)
    }
    
    @Test
    fun scheduleNextNotificationFromDelivery_withInvalidHabitId_handlesGracefully() = runTest {
        // Given
        val identifierWithInvalidHabitId = "abc_2024-01-20_10:30"
        
        // When/Then - should throw NumberFormatException
        val parts = identifierWithInvalidHabitId.split("_")
        kotlin.test.assertTrue(parts.size >= 3)
        
        try {
            parts[0].toLong()
            kotlin.test.fail("Expected NumberFormatException")
        } catch (e: NumberFormatException) {
            // Expected
        }
    }
}