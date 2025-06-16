package com.nagopy.kmp.habittracker.presentation.navigation

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Unit tests for navigation routes and functionality
 */
class HabitTrackerNavigationTest {
    
    @Test
    fun `editHabit should generate correct route with habitId`() {
        // Given
        val habitId = 123L
        
        // When
        val route = HabitTrackerRoutes.editHabit(habitId)
        
        // Then
        assertEquals("edit_habit/123", route)
    }
    
    @Test
    fun `routes should have correct base values`() {
        // Verify all route constants are defined correctly
        assertEquals("habit_list", HabitTrackerRoutes.HABIT_LIST)
        assertEquals("add_habit", HabitTrackerRoutes.ADD_HABIT)
        assertEquals("edit_habit/{habitId}", HabitTrackerRoutes.EDIT_HABIT)
        assertEquals("today", HabitTrackerRoutes.TODAY)
    }
}