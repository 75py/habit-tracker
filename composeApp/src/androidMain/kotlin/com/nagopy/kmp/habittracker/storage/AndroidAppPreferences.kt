package com.nagopy.kmp.habittracker.storage

import android.content.Context
import android.content.SharedPreferences
import com.nagopy.kmp.habittracker.domain.storage.AppPreferences

/**
 * Android implementation of AppPreferences using SharedPreferences.
 */
class AndroidAppPreferences(context: Context) : AppPreferences {
    
    private val preferences: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, 
        Context.MODE_PRIVATE
    )
    
    override suspend fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return preferences.getBoolean(key, defaultValue)
    }
    
    override suspend fun setBoolean(key: String, value: Boolean) {
        preferences.edit().putBoolean(key, value).apply()
    }
    
    companion object {
        private const val PREFS_NAME = "habit_tracker_prefs"
    }
}