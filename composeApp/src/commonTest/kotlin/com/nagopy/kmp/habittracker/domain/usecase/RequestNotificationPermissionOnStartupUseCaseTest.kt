package com.nagopy.kmp.habittracker.domain.usecase

import com.nagopy.kmp.habittracker.domain.notification.NotificationPermissionManager
import com.nagopy.kmp.habittracker.domain.storage.AppPreferences
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse

/**
 * Test for RequestNotificationPermissionOnStartupUseCase.
 * This validates the core logic without platform-specific behavior.
 */
class RequestNotificationPermissionOnStartupUseCaseTest {
    
    @Test
    fun `should request permission on first startup when notifications disabled`() = runTest {
        // Arrange
        val permissionManager = mockk<NotificationPermissionManager>()
        val preferences = mockk<AppPreferences>()
        val useCase = RequestNotificationPermissionOnStartupUseCase(permissionManager, preferences)
        
        coEvery { preferences.getBoolean(AppPreferences.KEY_NOTIFICATION_PERMISSION_REQUESTED, false) } returns false
        coEvery { permissionManager.requestNotificationPermission() } returns true
        coEvery { preferences.setBoolean(AppPreferences.KEY_NOTIFICATION_PERMISSION_REQUESTED, true) } returns Unit
        
        // Act
        val result = useCase()
        
        // Assert
        assertTrue(result, "Should return true when permission granted")
        coVerify { permissionManager.requestNotificationPermission() }
        coVerify { preferences.setBoolean(AppPreferences.KEY_NOTIFICATION_PERMISSION_REQUESTED, true) }
    }
    
    @Test
    fun `should not request permission again after first startup`() = runTest {
        // Arrange
        val permissionManager = mockk<NotificationPermissionManager>()
        val preferences = mockk<AppPreferences>()
        val useCase = RequestNotificationPermissionOnStartupUseCase(permissionManager, preferences)
        
        coEvery { preferences.getBoolean(AppPreferences.KEY_NOTIFICATION_PERMISSION_REQUESTED, false) } returns true
        coEvery { permissionManager.areNotificationsEnabled() } returns false
        
        // Act
        val result = useCase()
        
        // Assert
        assertFalse(result, "Should return false when notifications disabled on subsequent calls")
        coVerify(exactly = 0) { permissionManager.requestNotificationPermission() }
        coVerify { permissionManager.areNotificationsEnabled() }
    }
    
    @Test
    fun `should return true immediately when notifications already enabled`() = runTest {
        // Arrange
        val permissionManager = mockk<NotificationPermissionManager>()
        val preferences = mockk<AppPreferences>()
        val useCase = RequestNotificationPermissionOnStartupUseCase(permissionManager, preferences)
        
        coEvery { preferences.getBoolean(AppPreferences.KEY_NOTIFICATION_PERMISSION_REQUESTED, false) } returns false
        coEvery { permissionManager.requestNotificationPermission() } returns true
        coEvery { preferences.setBoolean(AppPreferences.KEY_NOTIFICATION_PERMISSION_REQUESTED, true) } returns Unit
        
        // Act
        val result = useCase()
        
        // Assert
        assertTrue(result, "Should return true when notifications already enabled")
        coVerify { preferences.setBoolean(AppPreferences.KEY_NOTIFICATION_PERMISSION_REQUESTED, true) }
    }
}