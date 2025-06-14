package com.nagopy.kmp.habittracker.presentation.internationalization

import habittracker.composeapp.generated.resources.Res
import habittracker.composeapp.generated.resources.*
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertNotEquals

/**
 * Test class to verify that internationalization resources are properly configured
 * and that strings can be loaded correctly.
 */
class InternationalizationTest {

    @Test
    fun `should load basic string resources`() = runBlocking {
        // Test that basic string resources can be loaded
        val appName = getString(Res.string.app_name)
        val todaysTasks = getString(Res.string.todays_tasks)
        val addHabit = getString(Res.string.add_habit)
        
        // Verify that strings are not empty
        assertTrue(appName.isNotEmpty(), "App name should not be empty")
        assertTrue(todaysTasks.isNotEmpty(), "Today's tasks string should not be empty")
        assertTrue(addHabit.isNotEmpty(), "Add habit string should not be empty")
    }

    @Test
    fun `should format string resources with parameters`() = runBlocking {
        // Test parameterized strings
        val errorMessage = getString(Res.string.error_prefix, "Test error")
        val taskError = getString(Res.string.failed_to_complete_task, "Network error")
        val greeting = getString(Res.string.hello_platform, "Android")
        
        // Verify that parameters are properly inserted
        assertTrue(errorMessage.contains("Test error"), "Error message should contain the parameter")
        assertTrue(taskError.contains("Network error"), "Task error should contain the parameter")
        assertTrue(greeting.contains("Android"), "Greeting should contain the platform name")
    }

    @Test
    fun `should load navigation strings`() = runBlocking {
        // Test navigation-related strings
        val back = getString(Res.string.back)
        val cancel = getString(Res.string.cancel)
        val save = getString(Res.string.save)
        val retry = getString(Res.string.retry)
        
        // Verify that navigation strings are loaded
        assertTrue(back.isNotEmpty(), "Back string should not be empty")
        assertTrue(cancel.isNotEmpty(), "Cancel string should not be empty")
        assertTrue(save.isNotEmpty(), "Save string should not be empty")
        assertTrue(retry.isNotEmpty(), "Retry string should not be empty")
    }

    @Test
    fun `should load habit edit screen strings`() = runBlocking {
        // Test habit edit screen strings
        val habitName = getString(Res.string.habit_name_required)
        val description = getString(Res.string.description_optional)
        val chooseColor = getString(Res.string.choose_color)
        val schedule = getString(Res.string.schedule)
        val frequency = getString(Res.string.frequency)
        
        // Verify that habit edit strings are loaded
        assertTrue(habitName.isNotEmpty(), "Habit name string should not be empty")
        assertTrue(description.isNotEmpty(), "Description string should not be empty")
        assertTrue(chooseColor.isNotEmpty(), "Choose color string should not be empty")
        assertTrue(schedule.isNotEmpty(), "Schedule string should not be empty")
        assertTrue(frequency.isNotEmpty(), "Frequency string should not be empty")
    }

    @Test
    fun `should load frequency type strings`() = runBlocking {
        // Test frequency type strings
        val onceDaily = getString(Res.string.once_daily)
        val hourly = getString(Res.string.hourly)
        val customInterval = getString(Res.string.custom_interval)
        
        // Verify that frequency strings are loaded and different
        assertTrue(onceDaily.isNotEmpty(), "Once daily string should not be empty")
        assertTrue(hourly.isNotEmpty(), "Hourly string should not be empty")
        assertTrue(customInterval.isNotEmpty(), "Custom interval string should not be empty")
        
        // Verify they are different strings
        assertNotEquals(onceDaily, hourly, "Once daily and hourly should be different")
        assertNotEquals(hourly, customInterval, "Hourly and custom interval should be different")
        assertNotEquals(onceDaily, customInterval, "Once daily and custom interval should be different")
    }

    @Test
    fun `should load permission dialog strings`() = runBlocking {
        // Test permission dialog strings
        val notificationPermission = getString(Res.string.notification_permission)
        val notificationExplanation = getString(Res.string.notification_permission_explanation)
        val reminderPermission = getString(Res.string.reminder_permission)
        val ok = getString(Res.string.ok)
        val skip = getString(Res.string.skip)
        
        // Verify that permission strings are loaded
        assertTrue(notificationPermission.isNotEmpty(), "Notification permission string should not be empty")
        assertTrue(notificationExplanation.isNotEmpty(), "Notification explanation should not be empty")
        assertTrue(reminderPermission.isNotEmpty(), "Reminder permission string should not be empty")
        assertTrue(ok.isNotEmpty(), "OK string should not be empty")
        assertTrue(skip.isNotEmpty(), "Skip string should not be empty")
    }

    @Test
    fun `should load error message strings`() = runBlocking {
        // Test error message strings
        val nameRequired = getString(Res.string.name_is_required)
        val failedToLoad = getString(Res.string.failed_to_load_habit)
        val failedToSave = getString(Res.string.failed_to_save_habit)
        val habitNotFound = getString(Res.string.habit_not_found)
        
        // Verify that error strings are loaded
        assertTrue(nameRequired.isNotEmpty(), "Name required string should not be empty")
        assertTrue(failedToLoad.isNotEmpty(), "Failed to load string should not be empty")
        assertTrue(failedToSave.isNotEmpty(), "Failed to save string should not be empty")
        assertTrue(habitNotFound.isNotEmpty(), "Habit not found string should not be empty")
    }
}