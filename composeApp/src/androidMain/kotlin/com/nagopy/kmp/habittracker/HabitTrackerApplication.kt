package com.nagopy.kmp.habittracker

import android.app.Application
import com.nagopy.kmp.habittracker.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class HabitTrackerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        initKoin {
            androidLogger()
            androidContext(this@HabitTrackerApplication)
        }
    }
}