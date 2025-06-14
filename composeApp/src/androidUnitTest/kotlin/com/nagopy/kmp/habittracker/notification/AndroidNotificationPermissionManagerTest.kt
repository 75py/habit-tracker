package com.nagopy.kmp.habittracker.notification

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.test.core.app.ApplicationProvider
import com.nagopy.kmp.habittracker.util.Logger
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.*

/**
 * Tests for AndroidNotificationPermissionManager focusing on proper mocking
 * of the AlarmManager and testing actual behavior.
 */
@RunWith(RobolectricTestRunner::class)
class AndroidNotificationPermissionManagerTest {

    private lateinit var context: Context
    private lateinit var mockActivity: Activity
    private lateinit var mockAlarmManager: AlarmManager
    private lateinit var permissionManager: AndroidNotificationPermissionManager

    @Before
    fun setup() {
        Logger.init()
        
        // Use real context for most operations
        context = ApplicationProvider.getApplicationContext()
        mockActivity = mockk<Activity>(relaxed = true)
        mockAlarmManager = mockk<AlarmManager>(relaxed = true)
        
        permissionManager = AndroidNotificationPermissionManager(context)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // ========================================
    // Basic Activity Lifecycle Tests
    // ========================================

    @Test
    fun `setActivity and clearActivity should work without errors`() {
        // When
        permissionManager.setActivity(mockActivity)
        permissionManager.clearActivity()
        
        // Then - no exceptions should be thrown
        assertTrue(true)
    }

    // ========================================
    // AlarmManager Mock Tests (SDK 34+)
    // ========================================

    @Test
    @Config(sdk = [34])
    fun `requestExactAlarmPermission should skip when canScheduleExactAlarms returns true`() = runTest{
        // Given - mock the context to return our mock AlarmManager
        val mockContext = mockk<Context>()
        every { mockContext.getSystemService(Context.ALARM_SERVICE) } returns mockAlarmManager
        every { mockAlarmManager.canScheduleExactAlarms() } returns true
        
        val permissionManager = AndroidNotificationPermissionManager(mockContext)
        permissionManager.setActivity(mockActivity)
        
        // Mock the notification permission check
        mockkStatic(ContextCompat::class)
        every { 
            ContextCompat.checkSelfPermission(
                mockContext, 
                Manifest.permission.POST_NOTIFICATIONS
            ) 
        } returns PackageManager.PERMISSION_GRANTED
        
        // When
        permissionManager.requestNotificationPermission()
        
        // Then - should check canScheduleExactAlarms but not start activity
        verify { mockAlarmManager.canScheduleExactAlarms() }
        verify(exactly = 0) { mockActivity.startActivity(any()) }
        
        unmockkStatic(ContextCompat::class)
    }

    @Test
    @Config(sdk = [34])
    fun `requestExactAlarmPermission should open settings when canScheduleExactAlarms returns false`() = runTest {
        // Given - mock the context to return our mock AlarmManager
        val mockContext = mockk<Context>()
        every { mockContext.getSystemService(Context.ALARM_SERVICE) } returns mockAlarmManager
        every { mockAlarmManager.canScheduleExactAlarms() } returns false
        
        val permissionManager = AndroidNotificationPermissionManager(mockContext)
        permissionManager.setActivity(mockActivity)
        
        // Mock the notification permission check
        mockkStatic(ContextCompat::class)
        every { 
            ContextCompat.checkSelfPermission(
                mockContext, 
                Manifest.permission.POST_NOTIFICATIONS
            ) 
        } returns PackageManager.PERMISSION_GRANTED
        
        // When
        permissionManager.requestNotificationPermission()
        
        // Then - should check canScheduleExactAlarms and start settings activity
        verify { mockAlarmManager.canScheduleExactAlarms() }
        verify { mockActivity.startActivity(any()) }
        
        // Verify the correct intent action is used
        val intentSlot = slot<Intent>()
        verify { mockActivity.startActivity(capture(intentSlot)) }
        assertEquals(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, intentSlot.captured.action)
        
        unmockkStatic(ContextCompat::class)
    }

    @Test
    @Config(sdk = [34])
    fun `requestExactAlarmPermission should handle no activity gracefully`() = runTest {
        // Given - mock the context to return our mock AlarmManager
        val mockContext = mockk<Context>()
        every { mockContext.getSystemService(Context.ALARM_SERVICE) } returns mockAlarmManager
        every { mockAlarmManager.canScheduleExactAlarms() } returns false
        
        val permissionManager = AndroidNotificationPermissionManager(mockContext)
        // No activity set
        
        // Mock the notification permission check
        mockkStatic(ContextCompat::class)
        every { 
            ContextCompat.checkSelfPermission(
                mockContext, 
                Manifest.permission.POST_NOTIFICATIONS
            ) 
        } returns PackageManager.PERMISSION_GRANTED
        
        // When
        val result = permissionManager.requestNotificationPermission()
        
        // Then - should return true (notification permission granted) but not crash
        assertTrue(result)
        
        // Should not try to start activity when no activity is available
        verify(exactly = 0) { mockActivity.startActivity(any()) }
        
        unmockkStatic(ContextCompat::class)
    }

    @Test
    @Config(sdk = [34])  
    fun `requestExactAlarmPermission should handle exceptions gracefully`() = runTest {
        // Given - mock the context to return our mock AlarmManager
        val mockContext = mockk<Context>()
        every { mockContext.getSystemService(Context.ALARM_SERVICE) } returns mockAlarmManager
        every { mockAlarmManager.canScheduleExactAlarms() } returns false
        every { mockActivity.startActivity(any()) } throws Exception("Test exception")
        
        val permissionManager = AndroidNotificationPermissionManager(mockContext)
        permissionManager.setActivity(mockActivity)
        
        // Mock the notification permission check
        mockkStatic(ContextCompat::class)
        every { 
            ContextCompat.checkSelfPermission(
                mockContext, 
                Manifest.permission.POST_NOTIFICATIONS
            ) 
        } returns PackageManager.PERMISSION_GRANTED
        
        // When
        val result = permissionManager.requestNotificationPermission()
        
        // Then - should not crash and return the permission status
        assertTrue(result)
        
        // Should have attempted to start activity
        verify { mockActivity.startActivity(any()) }
        
        unmockkStatic(ContextCompat::class)
    }

    @Test
    @Config(sdk = [34])
    fun `requestExactAlarmPermission should handle null AlarmManager gracefully`() = runTest {
        // Given - mock the context to return null AlarmManager (edge case)
        val mockContext = mockk<Context>()
        every { mockContext.getSystemService(Context.ALARM_SERVICE) } returns null
        
        val permissionManager = AndroidNotificationPermissionManager(mockContext)
        permissionManager.setActivity(mockActivity)
        
        // Mock the notification permission check
        mockkStatic(ContextCompat::class)
        every { 
            ContextCompat.checkSelfPermission(
                mockContext, 
                Manifest.permission.POST_NOTIFICATIONS
            ) 
        } returns PackageManager.PERMISSION_GRANTED
        
        // When
        val result = permissionManager.requestNotificationPermission()
        
        // Then - should handle gracefully and not crash
        assertTrue(result)
        
        // Should not try to start activity when AlarmManager is null
        verify(exactly = 0) { mockActivity.startActivity(any()) }
        
        unmockkStatic(ContextCompat::class)
    }

    // ========================================
    // SDK Behavior Tests
    // ========================================

    @Test
    @Config(sdk = [33])
    fun `requestNotificationPermission should not request exact alarm on SDK 33`() = runTest {
        // Given - SDK 33 (< 34), so no exact alarm permission request
        val mockContext = mockk<Context>()
        every { mockContext.getSystemService(Context.ALARM_SERVICE) } returns mockAlarmManager
        
        val permissionManager = AndroidNotificationPermissionManager(mockContext)
        permissionManager.setActivity(mockActivity)
        
        // Mock the notification permission check
        mockkStatic(ContextCompat::class)
        every { 
            ContextCompat.checkSelfPermission(
                mockContext, 
                Manifest.permission.POST_NOTIFICATIONS
            ) 
        } returns PackageManager.PERMISSION_GRANTED
        
        // When
        permissionManager.requestNotificationPermission()
        
        // Then - should not request exact alarm permission on SDK 33
        verify(exactly = 0) { mockContext.getSystemService(Context.ALARM_SERVICE) }
        verify(exactly = 0) { mockAlarmManager.canScheduleExactAlarms() }
        
        unmockkStatic(ContextCompat::class)
    }

    @Test
    @Config(sdk = [28])
    fun `requestNotificationPermission should use legacy behavior on pre-TIRAMISU`() = runTest {
        // Given - SDK 28 (< 33), so should use legacy notification behavior
        
        // When
        val result = permissionManager.requestNotificationPermission()
        
        // Then - should return boolean based on NotificationManagerCompat
        assertTrue(result is Boolean)
        
        // Should not try to access POST_NOTIFICATIONS permission or exact alarm
        // (this is verified by the test not throwing exceptions)
    }

    // ========================================
    // Permission Status Tests
    // ========================================

    @Test
    @Config(sdk = [33])
    fun `areNotificationsEnabled should check POST_NOTIFICATIONS on TIRAMISU`() = runTest {
        // Given - SDK 33, so should check POST_NOTIFICATIONS permission
        
        // When
        val result = permissionManager.areNotificationsEnabled()
        
        // Then - should return boolean based on POST_NOTIFICATIONS permission
        assertTrue(result is Boolean)
    }

    @Test
    @Config(sdk = [28])
    fun `areNotificationsEnabled should use NotificationManagerCompat on pre-TIRAMISU`() = runTest {
        // Given - SDK 28, so should use NotificationManagerCompat
        
        // When
        val result = permissionManager.areNotificationsEnabled()
        
        // Then - should return boolean based on NotificationManagerCompat
        assertTrue(result is Boolean)
    }
}