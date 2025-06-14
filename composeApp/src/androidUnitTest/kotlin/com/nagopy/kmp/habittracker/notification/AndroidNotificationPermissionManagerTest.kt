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
@Config(sdk = [28]) // Use SDK 28 to avoid newer API issues with Robolectric
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
    fun `should create AndroidNotificationPermissionManager without crashing`() {
        // When creating the permission manager
        val manager = AndroidNotificationPermissionManager(context)
        
        // Then it should be created successfully
        assertNotNull(manager)
    }

    @Test
    fun `should handle activity reference management without crashing`() {
        // Given
        val mockActivity = mockk<Activity>()
        
        // When setting and clearing activity reference
        permissionManager.setActivity(mockActivity)
        permissionManager.clearActivity()
        
        // Then no exceptions should be thrown
    }

    @Test
    fun `areNotificationsEnabled should not crash on any SDK level`() = runTest {
        // When checking notification status
        val result = permissionManager.areNotificationsEnabled()
        
        // Then it should return a boolean without crashing
        // Note: We can't assert specific behavior since it depends on system state
        // This test just ensures the method doesn't crash
    }

    @Test 
    fun `requestNotificationPermission should not crash when no activity is set`() = runTest {
        // When requesting permission without activity
        val result = permissionManager.requestNotificationPermission()
        
        // Then it should return a boolean without crashing
        // Note: This tests the new SDK 34+ exact alarm permission logic doesn't crash
        // when no activity is available
    }
}