package com.nagopy.kmp.habittracker.domain.storage

/**
 * Simple interface for storing app preferences.
 * This abstracts platform-specific storage mechanisms.
 */
interface AppPreferences {
    
    /**
     * Get a boolean preference value.
     * 
     * @param key The preference key
     * @param defaultValue The default value if key doesn't exist
     * @return The stored value or default value
     */
    suspend fun getBoolean(key: String, defaultValue: Boolean = false): Boolean
    
    /**
     * Set a boolean preference value.
     * 
     * @param key The preference key
     * @param value The value to store
     */
    suspend fun setBoolean(key: String, value: Boolean)
    
    companion object {
        const val KEY_NOTIFICATION_PERMISSION_REQUESTED = "notification_permission_requested"
    }
}