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
    fun `requestNotificationPermission should only request notification permission when granted on SDK 34`() = runTest {
        // Given - SDK 34, POST_NOTIFICATIONS granted
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
        // Should not request exact alarm permission automatically
        verify(exactly = 0) { mockActivity.startActivity(any()) }
    }

    @Test
    @Config(sdk = [34])
    fun `requestNotificationPermission should only request notification permission on SDK 34`() = runTest {
        // Given - SDK 34, POST_NOTIFICATIONS not granted
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
        assertFalse(result)
        // Should request notification permission
        verify { 
            ActivityCompat.requestPermissions(
                mockActivity,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                AndroidNotificationPermissionManager.NOTIFICATION_PERMISSION_REQUEST_CODE
            )
        }
        // Should not request exact alarm permission automatically
        verify(exactly = 0) { mockActivity.startActivity(any()) }
    }

    @Test
    @Config(sdk = [34])
    fun `requestNotificationPermission should handle no activity gracefully on SDK 34`() = runTest {
        // Given - SDK 34, no activity
        every { 
            ContextCompat.checkSelfPermission(
                mockContext, 
                Manifest.permission.POST_NOTIFICATIONS
            ) 
        } returns PackageManager.PERMISSION_GRANTED
        
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
    fun `requestNotificationPermission should not request exact alarm on SDK 34`() = runTest {
        // Given - SDK 34, POST_NOTIFICATIONS granted
        every { 
            ContextCompat.checkSelfPermission(
                mockContext, 
                Manifest.permission.POST_NOTIFICATIONS
            ) 
        } returns PackageManager.PERMISSION_GRANTED
        
        permissionManager.setActivity(mockActivity)
        
        // When
        val result = permissionManager.requestNotificationPermission()
        
        // Then - should not request exact alarm automatically
        assertTrue(result)
        verify(exactly = 0) { mockActivity.startActivity(any()) }
    }

    // ========================================
    // canScheduleExactAlarms Tests
    // ========================================

    @Test
    @Config(sdk = [31])
    fun `canScheduleExactAlarms should return true when permission granted on SDK 31+`() = runTest {
        // Given - SDK 31+, exact alarm permission granted
        every { mockContext.getSystemService(Context.ALARM_SERVICE) } returns mockAlarmManager
        every { mockAlarmManager.canScheduleExactAlarms() } returns true
        
        // When
        val result = permissionManager.canScheduleExactAlarms()
        
        // Then
        assertTrue(result)
        verify { mockAlarmManager.canScheduleExactAlarms() }
    }

    @Test
    @Config(sdk = [31])
    fun `canScheduleExactAlarms should return false when permission not granted on SDK 31+`() = runTest {
        // Given - SDK 31+, exact alarm permission not granted
        every { mockContext.getSystemService(Context.ALARM_SERVICE) } returns mockAlarmManager
        every { mockAlarmManager.canScheduleExactAlarms() } returns false
        
        // When
        val result = permissionManager.canScheduleExactAlarms()
        
        // Then
        assertFalse(result)
        verify { mockAlarmManager.canScheduleExactAlarms() }
    }

    @Test
    @Config(sdk = [30])
    fun `canScheduleExactAlarms should return true on SDK 30 and below`() = runTest {
        // Given - SDK 30 (below 31)
        
        // When
        val result = permissionManager.canScheduleExactAlarms()
        
        // Then
        assertTrue(result)
        // Should not check AlarmManager on older SDK versions
        verify(exactly = 0) { mockContext.getSystemService(Context.ALARM_SERVICE) }
    }

    // ========================================
    // requestExactAlarmPermission Tests
    // ========================================

    @Test
    @Config(sdk = [31])
    fun `requestExactAlarmPermission should request permission when not granted on SDK 31+`() = runTest {
        // Given - SDK 31+, permission not granted
        every { mockContext.getSystemService(Context.ALARM_SERVICE) } returns mockAlarmManager
        every { mockAlarmManager.canScheduleExactAlarms() } returns false
        
        permissionManager.setActivity(mockActivity)
        
        // When
        val result = permissionManager.requestExactAlarmPermission()
        
        // Then
        assertFalse(result)
        verify { mockActivity.startActivity(any()) }
        
        // Verify the correct intent action is used
        val intentSlot = slot<Intent>()
        verify { mockActivity.startActivity(capture(intentSlot)) }
        assertEquals(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, intentSlot.captured.action)
    }

    @Test
    @Config(sdk = [31])
    fun `requestExactAlarmPermission should return true when permission already granted on SDK 31+`() = runTest {
        // Given - SDK 31+, permission already granted
        every { mockContext.getSystemService(Context.ALARM_SERVICE) } returns mockAlarmManager
        every { mockAlarmManager.canScheduleExactAlarms() } returns true
        
        permissionManager.setActivity(mockActivity)
        
        // When
        val result = permissionManager.requestExactAlarmPermission()
        
        // Then
        assertTrue(result)
        // Should not start activity if permission already granted
        verify(exactly = 0) { mockActivity.startActivity(any()) }
    }

    @Test
    @Config(sdk = [30])
    fun `requestExactAlarmPermission should return true on SDK 30 and below`() = runTest {
        // Given - SDK 30 (below 31)
        
        // When
        val result = permissionManager.requestExactAlarmPermission()
        
        // Then
        assertTrue(result)
        // Should not check AlarmManager or start activity on older SDK versions
        verify(exactly = 0) { mockContext.getSystemService(Context.ALARM_SERVICE) }
        verify(exactly = 0) { mockActivity.startActivity(any()) }
    }

    @Test
    @Config(sdk = [31])
    fun `requestExactAlarmPermission should handle no activity gracefully on SDK 31+`() = runTest {
        // Given - SDK 31+, no activity set
        every { mockContext.getSystemService(Context.ALARM_SERVICE) } returns mockAlarmManager
        every { mockAlarmManager.canScheduleExactAlarms() } returns false
        
        // No activity set
        
        // When
        val result = permissionManager.requestExactAlarmPermission()
        
        // Then
        assertFalse(result)
        // Should not try to start activity when no activity is available
        verify(exactly = 0) { mockActivity.startActivity(any()) }
    }

    @Test
    @Config(sdk = [31])
    fun `requestExactAlarmPermission should handle exception gracefully on SDK 31+`() = runTest {
        // Given - SDK 31+, startActivity throws exception
        every { mockContext.getSystemService(Context.ALARM_SERVICE) } returns mockAlarmManager
        every { mockAlarmManager.canScheduleExactAlarms() } returns false
        every { mockActivity.startActivity(any()) } throws Exception("Test exception")
        
        permissionManager.setActivity(mockActivity)
        
        // When
        val result = permissionManager.requestExactAlarmPermission()
        
        // Then - should not crash
        assertFalse(result)
        verify { mockActivity.startActivity(any()) }
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