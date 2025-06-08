package com.nagopy.kmp.habittracker.domain.usecase

import com.nagopy.kmp.habittracker.domain.notification.NotificationPermissionManager
import com.nagopy.kmp.habittracker.domain.storage.AppPreferences
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse

/**
 * Test for RequestNotificationPermissionOnStartupUseCase.
 * This validates the core logic without platform-specific behavior.
 */
class RequestNotificationPermissionOnStartupUseCaseTest {
    
    private class MockNotificationPermissionManager(
        private var enabled: Boolean = false,
        private var grantPermission: Boolean = true
    ) : NotificationPermissionManager {
        
        var requestCalled = false
        
        override suspend fun areNotificationsEnabled(): Boolean = enabled
        
        override suspend fun requestNotificationPermission(): Boolean {
            requestCalled = true
            enabled = grantPermission
            return grantPermission
        }
    }
    
    private class MockAppPreferences : AppPreferences {
        private val storage = mutableMapOf<String, Boolean>()
        
        override suspend fun getBoolean(key: String, defaultValue: Boolean): Boolean {
            return storage[key] ?: defaultValue
        }
        
        override suspend fun setBoolean(key: String, value: Boolean) {
            storage[key] = value
        }
    }
    
    @Test
    fun `should request permission on first startup when notifications disabled`() = runTest {
        // Arrange
        val permissionManager = MockNotificationPermissionManager(enabled = false, grantPermission = true)
        val preferences = MockAppPreferences()
        val useCase = RequestNotificationPermissionOnStartupUseCase(permissionManager, preferences)
        
        // Act
        val result = useCase()
        
        // Assert
        assertTrue(result, "Should return true when permission granted")
        assertTrue(permissionManager.requestCalled, "Should request permission")
        assertTrue(
            preferences.getBoolean(AppPreferences.KEY_NOTIFICATION_PERMISSION_REQUESTED),
            "Should mark permission as requested"
        )
    }
    
    @Test
    fun `should not request permission again after first startup`() = runTest {
        // Arrange
        val permissionManager = MockNotificationPermissionManager(enabled = false)
        val preferences = MockAppPreferences()
        val useCase = RequestNotificationPermissionOnStartupUseCase(permissionManager, preferences)
        
        // First call
        useCase()
        permissionManager.requestCalled = false
        
        // Act - second call
        val result = useCase()
        
        // Assert
        assertFalse(result, "Should return false when notifications disabled on subsequent calls")
        assertFalse(permissionManager.requestCalled, "Should not request permission again")
    }
    
    @Test
    fun `should return true immediately when notifications already enabled`() = runTest {
        // Arrange
        val permissionManager = MockNotificationPermissionManager(enabled = true)
        val preferences = MockAppPreferences()
        val useCase = RequestNotificationPermissionOnStartupUseCase(permissionManager, preferences)
        
        // Act
        val result = useCase()
        
        // Assert
        assertTrue(result, "Should return true when notifications already enabled")
        assertTrue(
            preferences.getBoolean(AppPreferences.KEY_NOTIFICATION_PERMISSION_REQUESTED),
            "Should still mark permission as requested"
        )
    }
}