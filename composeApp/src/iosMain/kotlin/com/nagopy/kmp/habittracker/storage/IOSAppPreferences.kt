package com.nagopy.kmp.habittracker.storage

import com.nagopy.kmp.habittracker.domain.storage.AppPreferences
import platform.Foundation.NSUserDefaults

/**
 * iOS implementation of AppPreferences using NSUserDefaults.
 */
class IOSAppPreferences : AppPreferences {
    
    private val userDefaults = NSUserDefaults.standardUserDefaults
    
    override suspend fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        val value = userDefaults.objectForKey(key)
        return if (value != null) {
            userDefaults.boolForKey(key)
        } else {
            defaultValue
        }
    }
    
    override suspend fun setBoolean(key: String, value: Boolean) {
        userDefaults.setBool(value, key)
        userDefaults.synchronize()
    }
}