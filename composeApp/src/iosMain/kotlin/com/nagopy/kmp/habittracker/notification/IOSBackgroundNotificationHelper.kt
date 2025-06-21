package com.nagopy.kmp.habittracker.notification

import com.nagopy.kmp.habittracker.util.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Helper class to bridge Swift background tasks with Kotlin notification scheduling
 */
object IOSBackgroundNotificationHelper : KoinComponent {
    
    private val notificationScheduler: IOSNotificationScheduler by inject()
    
    /**
     * Called from Swift BGTaskScheduler to refresh notifications
     */
    fun performBackgroundRefresh() {
        Logger.d("Background notification refresh requested from Swift", "IOSBackgroundNotificationHelper")
        
        CoroutineScope(Dispatchers.Default).launch {
            try {
                notificationScheduler.performBackgroundRefresh()
                Logger.i("Background notification refresh completed", "IOSBackgroundNotificationHelper")
            } catch (e: Exception) {
                Logger.e(e, "Background notification refresh failed", "IOSBackgroundNotificationHelper")
            }
        }
    }
}