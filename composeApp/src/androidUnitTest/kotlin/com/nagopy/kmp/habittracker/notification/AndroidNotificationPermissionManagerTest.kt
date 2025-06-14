package com.nagopy.kmp.habittracker.notification

import android.content.Context
import android.app.Activity
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import io.mockk.mockk
import kotlin.test.assertNotNull

/**
 * Tests for AndroidNotificationPermissionManager to verify basic functionality
 * without requiring actual permission system interactions.
 */
@RunWith(RobolectricTestRunner::class)
class AndroidNotificationPermissionManagerTest {

    private lateinit var context: Context
    private lateinit var permissionManager: AndroidNotificationPermissionManager

    @Before
    fun setup() {
        // Initialize logger first to avoid potential issues
        com.nagopy.kmp.habittracker.util.Logger.init()
        
        context = ApplicationProvider.getApplicationContext()
        permissionManager = AndroidNotificationPermissionManager(context)
    }

    @Test
    @Config(sdk = [28]) // SDK 28 - pre-TIRAMISU, should use old notification logic
    fun `should create AndroidNotificationPermissionManager without crashing on SDK 28`() {
        // When creating the permission manager
        val manager = AndroidNotificationPermissionManager(context)
        
        // Then it should be created successfully
        assertNotNull(manager)
    }

    @Test
    @Config(sdk = [34]) // SDK 34 - Android 14, should trigger exact alarm permission logic
    fun `should create AndroidNotificationPermissionManager without crashing on SDK 34`() {
        // When creating the permission manager
        val manager = AndroidNotificationPermissionManager(context)
        
        // Then it should be created successfully
        assertNotNull(manager)
    }

    @Test
    @Config(sdk = [28])
    fun `should handle activity reference management without crashing on SDK 28`() {
        // Given
        val mockActivity = mockk<Activity>()
        
        // When setting and clearing activity reference
        permissionManager.setActivity(mockActivity)
        permissionManager.clearActivity()
        
        // Then no exceptions should be thrown
    }

    @Test
    @Config(sdk = [34])
    fun `should handle activity reference management without crashing on SDK 34`() {
        // Given
        val mockActivity = mockk<Activity>()
        
        // When setting and clearing activity reference
        permissionManager.setActivity(mockActivity)
        permissionManager.clearActivity()
        
        // Then no exceptions should be thrown
    }

    @Test
    @Config(sdk = [28])
    fun `areNotificationsEnabled should not crash on SDK 28`() = runTest {
        // When checking notification status
        val result = permissionManager.areNotificationsEnabled()
        
        // Then it should return a boolean without crashing
        // Note: We can't assert specific behavior since it depends on system state
        // This test just ensures the method doesn't crash
    }

    @Test
    @Config(sdk = [33]) // SDK 33 - TIRAMISU, uses POST_NOTIFICATIONS but no exact alarm logic
    fun `areNotificationsEnabled should not crash on SDK 33`() = runTest {
        // When checking notification status
        val result = permissionManager.areNotificationsEnabled()
        
        // Then it should return a boolean without crashing
    }

    @Test
    @Config(sdk = [34]) // SDK 34 - Android 14, should include exact alarm logic
    fun `areNotificationsEnabled should not crash on SDK 34`() = runTest {
        // When checking notification status  
        val result = permissionManager.areNotificationsEnabled()
        
        // Then it should return a boolean without crashing
    }

    @Test
    @Config(sdk = [28])
    fun `requestNotificationPermission should not crash when no activity is set on SDK 28`() = runTest {
        // When requesting permission without activity
        val result = permissionManager.requestNotificationPermission()
        
        // Then it should return a boolean without crashing
        // Note: On SDK 28, this should use the old NotificationManagerCompat path
    }

    @Test
    @Config(sdk = [33]) 
    fun `requestNotificationPermission should not crash when no activity is set on SDK 33`() = runTest {
        // When requesting permission without activity
        val result = permissionManager.requestNotificationPermission()
        
        // Then it should return a boolean without crashing
        // Note: On SDK 33, this should request POST_NOTIFICATIONS but not exact alarm permission
    }

    @Test
    @Config(sdk = [34])
    fun `requestNotificationPermission should not crash when no activity is set on SDK 34`() = runTest {
        // When requesting permission without activity
        val result = permissionManager.requestNotificationPermission()
        
        // Then it should return a boolean without crashing
        // Note: On SDK 34+, this should request both POST_NOTIFICATIONS and exact alarm permission
        // but should handle the case where no activity is available for exact alarm permission gracefully
    }

    @Test
    @Config(sdk = [34])
    fun `requestNotificationPermission should handle SDK 34 exact alarm logic with activity`() = runTest {
        // Given
        val mockActivity = mockk<Activity>(relaxed = true)
        permissionManager.setActivity(mockActivity)
        
        // When requesting permission with activity available
        val result = permissionManager.requestNotificationPermission()
        
        // Then it should return a boolean without crashing
        // Note: This tests that the SDK 34+ exact alarm permission logic executes 
        // when an activity is available, but we can't verify the exact behavior 
        // due to system dependencies
    }
}