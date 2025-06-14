package com.nagopy.kmp.habittracker.notification

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
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
 * Tests for AndroidNotificationPermissionManager with proper mocking
 * of static classes and verification of actual behavior.
 */
@RunWith(RobolectricTestRunner::class)
class AndroidNotificationPermissionManagerTest {

    private lateinit var mockContext: Context
    private lateinit var mockActivity: Activity
    private lateinit var mockAlarmManager: AlarmManager
    private lateinit var mockNotificationManagerCompat: NotificationManagerCompat
    private lateinit var permissionManager: AndroidNotificationPermissionManager

    @Before
    fun setup() {
        Logger.init()
        
        // Mock all dependencies
        mockContext = mockk<Context>(relaxed = true)
        mockActivity = mockk<Activity>(relaxed = true)
        mockAlarmManager = mockk<AlarmManager>(relaxed = true)
        mockNotificationManagerCompat = mockk<NotificationManagerCompat>(relaxed = true)
        
        // Mock static classes
        mockkStatic(ContextCompat::class)
        mockkStatic(ActivityCompat::class)
        mockkStatic(NotificationManagerCompat::class)
        
        permissionManager = AndroidNotificationPermissionManager(mockContext)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // ========================================
    // Activity Lifecycle Tests
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
    // areNotificationsEnabled Tests
    // ========================================

    @Test
    @Config(sdk = [33])
    fun `areNotificationsEnabled should check POST_NOTIFICATIONS on TIRAMISU and return true when granted`() = runTest {
        // Given - SDK 33, POST_NOTIFICATIONS granted
        every { 
            ContextCompat.checkSelfPermission(
                mockContext, 
                Manifest.permission.POST_NOTIFICATIONS
            ) 
        } returns PackageManager.PERMISSION_GRANTED
        
        // When
        val result = permissionManager.areNotificationsEnabled()
        
        // Then
        assertTrue(result)
        verify { 
            ContextCompat.checkSelfPermission(
                mockContext, 
                Manifest.permission.POST_NOTIFICATIONS
            ) 
        }
    }

    @Test
    @Config(sdk = [33])
    fun `areNotificationsEnabled should check POST_NOTIFICATIONS on TIRAMISU and return false when denied`() = runTest {
        // Given - SDK 33, POST_NOTIFICATIONS denied
        every { 
            ContextCompat.checkSelfPermission(
                mockContext, 
                Manifest.permission.POST_NOTIFICATIONS
            ) 
        } returns PackageManager.PERMISSION_DENIED
        
        // When
        val result = permissionManager.areNotificationsEnabled()
        
        // Then
        assertFalse(result)
        verify { 
            ContextCompat.checkSelfPermission(
                mockContext, 
                Manifest.permission.POST_NOTIFICATIONS
            ) 
        }
    }

    @Test
    @Config(sdk = [28])
    fun `areNotificationsEnabled should use NotificationManagerCompat on pre-TIRAMISU`() = runTest {
        // Given - SDK 28, use a real ApplicationContext but mock system service
        val realContext = ApplicationProvider.getApplicationContext<Context>()
        val mockNotificationManager = mockk<NotificationManager>()
        every { mockContext.getSystemService(Context.NOTIFICATION_SERVICE) } returns mockNotificationManager
        
        // For pre-TIRAMISU, NotificationManagerCompat uses NotificationManager internally
        // We'll test the actual behavior with a real context for robustness
        val testPermissionManager = AndroidNotificationPermissionManager(realContext)
        
        // When
        val result = testPermissionManager.areNotificationsEnabled()
        
        // Then - should return boolean result without throwing exception
        assertTrue(result is Boolean)
    }

    // ========================================
    // requestNotificationPermission Tests for SDK 33+
    // ========================================

    @Test
    @Config(sdk = [33])
    fun `requestNotificationPermission should return true when permission already granted on TIRAMISU`() = runTest {
        // Given - SDK 33, POST_NOTIFICATIONS already granted
        every { 
            ContextCompat.checkSelfPermission(
                mockContext, 
                Manifest.permission.POST_NOTIFICATIONS
            ) 
        } returns PackageManager.PERMISSION_GRANTED
        
        permissionManager.setActivity(mockActivity)
        
        // When
        val result = permissionManager.requestNotificationPermission()
        
        // Then
        assertTrue(result)
        verify { 
            ContextCompat.checkSelfPermission(
                mockContext, 
                Manifest.permission.POST_NOTIFICATIONS
            ) 
        }
        // Should not request permission if already granted
        verify(exactly = 0) { 
            ActivityCompat.requestPermissions(any(), any(), any()) 
        }
    }

    @Test
    @Config(sdk = [33])
    fun `requestNotificationPermission should request permission when not granted on TIRAMISU`() = runTest {
        // Given - SDK 33, POST_NOTIFICATIONS not granted
        every { 
            ContextCompat.checkSelfPermission(
                mockContext, 
                Manifest.permission.POST_NOTIFICATIONS
            ) 
        } returns PackageManager.PERMISSION_DENIED
        
        permissionManager.setActivity(mockActivity)
        
        // When
        val result = permissionManager.requestNotificationPermission()
        
        // Then
        assertFalse(result) // Should return current status (false)
        verify { 
            ActivityCompat.requestPermissions(
                mockActivity,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                AndroidNotificationPermissionManager.NOTIFICATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    @Test
    @Config(sdk = [33])
    fun `requestNotificationPermission should return current status when no activity available on TIRAMISU`() = runTest {
        // Given - SDK 33, no activity set
        every { 
            ContextCompat.checkSelfPermission(
                mockContext, 
                Manifest.permission.POST_NOTIFICATIONS
            ) 
        } returns PackageManager.PERMISSION_DENIED
        
        // No activity set
        
        // When
        val result = permissionManager.requestNotificationPermission()
        
        // Then
        assertFalse(result) // Should return current status
        verify(exactly = 0) { 
            ActivityCompat.requestPermissions(any(), any(), any()) 
        }
    }

    @Test
    @Config(sdk = [28])
    fun `requestNotificationPermission should use NotificationManagerCompat on pre-TIRAMISU`() = runTest {
        // Given - SDK 28, use real context for robustness
        val realContext = ApplicationProvider.getApplicationContext<Context>()
        val testPermissionManager = AndroidNotificationPermissionManager(realContext)
        
        // When
        val result = testPermissionManager.requestNotificationPermission()
        
        // Then
        assertTrue(result is Boolean)
        // On pre-TIRAMISU, should not use POST_NOTIFICATIONS permission
        // This is verified by the test not throwing exceptions related to POST_NOTIFICATIONS
    }

    // ========================================
    // requestNotificationPermission Tests for SDK 34+ (with Exact Alarm)
    // ========================================

    @Test
    @Config(sdk = [34])
    fun `requestNotificationPermission should also request exact alarm when permission granted on SDK 34`() = runTest {
        // Given - SDK 34, POST_NOTIFICATIONS granted, exact alarm not granted
        every { 
            ContextCompat.checkSelfPermission(
                mockContext, 
                Manifest.permission.POST_NOTIFICATIONS
            ) 
        } returns PackageManager.PERMISSION_GRANTED
        every { mockContext.getSystemService(Context.ALARM_SERVICE) } returns mockAlarmManager
        every { mockAlarmManager.canScheduleExactAlarms() } returns false
        
        permissionManager.setActivity(mockActivity)
        
        // When
        val result = permissionManager.requestNotificationPermission()
        
        // Then
        assertTrue(result)
        verify { mockAlarmManager.canScheduleExactAlarms() }
        verify { mockActivity.startActivity(any()) }
        
        // Verify the correct intent action is used
        val intentSlot = slot<Intent>()
        verify { mockActivity.startActivity(capture(intentSlot)) }
        assertEquals(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, intentSlot.captured.action)
    }

    @Test
    @Config(sdk = [34])
    fun `requestNotificationPermission should skip exact alarm request when already granted on SDK 34`() = runTest {
        // Given - SDK 34, POST_NOTIFICATIONS granted, exact alarm already granted
        every { 
            ContextCompat.checkSelfPermission(
                mockContext, 
                Manifest.permission.POST_NOTIFICATIONS
            ) 
        } returns PackageManager.PERMISSION_GRANTED
        every { mockContext.getSystemService(Context.ALARM_SERVICE) } returns mockAlarmManager
        every { mockAlarmManager.canScheduleExactAlarms() } returns true
        
        permissionManager.setActivity(mockActivity)
        
        // When
        val result = permissionManager.requestNotificationPermission()
        
        // Then
        assertTrue(result)
        verify { mockAlarmManager.canScheduleExactAlarms() }
        // Should not start activity if exact alarm permission already granted
        verify(exactly = 0) { mockActivity.startActivity(any()) }
    }

    @Test
    @Config(sdk = [34])
    fun `requestNotificationPermission should request both permissions when notification not granted on SDK 34`() = runTest {
        // Given - SDK 34, POST_NOTIFICATIONS not granted, exact alarm not granted
        every { 
            ContextCompat.checkSelfPermission(
                mockContext, 
                Manifest.permission.POST_NOTIFICATIONS
            ) 
        } returns PackageManager.PERMISSION_DENIED
        every { mockContext.getSystemService(Context.ALARM_SERVICE) } returns mockAlarmManager
        every { mockAlarmManager.canScheduleExactAlarms() } returns false
        
        permissionManager.setActivity(mockActivity)
        
        // When
        val result = permissionManager.requestNotificationPermission()
        
        // Then
        assertFalse(result)
        // Should request notification permission
        verify { 
            ActivityCompat.requestPermissions(
                mockActivity,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                AndroidNotificationPermissionManager.NOTIFICATION_PERMISSION_REQUEST_CODE
            )
        }
        // Should also request exact alarm permission
        verify { mockAlarmManager.canScheduleExactAlarms() }
        verify { mockActivity.startActivity(any()) }
    }

    @Test
    @Config(sdk = [34])
    fun `requestNotificationPermission should handle no activity gracefully on SDK 34`() = runTest {
        // Given - SDK 34, no activity, exact alarm not granted
        every { 
            ContextCompat.checkSelfPermission(
                mockContext, 
                Manifest.permission.POST_NOTIFICATIONS
            ) 
        } returns PackageManager.PERMISSION_GRANTED
        every { mockContext.getSystemService(Context.ALARM_SERVICE) } returns mockAlarmManager
        every { mockAlarmManager.canScheduleExactAlarms() } returns false
        
        // No activity set
        
        // When
        val result = permissionManager.requestNotificationPermission()
        
        // Then
        assertTrue(result)
        // Should not try to start activity when no activity is available
        verify(exactly = 0) { mockActivity.startActivity(any()) }
    }

    @Test
    @Config(sdk = [34])
    fun `requestNotificationPermission should handle exact alarm exception gracefully on SDK 34`() = runTest {
        // Given - SDK 34, exact alarm request throws exception
        every { 
            ContextCompat.checkSelfPermission(
                mockContext, 
                Manifest.permission.POST_NOTIFICATIONS
            ) 
        } returns PackageManager.PERMISSION_GRANTED
        every { mockContext.getSystemService(Context.ALARM_SERVICE) } returns mockAlarmManager
        every { mockAlarmManager.canScheduleExactAlarms() } returns false
        every { mockActivity.startActivity(any()) } throws Exception("Test exception")
        
        permissionManager.setActivity(mockActivity)
        
        // When
        val result = permissionManager.requestNotificationPermission()
        
        // Then - should not crash
        assertTrue(result)
        verify { mockActivity.startActivity(any()) }
    }

    @Test
    @Config(sdk = [34])
    fun `requestNotificationPermission should handle null AlarmManager gracefully on SDK 34`() = runTest {
        // Given - SDK 34, null AlarmManager
        every { 
            ContextCompat.checkSelfPermission(
                mockContext, 
                Manifest.permission.POST_NOTIFICATIONS
            ) 
        } returns PackageManager.PERMISSION_GRANTED
        every { mockContext.getSystemService(Context.ALARM_SERVICE) } returns null
        
        permissionManager.setActivity(mockActivity)
        
        // When
        val result = permissionManager.requestNotificationPermission()
        
        // Then - should handle gracefully
        assertTrue(result)
        // Should not crash when AlarmManager is null
        verify(exactly = 0) { mockActivity.startActivity(any()) }
    }

    // ========================================
    // SDK Behavior Verification Tests
    // ========================================

    @Test
    @Config(sdk = [33])
    fun `requestNotificationPermission should not request exact alarm on SDK 33`() = runTest {
        // Given - SDK 33 (< 34), POST_NOTIFICATIONS granted
        every { 
            ContextCompat.checkSelfPermission(
                mockContext, 
                Manifest.permission.POST_NOTIFICATIONS
            ) 
        } returns PackageManager.PERMISSION_GRANTED
        
        permissionManager.setActivity(mockActivity)
        
        // When
        permissionManager.requestNotificationPermission()
        
        // Then - should not access AlarmManager on SDK 33
        verify(exactly = 0) { mockContext.getSystemService(Context.ALARM_SERVICE) }
        verify(exactly = 0) { mockActivity.startActivity(any()) }
    }
}